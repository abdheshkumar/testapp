package ing.etl

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.StreamingQuery

import scala.collection.Map

trait QueryProcessor { self: StreamProcessor =>

  def inputStream: DataFrame
  def jdbcOption: Map[String, String]
  def aggregatedStreamQuery: StreamingQuery =
    processMostPopularEventsStream(inputStream)(
      tableName =>
        ExternalJdbcWriter(
          jdbcOption + ("dbtable" -> tableName)
      )
    )

  def writeTrendingTopicsStream: StreamingQuery =
    processTrendingTopicsStream(inputStream)(
      tableName =>
        ExternalJdbcWriter(
          jdbcOption + ("dbtable" -> tableName)
      )
    )
  def startAll: Seq[StreamingQuery] =
    Seq(aggregatedStreamQuery/*, writeTrendingTopicsStream*/)

}
