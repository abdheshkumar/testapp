import java.util.concurrent.TimeUnit

import org.apache.spark.SparkConf
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.streaming.{ProcessingTime, Trigger}
import org.apache.spark.sql.{Column, DataFrame, Encoder, Encoders}
case class Rsvp(venue: Venue,
                response: String,
                member: Member,
                mtime: Long,
                event: Event,
                group: Group)
case class Venue(venueName: String,
                 venueId: String,
                 lon: Option[Float],
                 lat: Option[Float])

case class Event(eventId: String,
                 eventName: String,
                 eventUrl: String,
                 time: Long)
case class GroupTopics(urlkey: String, topicName: String)
case class Group(groupId: String,
                 groupUrlname: String,
                 groupName: String,
                 groupCity: String,
                 groupLat: Float,
                 groupLon: Float,
                 groupCountry: String,
                 groupTopics: Seq[GroupTopics])

case class Member(memberName: String, memberId: String, photo: String)

object MainApp extends App {
//https://databricks.com/blog/2016/02/09/reshaping-data-with-pivot-in-apache-spark.html
  //https://databricks.com/session/pivoting-data-with-sparksql
  import org.apache.spark.sql.functions._
  import org.apache.spark.sql.SparkSession
  import org.apache.spark.sql.expressions.scalalang.typed
  case class TrendingTopics(country: String, topics: Iterator[String])

  lazy val sparkConf = new SparkConf()
    .setAppName("Learn Spark")
    .setMaster("local[*]")
    .set("spark.cores.max", "2")

  val spark = SparkSession.builder
    .config(sparkConf)
    .getOrCreate()

  /*  implicit val venueEncoder: Encoder[Venue] = Encoders.product[Venue]
    implicit val memberEncoder: Encoder[Member] = Encoders.product[Member]
    implicit val eventEncoder: Encoder[Event] = Encoders.product[Event]
    implicit val groupMemberEncoder: Encoder[GroupTopics] =
      Encoders.product[GroupTopics]
    implicit val groupEncoder: Encoder[Group] = Encoders.product[Group]
    implicit val rsvpEncoder: Encoder[Rsvp] = Encoders.product[Rsvp]*/

  import spark.implicits._

  /*case class Choice(name: String, choice: String)
  val r = Seq(
    Choice("A", "Y"),
    Choice("A", "N"),
    Choice("B", "Y"),
    Choice("A", "N"),
    Choice("A", "N")
  ).toDS
    .groupBy($"name")
    .pivot($"choice", Seq("Y", "N"))
    .count()
    .show*/
  /*case class Test(c: String, topics: Seq[String])

  val df =
    Seq(
      Test("1", Seq("a", "b", "a", "c")),
      Test("1", Seq("c", "d")),
      Test("2", Seq("ec", "e"))
    ).toDF()

  df.groupBy("c")
    .agg(flatten(collect_list($"topics")).as("topics"))
    .select($"c", explode($"topics").as("topics"))
    .groupBy("c", "topics")
    .agg(count(col("topics")).as("count"))
    .filter($"c" === "1")
    .orderBy(col("count").desc)
    .show()*/
  val YES = "yes"
  val NO = "no"
  val s = spark.read
  //.schema(rsvpEncoder.schema)
    .json(
      "/home/abdhesh/Documents/code-2019/ing_meetup/etl/src/main/resources/meetup.json"
    )

  /* s.transform(trendingTopicsByCountry)
    .writeStream
    .format("console")
    .trigger(Trigger.ProcessingTime(5, TimeUnit.SECONDS))
    .start()*/
  s.transform(trendingTopicsByCountry("us")).show()

  /**
    * Trending topics by country
    * @param df
    * @return
    */
  def trendingTopicsByCountry(country: String)(df: DataFrame) = {
    df.filter($"group.group_country" === country)
      .select(
        $"group.group_country".as("country"),
        explode($"group.group_topics.topic_name").as("topics")
      )
      .groupBy("country", "topics")
      .agg(count($"topics").as("count"))
      .orderBy(col("count").desc)
  }

  /**
    * Categories response yes/no to 1/0
    * @param df
    * @return
    */
  def transformRsvpResponse(df: DataFrame): DataFrame = {
    val transformYesNoToNumeric: String => String => Int = yesNo =>
      columnValue => if (yesNo == columnValue) 1 else 0
    val yes = udf(transformYesNoToNumeric(YES))
    val no = udf(transformYesNoToNumeric(NO))
    df.withColumn(YES, yes($"response"))
      .withColumn(NO, no($"response"))
  }

  def getFirst(columnName: String): Column =
    first(col(columnName)).as(columnName)

  /**
    * GroupBy event_id
    * @param df
    * @return
    */
  def findMostPopularLocationsInTheWorldByEventId(df: DataFrame): DataFrame = {
    df.select(
        $"group.group_city".as("group_city"),
        $"group.group_country".as("group_country"),
        $"group.group_id".as("group_id"),
        $"group.group_name".as("group_name"),
        $"group.group_lon".as("group_lon"),
        $"group.group_lat".as("group_lat"),
        $"event.event_id".as("eventId"),
        $"response",
        $"event.event_name".as("eventName")
      )
      .transform(transformRsvpResponse)
      .groupBy("eventId")
      .agg(
        getFirst("group_city"),
        getFirst("group_country"),
        getFirst("group_id"),
        getFirst("group_name"),
        getFirst("group_lon"),
        getFirst("group_lat"),
        getFirst("eventName"),
        sum(YES).alias(YES),
        sum(NO).alias(NO)
      )
      .orderBy(col(YES).desc)
  }

}
