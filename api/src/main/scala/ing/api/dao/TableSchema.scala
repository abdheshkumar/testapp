package ing.api.dao

import ing.api.dao.TableSchema.{MeetUp, TrendingTopic}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
trait DBProfile {
  val profile: JdbcProfile

}
trait TableSchema { self: DBProfile =>
  import profile.api._

  private[dao] class TrendingTopicTable(tag: Tag)
      extends Table[TrendingTopic](tag, Some("meetup"), "trending") {
    def country: Rep[String] =
      column[String]("country")
    def topics: Rep[String] = column[String]("topics")
    def count: Rep[Int] = column[Int]("count")

    def * : ProvenShape[TrendingTopic] =
      (country, topics, count).mapTo[TrendingTopic]
  }

  private[dao] class MeetUpTable(tag: Tag)
      extends Table[MeetUp](tag, Some("meetup"), "meetup_by_event_id") {
    def eventId: Rep[String] = column[String]("event_id", O.PrimaryKey)
    def eventName: Rep[String] = column[String]("event_name")
    def groupId: Rep[String] = column[String]("group_id")
    def groupName: Rep[String] = column[String]("group_name")
    def groupCountry: Rep[String] = column[String]("group_country")
    def groupLon: Rep[Float] = column[Float]("group_lon")
    def groupLat: Rep[Float] = column[Float]("group_lat")
    def yes: Rep[Int] = column[Int]("yes")
    def no: Rep[Int] = column[Int]("no")

    def * : ProvenShape[MeetUp] =
      (
        eventId,
        eventName,
        groupId,
        groupName,
        groupCountry,
        groupLon,
        groupLat,
        yes,
        no
      ).mapTo[MeetUp]
  }

  val trendingTopicTable: TableQuery[TrendingTopicTable] =
    TableQuery[TrendingTopicTable]
  val meetUpByEventTable: TableQuery[MeetUpTable] = TableQuery[MeetUpTable]

}

object TableSchema {
  case class TrendingTopic(country: String, topics: String, count: Int)

  case class MeetUp(eventId: String,
                    eventName: String,
                    groupId: String,
                    groupName: String,
                    groupCountry: String,
                    groupLon: Float,
                    groupLat: Float,
                    yesResponse: Int,
                    noResponse: Int)

}
