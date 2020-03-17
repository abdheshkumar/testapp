import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}

object IngestionApp extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  val rsvpStream = RsvpStream(system, materializer)

  val (upgradeResponse, promise) = rsvpStream.start()
  val connected: Future[Done.type] = upgradeResponse.flatMap { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      println("Connection Established..")
      Future.successful(Done)
    } else {
      Future.failed(
        new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      )
    }
  }
  val dbSession = DBSession.forConfig("slick-postgres", system.settings.config)
  val tableIngestion = new IngestionKafkaToPostgres()
  val meetUpByEventTableStreamOutput =
    tableIngestion.meetUpByEventTableStream("trendingEvents", dbSession)
  val trendingTableStreamOutput =
    tableIngestion.trendingTableStream("trendingTopics", dbSession)
  val cs: CoordinatedShutdown = CoordinatedShutdown(system)
  cs.addTask(
    CoordinatedShutdown.PhaseServiceStop,
    "shut-down-client-http-pool"
  )(() => {
    dbSession.close()
    Http()(system)
      .shutdownAllConnectionPools()
      .map(_ => {
        println("Stopping......")
        promise.success(None) //at some later time we want to disconnect
        Done
      })
  })
  meetUpByEventTableStreamOutput.onComplete(
    f => println(s"Completed meetup_by_event stream: ${f}")
  )
  trendingTableStreamOutput.onComplete(
    f => println(s"Completed trending stream: ${f}")
  )
  connected.onComplete {
    case Failure(exception) =>
      exception.printStackTrace()
      sys.error(exception.getMessage)
    case Success(_) =>
      println("Started websocket client....")
  }
}
