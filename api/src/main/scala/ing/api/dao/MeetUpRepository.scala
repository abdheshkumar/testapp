package ing.api.dao

import ing.api.dao.TableSchema.{MeetUp, TrendingTopic}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class MeetUpRepository(val profile: JdbcProfile, meetUpDao: MeetUpDao)
    extends TableSchema
    with DBProfile {
  def getTrendingTopicsByCountry(country: String,
                                 limit: Int): Future[Seq[TrendingTopic]] =
    meetUpDao.getTrendingTopicsByCountry(country, limit)
  def getMostPopularMeetUpInTheWorld(limit: Int): Future[Seq[MeetUp]] =
    meetUpDao.getMostPopularMeetUpInTheWorld(limit)
}

object MeetUpRepository {
  def apply(profile: JdbcProfile, meetUpDao: MeetUpDao): MeetUpRepository =
    new MeetUpRepository(profile, meetUpDao)
}
