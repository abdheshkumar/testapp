import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.{Directives, Route}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future

trait MeetUpRoutes extends Directives with AkkaKafkaConsumer {
  implicit def system: ActorSystem
  implicit def m: Materializer

  def rsvpAkkaKafkaStream: Flow[Any, TextMessage, Future[Done]] =
    Flow.fromSinkAndSourceMat(Sink.foreach(println), readRsvp("test"))(
      Keep.left
    )

  def rsvpStream: Route =
    path("rsvp") {
      handleWebSocketMessages(rsvpAkkaKafkaStream)
    }

  def routes: Route = concat(rsvpStream)
}

object MeetUpRoutes {
  def apply(implicit sys: ActorSystem,
            materializer: Materializer): MeetUpRoutes = new MeetUpRoutes {
    override val system: ActorSystem = sys

    override val m: Materializer = materializer
  }
}

trait AkkaKafkaConsumer {

  def consumerSettings(
    implicit system: ActorSystem
  ): ConsumerSettings[String, String] = {
    val config = system.settings.config.getConfig("our-kafka-consumer")
    ConsumerSettings(config, new StringDeserializer, new StringDeserializer)
      .withGroupId("group2")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }

  def consumerStream(topic: String)(
    implicit system: ActorSystem
  ): Source[ConsumerRecord[String, String], Consumer.Control] = {
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
  }

  def readRsvp(
    topic: String
  )(implicit system: ActorSystem): Source[TextMessage, Consumer.Control] = {
    consumerStream(topic).map { elem =>
      TextMessage.Strict(elem.value())
    }
  }

}
