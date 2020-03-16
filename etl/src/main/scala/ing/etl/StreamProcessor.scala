package ing.etl

import ing.etl.Constants.{NO, YES}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

trait StreamProcessor extends HelperFunctions {
  self: SparkBoot =>
  import spark.implicits._

  /**
    * Process most popular events happening in the world and materialized in external sink
    * @param stream: DataFrame input stream
    * @return
    */
  def processMostPopularEventsStream(
    stream: DataFrame
  )(writer: WriteIntoExternalSource): StreamingQuery = {
    writer.writeDataIntoExternalSource(stream)(
      findMostPopularLocationsInTheWorldByEventId
    )
  }

  /**
    * Process most trending topics by country and materialzed aggregation in external sink
    * @param stream DataFrame input stream
    * @return
    */
  def processTrendingTopicsStream(
    stream: DataFrame
  )(writer: WriteIntoExternalSource): StreamingQuery = {
    writer.writeDataIntoExternalSource(stream)(trendingTopicsByCountry)
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
      .agg(count("topic_name").as("count"))
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
