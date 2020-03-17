package ing.api.dao

import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class PostgresMeetUpDao(val profile: JdbcProfile,
                        db: JdbcProfile#Backend#Database)
    extends MeetUpDao
    with TableSchema
    with DBProfile {

  import profile.api._

  override def getTrendingTopicsByCountry(
    country: String,
    limit: Int
  ): Future[Seq[TableSchema.TrendingTopic]] = {
    db.run(
      trendingTopicTable
        .filter(_.country === country)
        .sortBy(_.count.desc)
        .take(limit)
        .result
    )
  }

  override def getMostPopularMeetUpInTheWorld(
    limit: Int
  ): Future[Seq[TableSchema.MeetUp]] = {
    db.run(meetUpByEventTable.sortBy(_.yes.desc).take(limit).result)
  }
}

object PostgresMeetUpDao {
  def apply(profile: JdbcProfile, db: JdbcProfile#Backend#Database) =
    new PostgresMeetUpDao(profile, db)
}
