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

  val cs: CoordinatedShutdown = CoordinatedShutdown(system)
  cs.addTask(
    CoordinatedShutdown.PhaseServiceStop,
    "shut-down-client-http-pool"
  )(() => {
    Http()(system)
      .shutdownAllConnectionPools()
      .map(_ => {
        println("Stopping......")
        promise.success(None) //at some later time we want to disconnect
        Done
      })
  })
  connected.onComplete {
    case Failure(exception) =>
      exception.printStackTrace()
      sys.error(exception.getMessage)
    case Success(_) =>
      println("Started websocket client....")
  }
}
