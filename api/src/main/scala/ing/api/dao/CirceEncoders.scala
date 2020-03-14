package ing.api.dao
import ing.api.dao.TableSchema.{MeetUp, TrendingTopic}
import io.circe.Encoder
import io.circe.generic.semiauto._
object CirceEncoders {
  implicit lazy val meetUpEncoder: Encoder.AsObject[MeetUp] =
    deriveEncoder[MeetUp]
  implicit lazy val trendingTopicEncoder: Encoder.AsObject[TrendingTopic] =
    deriveEncoder[TrendingTopic]
}
