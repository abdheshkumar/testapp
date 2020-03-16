package ing.etl

import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.streaming.{
  DataStreamWriter,
  OutputMode,
  StreamingQuery
}

trait KafkaStreamWriter extends WriteIntoExternalSource {
  def topic: String
  def writeDataIntoExternalSource(
    df: DataFrame
  )(transformFunc: DataFrame => DataFrame): StreamingQuery = {
    df.transform(transformFunc)
      .selectExpr("to_json(struct(*)) AS value")
      .writeStream
      .format("kafka")
      .option("topic", topic)
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("checkpointLocation", s"ing_meetup/spark/checkpoints/$topic")
      .outputMode(OutputMode.Update())
      .start()
  }
}

object KafkaStreamWriter {
  def apply(topicName: String): KafkaStreamWriter = new KafkaStreamWriter {
    override def topic: String = topicName
  }

}
