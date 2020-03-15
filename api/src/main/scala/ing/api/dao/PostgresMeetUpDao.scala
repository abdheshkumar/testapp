package ing.api.dao

import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.Future

class PostgresMeetUpDao(val profile: JdbcProfile,
                        db: JdbcProfile#Backend#Database)
    extends MeetUpDao
    with TableSchema
    with DBProfile {

  import profile.api._

  implicit val getTrendingTopicResult =
    GetResult(r => TableSchema.TrendingTopic(r.<<, r.<<, r.<<))

  implicit val getMeetUpResult =
    GetResult(
      r =>
        TableSchema.MeetUp(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<)
    )

  override def getTrendingTopicsByCountry(
    country: String,
    limit: Int
  ): Future[Seq[TableSchema.TrendingTopic]] = {
    val query =
      sql"""
        SELECT country,topic_name,count
        FROM (
                 SELECT country,topic_name,count
                      , row_number() OVER(PARTITION BY country, topic_name ORDER BY count DESC) AS rn
                 FROM   meetup.trending where country='#$country'
             ) sub
        WHERE  rn = 1 order by count desc limit #$limit;
        """.as[TableSchema.TrendingTopic]

    db.run(query)
  }

  override def getMostPopularMeetUpInTheWorld(
    limit: Int
  ): Future[Seq[TableSchema.MeetUp]] = {

    val query =
      sql"""
        select  event_id,event_name,group_id, group_name,group_country, group_lon,group_lat,yes,no
        from ( SELECT event_id,event_name,group_id, group_name,group_country, group_lon,group_lat,yes,no,
                      row_number() OVER(PARTITION BY group_country ORDER BY yes DESC) AS rn1
               FROM   (
                          SELECT event_id,event_name,group_id, group_name,group_country, group_lon,group_lat,yes,no
                               , row_number() OVER(PARTITION BY event_id,group_country ORDER BY yes DESC) AS rn
                          FROM   meetup.meetup_by_event_id
                      ) sub
               WHERE  rn = 1
               order by yes desc) sub1
        where rn1 = 1
        limit #$limit
        """.as[TableSchema.MeetUp]
    db.run(query)
  }
}

object PostgresMeetUpDao {
  def apply(profile: JdbcProfile, db: JdbcProfile#Backend#Database) =
    new PostgresMeetUpDao(profile, db)
}
