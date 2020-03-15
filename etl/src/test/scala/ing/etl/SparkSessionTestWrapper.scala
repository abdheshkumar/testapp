package ing.etl

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{StructField, StructType}

trait SparkSessionTestWrapper {
  lazy val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("spark session")
      .config("spark.ui.enabled", false)
      .config("spark.sql.shuffle.partitions", "1")
      .getOrCreate()
  }

  implicit class Ops(df: DataFrame) {
    def setNullableStateOfColumn(cn: (String, Boolean)*): DataFrame = {

      // get schema
      val schema = df.schema
      // modify [[StructField] with name `cn`
      val newSchema = StructType(schema.map { s =>
        cn.find(_._1 == s.name) match {
          case Some((_, nullable)) =>
            s.copy(nullable = nullable)
          case None => s
        }
      })
      // apply new schema
      df.sqlContext.createDataFrame(df.rdd, newSchema)
    }
  }
}
