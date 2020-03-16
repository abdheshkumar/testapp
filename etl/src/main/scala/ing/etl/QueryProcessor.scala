package ing.etl

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.StreamingQuery

import scala.collection.Map

trait QueryProcessor { self: StreamProcessor =>

  def inputStream: DataFrame
  def jdbcOption: Map[String, String]
  def aggregatedStreamQuery: StreamingQuery =
    processMostPopularEventsStream(inputStream)(
      ExternalJdbcWriter(
        jdbcOption + ("dbtable" -> Constants.meetupByEventIdTable)
      )
    )

  def writeTrendingTopicsStream: StreamingQuery =
    processTrendingTopicsStream(inputStream)(
      ExternalJdbcWriter(
        jdbcOption + ("dbtable" -> Constants.trendingTopicsTable)
      )
    )

  def trendingEventsStreamQuery: StreamingQuery =
    processMostPopularEventsStream(inputStream)(
      KafkaStreamWriter(Constants.trendingEvents)
    )

  def trendingTopicsStream: StreamingQuery =
    processTrendingTopicsStream(inputStream)(
      KafkaStreamWriter(Constants.trendingTopics)
    )

  def startAll: Seq[StreamingQuery] =
    Seq(
      aggregatedStreamQuery,
      writeTrendingTopicsStream,
      trendingEventsStreamQuery,
      trendingTopicsStream
    )

}
