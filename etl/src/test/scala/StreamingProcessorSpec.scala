/*
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import ing.etl.InMemoryQueryProcessor
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import ing.etl.Models.{Event, Group, GroupTopics, Member, Rsvp, Venue}
class StreamingProcessorSpec
    extends FunSuite
    with InMemoryQueryProcessor
    with DataFrameSuiteBase
    with BeforeAndAfterAll {
  // re-use the spark context
  override implicit def reuseContextIfPossible: Boolean = true
  //override def afterAll() = spark.stop()
  /* test("simple test") {
    val sqlCtx = sqlContext
    import sqlCtx.implicits._

    val input1 = sc.parallelize(List(1, 2, 3)).toDF
    assertDataFrameEquals(input1, input1) // equal

    val input2 = sc.parallelize(List(4, 5, 6)).toDF
    intercept[org.scalatest.exceptions.TestFailedException] {
      assertDataFrameEquals(input1, input2) // not equal
    }
  }
   */
  /*  test("use of me memory stream") {
    import org.apache.spark.sql.execution.streaming.MemoryStream
    import org.apache.spark.sql.SparkSession
    val spark: SparkSession = SparkSession.builder.getOrCreate()
    val ctx = sqlContext
    import ctx.implicits._
    // It uses two implicits: Encoder[Int] and SQLContext
    val intsInput = MemoryStream[Venue]
    val memoryQuery =
      intsInput.toDF.writeStream
        .format("memory")
        .queryName("memStream")
        .start
    val zeroOffset = intsInput.addData(
      Venue(
        "Imbibe Bottle House & Taproom",
        "26174576",
        Some(-122.04533F),
        Some(47.39164F)
      )
    )
    memoryQuery.processAllAvailable()
    val intsOut = spark.table("memStream").as[Venue]
    intsOut.show
    memoryQuery.stop()
  }*/

  test("It should write into h2 database") {
    import spark.implicits._
    val intsInput = MemoryStream[Rsvp]
    val processBuilder = processor(intsInput)
    val queries = processBuilder.startAll
    val memoryQuery =
      intsInput.toDF.writeStream
        .format("memory")
        .queryName("rsvp")
        .start
    val rsvp = Rsvp(
      venue = Venue(
        "Imbibe Bottle House & Taproom",
        "26174576",
        Some(-122.04533F),
        Some(47.39164F)
      ),
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
        47.4F,
        -122.03F,
        "us",
        Seq(GroupTopics("classics", "Classic Books"))
      )
    )
    val zeroOffset = intsInput.addData(rsvp)

    memoryQuery.processAllAvailable()
    println("**********Start MEETUP_BY_EVENT_ID************")
    val t = spark.read
      .format("jdbc")
      .options(
        processBuilder.jdbcOption + ("dbtable" -> "MEETUP.MEETUP_BY_EVENT_ID")
      )
      .load()

      //.show()
    println(t.select("event_name").as[String].take(4).toList)
    println("**********End MEETUP_BY_EVENT_ID************")
    //intsOut.show
    //queries.map(_.stop())
    memoryQuery.stop()

  }
}
*/
