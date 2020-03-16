package ing.etl

import com.github.mrpowers.spark.fast.tests.DatasetComparer
import ing.etl.Models.{Group, GroupTopics, Rsvp}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.ListMap

class StreamProcessorSpec
    extends FlatSpec
    with InMemoryQueryProcessor
    with SparkSessionTestWrapper
    with DatasetComparer
    with Matchers {

  import StreamProcessorSpec._
  import spark.implicits._
  case class GP(u: String, t: String)
  case class Test(c: Int, value: String, groups: Seq[GP])
  "this" should "work" in {
    import org.apache.spark.sql.functions._
    val d = spark.createDataFrame(
      List(
        Test(1, "A", Seq(GP("u1", "t1"), GP("u2", "t2"))),
        Test(1, "D", Seq(GP("u1", "t1"), GP("u2", "t2"))),
        Test(2, "B", Seq(GP("u3", "t3"), GP("u4", "t4"))),
        Test(3, "C", Seq.empty),
        Test(2, "E", Seq(GP("u3", "t3"), GP("u4", "t4")))
      )
    )
    val s =
      d.select($"c", explode($"groups").as("groups")).select("c", "groups.*")
    s.show()
    s.groupBy("c","u").agg(count("u"),first("c")).show()
  }
  "StreamProcessor" should "trending topics of the countries" in {
    val streamProcessor = StreamProcessor.create(spark)

    val data = List(rsvp, rsvp1, rsvp2).toDF()
    val result = streamProcessor.trendingTopicsByCountry(data)
    result.show()
    val excepted =
      Seq(("us", "Classic Books", 3L), ("us", "Toxic Relationships", 3L))
        .toDF("country", "topic_name", "count")

    //assertSmallDatasetEquality(result, excepted)
  }
/*
  it should "aggregate topics by country" in {
    val streamProcessor = StreamProcessor.create(spark)

    val testData = List(
      Group(
        group_id = "group-01",
        group_urlname = "",
        group_name = "Group-01",
        group_city = "any city",
        group_lat = 0,
        group_lon = 0,
        group_country = "us",
        group_topics = Seq(
          GroupTopics("classic-books", "Classic Books"),
          GroupTopics("classic-books", "Toxic Relationships")
        )
      ),
      Group(
        group_id = "group-02",
        group_urlname = "",
        group_name = "Group-02",
        group_city = "any city",
        group_lat = 0,
        group_lon = 0,
        group_country = "us",
        group_topics = Seq(
          GroupTopics("classic-books", "Classic Books"),
          GroupTopics("classic-books", "Toxic Relationships")
        )
      ),
      Group(
        group_id = "group-02",
        group_urlname = "",
        group_name = "Group-03",
        group_city = "any city",
        group_lat = 0,
        group_lon = 0,
        group_country = "us",
        group_topics = Seq(
          GroupTopics("nightlife", "Nightlife"),
          GroupTopics("diningout", "Dining Out")
        )
      ),
      Group(
        group_id = "group-03",
        group_urlname = "",
        group_name = "Help coronavirus affected people",
        group_city = "London",
        group_lat = 0,
        group_lon = 0,
        group_country = "uk",
        group_topics = Seq(GroupTopics("covid", "Covid19"))
      )
    )
    val testDF = testData.toDF().as("group")

    val expected =
      convertTopicByCountry(testData).toDF(trendingTopicColumns: _*)

    val result = streamProcessor.trendingTopicsByCountry(testDF)

    assertSmallDatasetEquality(result, expected)
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

  it should "aggregate same events" in {
    val streamProcessor = StreamProcessor.create(spark)

    val data = spark.read.json("etl/src/test/resources/data6.json")

    val result =
      streamProcessor.findMostPopularLocationsInTheWorldByEventId(data)
    result.count() shouldBe 5
  }*/

}

object StreamProcessorSpec {
  import ing.etl.Models._

  def convertTopicByCountry(data: List[Group]) = {
    data
      .flatMap(g => g.group_topics.map(gt => (g.group_country, gt.topic_name)))
      .foldLeft(ListMap.empty[(String, String), Long]) {
        case (acc, (country, topicName)) =>
          val key = (country, topicName)
          acc.find(_._1 == key) match {
            case Some(value) =>
              acc + (key -> (value._2 + 1))
            case None => acc + (key -> 1)
          }
      }
      .map { case ((country, topicName), count) => (country, topicName, count) }
      .toList
  }
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
      group_id = "group-01",
      group_urlname = "",
      group_name = "Group-01",
      group_city = "any city",
      group_lat = 0,
      group_lon = 0,
      group_country = "us",
      group_topics = Seq(
        GroupTopics("classic-books", "Classic Books"),
        GroupTopics("toxic-relationships", "Toxic Relationships")
      )
    )
  )

  val rsvp1 = Rsvp(
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
      group_id = "group-02",
      group_urlname = "",
      group_name = "Group-03",
      group_city = "any city",
      group_lat = 0,
      group_lon = 0,
      group_country = "us",
      group_topics = Seq(
        GroupTopics("nightlife", "Nightlife"),
        GroupTopics("diningout", "Dining Out")
      )
    )
  )
  val rsvp2 = Rsvp(
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
      group_id = "group-01",
      group_urlname = "",
      group_name = "Group-01",
      group_city = "any city",
      group_lat = 0,
      group_lon = 0,
      group_country = "us",
      group_topics = Seq(
        GroupTopics("classic-books", "Classic Books"),
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
