import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}
import com.typesafe.config.Config
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import io.circe.parser._
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
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

class IngestionKafkaToPostgres {

  private def consumerSettings(
    topic: String
  )(implicit system: ActorSystem): ConsumerSettings[String, String] = {
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withGroupId(s"${topic}_group")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")/*
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")*/
  }

  def consumerStream(topic: String)(
    implicit system: ActorSystem
  ): Source[ConsumerRecord[String, String], Consumer.Control] = {
    Consumer
      .plainSource(consumerSettings(topic), Subscriptions.topics(topic))
  }

  implicit val meetUpEncoder: Decoder[MeetUp] = new Decoder[MeetUp] {
    override def apply(c: HCursor): Result[MeetUp] = {
      for {
        eventId <- c.get[String]("event_id")
        eventName <- c.get[String]("event_name")
        groupId <- c.get[String]("group_id")
        groupName <- c.get[String]("group_name")
        groupCountry <- c.get[String]("group_country")
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
          urlkey <- c.get[String]("urlkey")
          count <- c.get[Int]("count")
        } yield TrendingTopic(country, urlkey, topicName, count)
      }
    }

  case class TrendingTopic(country: String,
                           urlkey: String,
                           topicName: String,
                           count: Int)

  case class MeetUp(eventId: String,
                    eventName: String,
                    groupId: String,
                    groupName: String,
                    groupCountry: String,
                    groupLon: Float,
                    groupLat: Float,
                    yesResponse: Int,
                    noResponse: Int)

/*  def meetByEventTableFlow(
    dbSession: DBSession
  )(implicit ec: ExecutionContext): Flow[CommittableMessage[String, String],
                                         (Int, CommittableOffset),
                                         NotUsed] = {
    import dbSession.profile.api._
    Flow[ConsumerMessage.CommittableMessage[String, String]].mapAsync(1) {
      kafkaMessage =>
        val meetupEither =
          parse(kafkaMessage.record.value()).flatMap(_.as[MeetUp])

        meetupEither match {
          case Right(meetUp) =>
            val statement = sqlu"""
                 INSERT INTO meetup.meetup_by_event_id(
                 event_id,
                 event_name,
                 group_id,
                 group_name,
                 group_country,
                 group_lon,
                 group_lat,
                 yes,
                 no) VALUES(${meetUp.eventId}, ${meetUp.eventName},${meetUp.groupId},${meetUp.groupName},${meetUp.groupCountry},${meetUp.groupLon},${meetUp.groupLat},${meetUp.yesResponse},${meetUp.noResponse}) ON CONFLICT (event_id)
                 DO UPDATE SET
                 yes=meetup_by_event_id.yes,
                 no=meetup_by_event_id.no
          """

            dbSession.db
              .run(statement)
              .map(f => (f, kafkaMessage.committableOffset))
          case Left(value) => Future.failed(value)
        }
    }
  }*/
/*
  def meetUpByEventTableStream(topic: String, dbSession: DBSession)(
    implicit system: ActorSystem,
    ec: ExecutionContext,
    ac: ActorMaterializer
  ): Future[Done] =
    consumerStream(topic)
      .via(meetByEventTableFlow(dbSession))
      .log(topic)
      .runWith(Sink.ignore)*/

  def trendingTableFlow(
    dbSession: DBSession
  )(implicit ec: ExecutionContext): Flow[ConsumerRecord[String, String],
                                         Int,
                                         NotUsed] = {
    import dbSession.profile.api._
    Flow[ConsumerRecord[String, String]].mapAsync(1) {
      kafkaMessage =>
        println("***********before**************\n\n\n\n\\"+ kafkaMessage)
        val trendingEither =
          parse(kafkaMessage.value()).flatMap(_.as[TrendingTopic])
        println("*****************after********\n\n\n\n\\"+ trendingEither)
        trendingEither match {
          case Right(trending) =>

            val statement = sqlu"""
                 INSERT INTO meetup.trending(
                 country,
                 topic_name,
                 count) VALUES(${trending.country}, ${trending.topicName},${trending.count}) ON CONFLICT (country,topic_name)
                 DO UPDATE SET
                 count=trending.count
          """

            val r = dbSession.db
              .run(statement)
              .map(f => f)
            r.transform(t=>{
              println("**************tras*")

              t
            })
          case Left(value) =>
            println("*******failed******************\n\n\n\n\\"+ value)
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
      //.log(topic)
      .runWith(Sink.ignore)
}
