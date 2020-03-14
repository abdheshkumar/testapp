import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Encoder, Encoders}

object Models {
  case class Rsvp(venue: Venue,
                  response: String,
                  member: Member,
                  mtime: Long,
                  event: Event,
                  group: Group)

  object Rsvp {

    implicit val rsvpEncoder: Encoder[Rsvp] = Encoders.product[Rsvp]
    val schema: StructType = rsvpEncoder.schema
  }

  case class Venue(venue_name: String,
                   venue_id: String,
                   lon: Option[Float],
                   lat: Option[Float])

  case class Event(event_id: String,
                   event_name: String,
                   event_url: String,
                   time: Long)
  case class GroupTopics(urlkey: String, topic_name: String)
  case class Group(group_id: String,
                   group_urlname: String,
                   group_name: String,
                   group_city: String,
                   group_lat: Float,
                   group_lon: Float,
                   group_country: String,
                   group_topics: Seq[GroupTopics])

  case class Member(member_name: String, member_id: String, photo: String)

}
