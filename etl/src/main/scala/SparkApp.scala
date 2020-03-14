import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{
  Column,
  DataFrame,
  Encoder,
  Encoders,
  ForeachWriter,
  Row,
  SaveMode
}
case class Rsvp(venue: Venue,
                response: String,
                member: Member,
                mtime: Long,
                event: Event,
                group: Group)
case class Venue(venue_name: String,
                 venue_id: String,
                 lon: Option[Float],
                 lat: Option[Float])

case class Event(event_id: String,
                 event_name: String,
                 event_url: String,
                 time: Long)
case class GroupTopics(urlkey: String, topic_name: String)
case class Group(group_id: String,
                 group_urlname: String,
                 group_name: String,
                 group_city: String,
                 group_lat: Float,
                 group_lon: Float,
                 group_country: String,
                 group_topics: Seq[GroupTopics])

case class Member(member_name: String, member_id: String, photo: String)

object MainApp extends App {
  import org.apache.spark.sql.SparkSession
  import org.apache.spark.sql.functions._
  case class TrendingTopics(country: String, topics: Iterator[String])

  lazy val sparkConf = new SparkConf()
    .setAppName("Learn Spark")
    .setMaster("local[*]")
    .set("spark.cores.max", "2")

  val spark = SparkSession.builder
    .config(sparkConf)
    .getOrCreate()

  implicit val rsvpEncoder: Encoder[Rsvp] = Encoders.product[Rsvp]
  import spark.implicits._

  val YES = "yes"
  val NO = "no"

  val stream = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "test")
    .option("startingOffsets", "earliest") // From starting
    .load()
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .select(from_json($"value", rsvpEncoder.schema).as("rsvp"))
    .select("rsvp.*")

  /*  val kafkaStreamTrending = stream
    .transform(findMostPopularLocationsInTheWorldByEventId)
    .selectExpr("","to_json(struct(*)) AS value")
    .writeStream
    .format("kafka")
    .outputMode("update")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("topic", "topic2")
    .option("checkpointLocation", "ing_meetup/spark/checkpoints/topic2")
    .start()*/

  val postgresProperties = scala.collection.Map(
    "url" -> "jdbc:postgresql://localhost/postgres",
    "dbtable" -> "meetup.meetup_by_event_id",
    "driver" -> "org.postgresql.Driver",
    "user" -> "postgres",
    "password" -> "postgres"
  )
  val trendingStream = stream
  writeIntoDB(stream, "meetup.meetup_by_event_id")(
    findMostPopularLocationsInTheWorldByEventId
  )

  val kafkaStream =
    writeIntoDB(stream, "meetup.trending")(trendingTopicsByCountry("us"))

  spark.streams.awaitAnyTermination()
  //kafkaStreamTrending.awaitTermination()
  //kafkaStream.awaitTermination()

  def writeIntoDB(df: DataFrame, tableName: String)(
    transformFunc: DataFrame => DataFrame
  ): StreamingQuery = {
    df.transform(transformFunc)
      .writeStream
      .option("checkpointLocation", s"ing_meetup/spark/checkpoints/$tableName")
      .outputMode(OutputMode.Update())
      .foreachBatch { (df: DataFrame, _: Long) =>
        df.write
          .format("jdbc")
          .options(postgresProperties + ("dbtable" -> tableName))
          .mode(SaveMode.Overwrite)
          .save()

      }
      .start()
  }

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
    //.orderBy(col("count").desc)
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
        $"event.event_id".as("event_id"),
        $"response",
        $"event.event_name".as("event_name")
      )
      .transform(transformRsvpResponse)
      .groupBy("event_id")
      .agg(
        getFirst("group_city"),
        getFirst("group_country"),
        getFirst("group_id"),
        getFirst("group_name"),
        getFirst("group_lon"),
        getFirst("group_lat"),
        getFirst("event_name"),
        sum(YES).alias(YES),
        sum(NO).alias(NO)
      )
    //.orderBy(col(YES).desc)
  }

}
