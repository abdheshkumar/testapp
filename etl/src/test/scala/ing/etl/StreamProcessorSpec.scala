package ing.etl

import com.github.mrpowers.spark.fast.tests.DatasetComparer
import ing.etl.Models.Rsvp
import org.scalatest.FlatSpec

class StreamProcessorSpec
    extends FlatSpec
    with InMemoryQueryProcessor
    with SparkSessionTestWrapper
    with DatasetComparer {

  import StreamProcessorSpec._
  import spark.implicits._

  "StreamProcessor" should "trending topics of the countries" in {
    val streamProcessor = StreamProcessor.create(spark)

    val data = List(rsvp, rsvp, rsvp).toDF()
    val result = streamProcessor.trendingTopicsByCountry(data)
    val excepted =
      Seq(("us", "Classic Books", 3L), ("us", "Toxic Relationships", 3L))
        .toDF("country", "topic_name", "count")

    assertSmallDatasetEquality(result, excepted)
  }

  it should "find most popular events" in {
    val streamProcessor = StreamProcessor.create(spark)

    val data = List(rsvp, rsvp, rsvp).toDF()
    val result =
      streamProcessor.findMostPopularLocationsInTheWorldByEventId(data)

    val excepted = spark.sqlContext
      .createDataFrame(
        List(
          Test(
            "rqccmrybcfbqb",
            "Maple Valley",
            "us",
            "27031693",
            "Maple Valley Books and Beers",
            -122.03,
            47.4,
            "March Monthly Meeting",
            0,
            3
          )
        )
      )
      .setNullableStateOfColumn(
        ("group_lon", true),
        ("group_lat", true),
        ("yes", true),
        ("no", true)
      )
    assertSmallDatasetEquality(result, excepted)
  }

  "transformRsvpResponse" should "transfer response yes/no to 1/0" in {
    val streamProcessor = StreamProcessor.create(spark)
    val data = List("yes", "yes", "no").toDF("response")
    val result = streamProcessor.transformRsvpResponse(data)
    val excepted = Seq((1, 0), (1, 0), (0, 1)).toDF("yes", "no")
    excepted.printSchema()
    assertSmallDatasetEquality(result, excepted)
  }

}

object StreamProcessorSpec {
  import ing.etl.Models._
  val trendingTopicColumns = Seq("country", "topic_name", "count")
  val meetUpEventByColumns = Seq(
    "event_id",
    "group_city",
    "group_country",
    "group_id",
    "group_name",
    "group_lon",
    "group_lat",
    "event_name",
    "yes",
    "no"
  )
  val rsvp = Rsvp(
    venue =
      Venue("Imbibe Bottle House & Taproom", "26174576", -122.04533, 47.39164),
    response = "no",
    member = Member(
      "Jenny",
      "7717630",
      "https://secure.meetupstatic.com/photos/member/d/b/8/a/thumb_289556202.jpeg"
    ),
    mtime = 1829314800,
    event = Event(
      "rqccmrybcfbqb",
      "March Monthly Meeting",
      "https://www.meetup.com/Maple-Valley-Books-and-Beers/events/267847717/",
      1584064800000L
    ),
    group = Group(
      "27031693",
      "Maple-Valley-Books-and-Beers",
      "Maple Valley Books and Beers",
      "Maple Valley",
      47.4,
      -122.03,
      "us",
      Seq(
        GroupTopics("classics", "Classic Books"),
        GroupTopics("toxic-relationships", "Toxic Relationships")
      )
    )
  )

}

case class Test(event_id: String,
                group_city: String,
                group_country: String,
                group_id: String,
                group_name: String,
                group_lon: Double,
                group_lat: Double,
                event_name: String,
                yes: Long,
                no: Long)
