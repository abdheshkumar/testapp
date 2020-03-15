import ing.etl.{
  Constants,
  KafkaStreamReader,
  LiveSparkBoot,
  QueryProcessor,
  StreamProcessor
}
import org.apache.spark.sql.DataFrame

object MainApp extends {

  def main(args: Array[String]): Unit = {
    val processor = new QueryProcessor with KafkaStreamReader
    with StreamProcessor with LiveSparkBoot {
      val inputStream: DataFrame = readFromKafkaStream

      override val jdbcOption: collection.Map[String, String] =
        Constants.postgresProperties
    }
    processor.startAll
    processor.spark.streams.awaitAnyTermination()
  }

}
