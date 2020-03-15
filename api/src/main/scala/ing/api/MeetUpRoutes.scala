package ing.api

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, OPTIONS, POST, PUT}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`
}
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.{Directives, Route}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import ing.api.dao.MeetUpRepository
import ing.api.dao.CirceEncoders._
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Encoder
import ing.api.MeetUpRoutes.Error

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait MeetUpRoutes extends Directives with AkkaKafkaConsumer with LazyLogging {
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
              logger.info("Receive message from websocket: {}", text)
              if (text == "END") {
                control.future.flatMap(control => {
                  control
                    .drainAndShutdown(Future {
                      logger.info("Finished consumer")
                    })
                    .map(res => {
                      logger
                        .info("Response from consumer shutting down: {}", res)
                      ()
                    })
                })
              } else Future.unit

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

trait AkkaKafkaConsumer extends LazyLogging{

  def consumerSettings(
    implicit system: ActorSystem
  ): ConsumerSettings[String, String] = {
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withGroupId("group2")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
  }

  def consumerStream(topic: String)(
    implicit system: ActorSystem
  ): Source[ConsumerMessage.CommittableMessage[String, String],
            Consumer.Control] = {
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic))
  }

  def readRsvp(
    topic: String
  )(implicit system: ActorSystem): Source[Message, Consumer.Control] = {
    consumerStream(topic).map { elem =>
    logger.info("Consumed message: ..")
      TextMessage.Strict(elem.record.value())
    }
  }

}
