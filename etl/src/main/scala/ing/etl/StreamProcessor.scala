package ing.etl

import ing.etl.Constants.{NO, YES}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

trait StreamProcessor {
  self: SparkBoot =>
  import spark.implicits._

  /**
    * Process most popular events happening in the world and materialized in external sink
    * @param stream: DataFrame input stream
    * @param writeTable
    * @return
    */
  def processMostPopularEventsStream(
    stream: DataFrame
  )(writeTable: String => ExternalJdbcWriter): StreamingQuery = {
    val jdbcWriter = writeTable(Constants.meetupByEventIdTable)
    jdbcWriter.writeDataIntoExternalSource(
      stream,
      Constants.meetupByEventIdTable
    )(findMostPopularLocationsInTheWorldByEventId)
  }

  /**
    * Process most trending topics by country and materialzed aggregation in external sink
    * @param stream DataFrame input stream
    * @param writeTable
    * @return
    */
  def processTrendingTopicsStream(
    stream: DataFrame
  )(writeTable: String => ExternalJdbcWriter): StreamingQuery = {
    val jdbcWriter = writeTable(Constants.trendingTopicsTable)
    jdbcWriter.writeDataIntoExternalSource(
      stream,
      Constants.trendingTopicsTable
    )(trendingTopicsByCountry)
  }

  /**
    * Trending topics by country
    * @param df
    * @return
    */
  def trendingTopicsByCountry(df: DataFrame): DataFrame = {
    df.select(
        $"group.group_country".as("country"),
        explode($"group.group_topics.topic_name").as("topic_name")
      )
      .groupBy("country", "topic_name")
      .agg(count($"topic_name").as("count"))
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
      .drop("response")
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

}

object StreamProcessor {
  def create(sparkSession: SparkSession): StreamProcessor =
    new StreamProcessor with SparkBoot {
      override val spark: SparkSession = sparkSession
    }
}
