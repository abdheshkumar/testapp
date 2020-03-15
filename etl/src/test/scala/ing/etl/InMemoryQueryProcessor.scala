package ing.etl

import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.{DataFrame, SparkSession}

trait InMemoryQueryProcessor { self: SparkSessionTestWrapper =>
  type Logic = QueryProcessor with StreamProcessor with SparkBoot
  def processor[T](intsInput: MemoryStream[T]): Logic =
    new QueryProcessor with StreamProcessor with SparkBoot {
      override val inputStream: DataFrame = intsInput.toDF

      override val jdbcOption: collection.Map[String, String] =
        Constants.h2Properties
      override val spark: SparkSession = self.spark
    }

}
