package ing.etl

object Constants {
  val YES = "yes"
  val NO = "no"
  val meetupByEventIdTable = "meetup.meetup_by_event_id"
  val trendingTopicsTable = "meetup.trending"

  val postgresProperties: collection.Map[String, String] = scala.collection.Map(
    "url" -> "jdbc:postgresql://localhost/postgres",
    "dbtable" -> "meetup.meetup_by_event_id",
    "driver" -> "org.postgresql.Driver",
    "user" -> "postgres",
    "password" -> "postgres"
  )

  val h2Properties: collection.Map[String, String] = scala.collection.Map(
    "url" -> "jdbc:h2:mem:meetup;INIT=create schema if not exists meetup\\;runscript from 'classpath:create.sql'",
    "driver" -> "org.h2.Driver"
  )
}
