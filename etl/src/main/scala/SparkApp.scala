import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.StreamingQuery

object MainApp extends StreamProcessor with SparkBoot {

  def main(args: Array[String]): Unit = {}

  val stream: DataFrame = kafkaStream

  val trendingStream: StreamingQuery =
    writeIntoDB(stream, "meetup.meetup_by_event_id")(
      findMostPopularLocationsInTheWorldByEventId
    )

  val writeTrendingTopicsStream: StreamingQuery =
    writeIntoDB(stream, "meetup.trending")(trendingTopicsByCountry)

  spark.streams.awaitAnyTermination()

}
