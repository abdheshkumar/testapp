import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder.Result
import io.circe.parser._
import io.circe.{Decoder, HCursor}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

sealed abstract class DBSession {
  val db: JdbcBackend#Database
  val profile: JdbcProfile

  /**
    * You are responsible for closing the database after use!!
    */
  def close(): Unit = db.close()
}

object DBSession {
  private final class DBSessionImpl(val slick: DatabaseConfig[JdbcProfile])
      extends DBSession {
    val db: JdbcBackend#Database = slick.db
    val profile: JdbcProfile = slick.profile
  }
  def forConfig(path: String, config: Config): DBSession =
    new DBSessionImpl(DatabaseConfig.forConfig[JdbcProfile](path, config))
}

class IngestionKafkaToPostgres extends LazyLogging {

  private def consumerSettings(
    topic: String
  )(implicit system: ActorSystem): ConsumerSettings[String, String] = {
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withGroupId(s"${topic}_group")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
  }

  def consumerStream(topic: String)(
    implicit system: ActorSystem
  ): Source[ConsumerMessage.CommittableMessage[String, String],
            Consumer.Control] = {
    Consumer
      .committableSource(consumerSettings(topic), Subscriptions.topics(topic))
  }

  implicit val meetUpEncoder: Decoder[MeetUp] = new Decoder[MeetUp] {
    override def apply(c: HCursor): Result[MeetUp] = {
      for {
        eventId <- c.get[String]("event_id")
        eventName <- c.get[String]("event_name")
        groupId <- c.get[String]("group_id")
        groupName <- c.get[String]("group_name")
        groupCountry <- c.get[String]("group_country")
        groupCity <- c.get[String]("group_city")
        groupLon <- c.get[Float]("group_lon")
        groupLat <- c.get[Float]("group_lat")
        yesResponse <- c.get[Int]("yes")
        noResponse <- c.get[Int]("no")
      } yield
        MeetUp(
          eventId,
          eventName,
          groupId,
          groupName,
          groupCountry,
          groupCity,
          groupLon,
          groupLat,
          yesResponse,
          noResponse
        )
    }
  }

  implicit val trendingTopicEncoder: Decoder[TrendingTopic] =
    new Decoder[TrendingTopic] {
      override def apply(c: HCursor): Result[TrendingTopic] = {
        for {
          country <- c.get[String]("country")
          topicName <- c.get[String]("topic_name")
          count <- c.get[Int]("count")
        } yield TrendingTopic(country, topicName, count)
      }
    }

  case class TrendingTopic(country: String, topicName: String, count: Int)

  case class MeetUp(eventId: String,
                    eventName: String,
                    groupId: String,
                    groupName: String,
                    groupCountry: String,
                    groupCity: String,
                    groupLon: Float,
                    groupLat: Float,
                    yesResponse: Int,
                    noResponse: Int)

  def meetByEventTableFlow(dbSession: DBSession)(
    implicit ec: ExecutionContext
  ): Flow[ConsumerMessage.CommittableMessage[String, String], Int, NotUsed] = {
    import dbSession.profile.api._
    Flow[ConsumerMessage.CommittableMessage[String, String]].mapAsync(1) {
      kafkaMessage =>
        val meetupEither =
          parse(kafkaMessage.record.value()).flatMap(_.as[MeetUp])

        meetupEither match {
          case Right(meetUp) =>
            val statement = sqlu"""
                 INSERT INTO meetup.trending_events(
                 event_id,
                 event_name,
                 group_id,
                 group_name,
                 group_country,
                 group_city,
                 group_lon,
                 group_lat,
                 yes,
                 no) VALUES(${meetUp.eventId}, ${meetUp.eventName},${meetUp.groupId},${meetUp.groupName},${meetUp.groupCountry},${meetUp.groupCity},${meetUp.groupLon},${meetUp.groupLat},${meetUp.yesResponse},${meetUp.noResponse}) ON CONFLICT (event_id)
                 DO UPDATE SET
                 yes=EXCLUDED.yes,
                 no=EXCLUDED.no
          """

            dbSession.db
              .run(statement)
              .map(f => f)
          case Left(value) => Future.failed(value)
        }
    }
  }

  def meetUpByEventTableStream(topic: String, dbSession: DBSession)(
    implicit system: ActorSystem,
    ec: ExecutionContext,
    ac: ActorMaterializer
  ): Future[Done] =
    consumerStream(topic)
      .via(meetByEventTableFlow(dbSession))
      .log(topic)
      .runWith(Sink.ignore)

  def trendingTableFlow(dbSession: DBSession)(
    implicit ec: ExecutionContext
  ): Flow[ConsumerMessage.CommittableMessage[String, String], Int, NotUsed] = {
    import dbSession.profile.api._
    Flow[ConsumerMessage.CommittableMessage[String, String]].mapAsync(1) {
      kafkaMessage =>
        val trendingEither =
          parse(kafkaMessage.record.value()).flatMap(_.as[TrendingTopic])
        trendingEither match {
          case Right(trending) =>
            val statement = sqlu"""
                 INSERT INTO meetup.trending_topic(
                 country,
                 topic_name,
                 count) VALUES(${trending.country}, ${trending.topicName},${trending.count}) ON CONFLICT (country,topic_name)
                 DO UPDATE SET
                 count=EXCLUDED.count
          """

            dbSession.db
              .run(statement)
              .map(f => f)

          case Left(value) =>
            logger.error("Failed while parsing json into case class", value)
            Future.failed(value)
        }
    }
  }

  def trendingTableStream(topic: String, dbSession: DBSession)(
    implicit system: ActorSystem,
    ec: ExecutionContext,
    ac: ActorMaterializer
  ): Future[Done] =
    consumerStream(topic)
      .via(trendingTableFlow(dbSession))
      .log(topic)
      .runWith(Sink.ignore)
}
