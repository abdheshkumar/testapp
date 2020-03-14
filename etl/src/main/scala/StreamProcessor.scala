import org.apache.spark.sql.{Column, DataFrame, SaveMode}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import Constants._
trait StreamProcessor {
  self: SparkBoot =>
  import spark.implicits._

  /**
    * Write Batch aggregation into external source. Here we writing into Postgres
    * @param df: DataFrame incoming dataframe
    * @param tableName: String Postgres table in which spark will write batch aggregation
    * @param transformFunc: DataFrame => DataFrame function transform streaming into aggregated data by applying the passed function
    * @return
    */
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
  def trendingTopicsByCountry(df: DataFrame): DataFrame = {
    df.select(
        $"group.group_country".as("country"),
        explode($"group.group_topics.topic_name").as("topics")
      )
      .groupBy("country", "topics")
      .agg(count($"topics").as("count"))
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
  }

  /**
    * Read streaming data from kafka and convert value into json format
    * @return
    */
  def kafkaStream =
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "test")
      .option("startingOffsets", "earliest") // From starting
      .load()
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .select(from_json($"value", Models.Rsvp.schema).as("rsvp"))
      .select("rsvp.*")
}
