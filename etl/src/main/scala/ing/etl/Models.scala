package ing.etl

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Encoder, Encoders}

object Models {
  case class Rsvp(venue: Venue,
                  response: String,
                  member: Member,
                  mtime: Long,
                  event: Event,
                  group: Group)
  case class TrendingTopic(country: String, topicName: String, count: Int)

  object Rsvp {

    implicit val rsvpEncoder: Encoder[Rsvp] = Encoders.product[Rsvp]
    val schema: StructType = ScalaReflection.schemaFor[Rsvp].dataType.asInstanceOf[StructType]
  }

  case class Venue(venue_name: String,
                   venue_id: String,
                   lon: Double,
                   lat: Double)

  case class Event(event_id: String,
                   event_name: String,
                   event_url: String,
                   time: Long)
  case class GroupTopics(urlkey: String, topic_name: String)
  case class Group(group_id: String,
                   group_urlname: String,
                   group_name: String,
                   group_city: String,
                   group_lat: Double,
                   group_lon: Double,
                   group_country: String,
                   group_topics: Seq[GroupTopics])

  case class Member(member_name: String, member_id: String, photo: String)

}
