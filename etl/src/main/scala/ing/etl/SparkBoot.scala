package ing.etl

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait SparkBoot {
  val spark: SparkSession
}

trait LiveSparkBoot extends SparkBoot {
  lazy val sparkConf = new SparkConf()
    .setAppName("Meetup")
    .setMaster("local[*]")
    .set("spark.cores.max", "2")

  val spark: SparkSession = SparkSession.builder
    .config(sparkConf)
    .getOrCreate()
}
