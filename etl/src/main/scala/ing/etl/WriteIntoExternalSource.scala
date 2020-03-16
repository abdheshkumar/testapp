package ing.etl

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.StreamingQuery

trait WriteIntoExternalSource {

  /**
   * Write Batch aggregation into external source. Here we writing into Postgres
   * @param df: DataFrame incoming dataframe
   * @param transformFunc: DataFrame => DataFrame function transform streaming into aggregated data by applying the passed function
   * @return
   */
  def writeDataIntoExternalSource(df: DataFrame)(
    transformFunc: DataFrame => DataFrame
  ): StreamingQuery
}