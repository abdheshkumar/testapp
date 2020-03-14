package ing.api.dao

import scala.concurrent.Future

trait MeetUpDao { self: TableSchema =>
  def getTrendingTopicsByCountry(
    country: String,
    limit: Int
  ): Future[Seq[TableSchema.TrendingTopic]]
  def getMostPopularMeetUpInTheWorld(
    limit: Int
  ): Future[Seq[TableSchema.MeetUp]]
}
