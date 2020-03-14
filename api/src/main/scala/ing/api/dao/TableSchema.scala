package ing.api.dao

import ing.api.dao.MeetUpDAO.TrendingTopic
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

trait DBProfile {
  val profile: JdbcProfile

}
trait MeetUpDAO { self: DBProfile =>
  import profile.api._
  class Suppliers(tag: Tag) extends Table[TrendingTopic](tag, "trending") {
    def country: Rep[Int] =
      column[Int]("country")
    def topics: Rep[String] = column[String]("topics")
    def count: Rep[String] = column[String]("count")

    def * : ProvenShape[TrendingTopic] =
      (country, topics, count).mapTo[TrendingTopic]
  }
  val trendingTopicTable: TableQuery[TrendingTopic] = TableQuery[TrendingTopic]
}

object MeetUpDAO {
  case class TrendingTopic(country: String, topics: String, count: Int)
}
