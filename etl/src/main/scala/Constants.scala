object Constants {
  val YES = "yes"
  val NO = "no"
  val postgresProperties: collection.Map[String, String] = scala.collection.Map(
    "url" -> "jdbc:postgresql://localhost/postgres",
    "dbtable" -> "meetup.meetup_by_event_id",
    "driver" -> "org.postgresql.Driver",
    "user" -> "postgres",
    "password" -> "postgres"
  )
}
