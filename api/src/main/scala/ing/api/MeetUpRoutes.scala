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

import scala.concurrent.{ExecutionContext, Future, Promise}

trait MeetUpRoutes extends Directives with AkkaKafkaConsumer {
  implicit def system: ActorSystem
  implicit def m: Materializer

  def rsvpAkkaKafkaStream = {

    val controlPromise = Promise[Consumer.Control]()

    controlPromise -> readRsvp("test")
      .mapMaterializedValue(controlPromise.success)

  }

  def rsvpStream: Route = {
    implicit val ec = system.dispatcher
    path("rsvp") {
      extractUpgradeToWebSocket { websocket =>
        val (control, source) = rsvpAkkaKafkaStream
        val endMsgHandler: Sink[Message, Future[Done]] =
          Sink.foreachAsync(1)({
            case TextMessage.Strict(text) => {

              println("********" + text)
              control.future.flatMap(control => {
                control
                  .drainAndShutdown(Future { println("Finished consumer") })
                  .map(res => {
                    println(s">>>>>>>>>>${res}")
                    ()
                  })
              })
            }

          })

        complete(websocket.handleMessagesWithSinkSource(endMsgHandler, source))
      }
    }
  }

  def routes: Route = rsvpStream
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
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
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
  )(implicit system: ActorSystem): Source[Message, Consumer.Control] = {
    consumerStream(topic).map { elem =>
      TextMessage.Strict(elem.value())
    }
  }

}
