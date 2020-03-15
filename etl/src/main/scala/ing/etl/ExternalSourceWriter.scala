package ing.etl

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, DataFrameWriter, Row, SaveMode}

import scala.collection.Map

trait ExternalJdbcOption {
  def options: Map[String, String]
}

trait ExternalJdbcWriter { self: ExternalJdbcOption =>

  /**
    * Write given data into JDBC table
    * @param tableName
    * @param df
    * @return
    */
  def writeBatchDataIntoJdbc(
    tableName: String
  )(df: DataFrame): DataFrameWriter[Row] = {
    df.write
      .format("jdbc")
      .options(options)
      .mode(SaveMode.Append)
  }

  /**
    * Write Batch aggregation into external source. Here we writing into Postgres
    * @param df: DataFrame incoming dataframe
    * @param tableName: String Postgres table in which spark will write batch aggregation
    * @param transformFunc: DataFrame => DataFrame function transform streaming into aggregated data by applying the passed function
    * @return
    */
  def writeDataIntoExternalSource(df: DataFrame, tableName: String)(
    transformFunc: DataFrame => DataFrame
  ): StreamingQuery = {
    df.transform(transformFunc)
      .writeStream
      .option("checkpointLocation", s"ing_meetup/spark/checkpoints/$tableName")
      .outputMode(OutputMode.Update())
      .foreachBatch { (df: DataFrame, _: Long) =>
        val writeBatch = writeBatchDataIntoJdbc(tableName)(df)
        writeBatch.save()
      }
      .start()
  }

}

object ExternalJdbcWriter {
  def apply(
    opts: Map[String, String]
  ): ExternalJdbcWriter with ExternalJdbcOption =
    new ExternalJdbcWriter with ExternalJdbcOption {
      override def options: Map[String, String] = opts
    }
}
