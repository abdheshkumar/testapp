import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{Future, Promise}

trait RsvpStream {
  implicit def actorSystem: ActorSystem
  implicit def materializer: ActorMaterializer

  def kafkaProducerSettings: ProducerSettings[String, String] =
    ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)

  def kafkaSink: Sink[ProducerRecord[String, String], Future[Done]] = Producer.plainSink(kafkaProducerSettings)

  def webSocketFlow: Sink[Message, Future[Done]] = {
    Flow[Message]
      .mapConcat {
        case tm: TextMessage.Strict => tm.text :: Nil
        case bm: BinaryMessage =>
          bm.dataStream.runWith(Sink.ignore)
          Nil
        case bm: TextMessage =>
          bm.textStream.runWith(Sink.ignore)
          Nil
      }
      .map(text => new ProducerRecord[String, String]("test", text))
      .toMat(kafkaSink)(Keep.right)
  }

  def flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(webSocketFlow, Source.maybe[Message])(Keep.right)

  def start(): (Future[WebSocketUpgradeResponse], Promise[Option[Message]]) =
    Http().singleWebSocketRequest(
      WebSocketRequest("ws://stream.meetup.com/2/rsvps"),
      flow
    )
}

object RsvpStream {
  def apply(implicit system: ActorSystem, mat: ActorMaterializer): RsvpStream =
    new RsvpStream {
      override val actorSystem: ActorSystem = system

      override val materializer: ActorMaterializer = mat
    }
}
