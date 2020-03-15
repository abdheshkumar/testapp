package ing.etl

import org.apache.spark.sql.functions.from_json

trait KafkaStreamReader { self: SparkBoot =>
  import self.spark.implicits._

  /**
    * Read streaming data from kafka and convert value into json format
    * @return
    */
  def readFromKafkaStream =
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "test")
      .option("startingOffsets", "earliest") // From starting
      .load()
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .select(from_json($"value", Models.Rsvp.schema).as("rsvp"))
      .select("rsvp.*")
}
