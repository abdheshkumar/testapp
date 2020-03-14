package ing.api

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, OPTIONS, POST, PUT}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{
  `Access-Control-Allow-Headers`,
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`
}
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.{Directives, Route}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import ing.api.dao.MeetUpRepository
import ing.api.dao.CirceEncoders._
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Encoder
import ing.api.MeetUpRoutes.Error

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait MeetUpRoutes extends Directives with AkkaKafkaConsumer {
  implicit def system: ActorSystem
  implicit def m: Materializer

  def rsvpAkkaKafkaStream: (Promise[Consumer.Control], Source[Message, Any]) = {

    val controlPromise: Promise[Consumer.Control] = Promise[Consumer.Control]()

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

  def mostPopularEvents(repository: MeetUpRepository): Route = path("events") {
    onComplete(repository.getMostPopularMeetUpInTheWorld(10)) {
      case Failure(exception) =>
        complete((StatusCodes.InternalServerError, Error(exception.getMessage)))
      case Success(value) => complete(value)
    }
  }

  def trendingTopicsByCountry(repository: MeetUpRepository): Route =
    path("trending" / Segment) { country =>
      onComplete(repository.getTrendingTopicsByCountry(country, 10)) {
        case Failure(exception) =>
          complete(
            (StatusCodes.InternalServerError, Error(exception.getMessage))
          )
        case Success(value) => complete(value)
      }
    }

  def routes(repository: MeetUpRepository): Route =
    withCorsHeader(
      concat(
        rsvpStream,
        mostPopularEvents(repository),
        trendingTopicsByCountry(repository)
      )
    )

  private def withCorsHeader: Route => Route =
    next =>
      respondWithHeaders(
        `Access-Control-Allow-Methods`(OPTIONS, POST, GET, PUT, DELETE),
        // `Access-Control-Allow-Headers`("Content-Type", "X-Requested-With", "domain_code", "ApiRequestContext"),
        `Access-Control-Allow-Origin`.*
      )(next)

}

object MeetUpRoutes {
  case class Error(error: String)

  object Error {
    import io.circe.generic.semiauto._
    implicit val errorEncoder: Encoder.AsObject[Error] = deriveEncoder[Error]
  }

  def apply(
    sys: ActorSystem
  )(implicit materializer: Materializer): MeetUpRoutes = new MeetUpRoutes {
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
