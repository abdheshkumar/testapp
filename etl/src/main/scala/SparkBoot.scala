import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait SparkBoot {
  lazy val sparkConf = new SparkConf()
    .setAppName("Learn Spark")
    .setMaster("local[*]")
    .set("spark.cores.max", "2")

  val spark = SparkSession.builder
    .config(sparkConf)
    .getOrCreate()
}
