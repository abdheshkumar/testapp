package ing.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import ing.api.dao.{MeetUpRepository, PostgresMeetUpDao}
import akka.http.scaladsl.server.Route
import breeze.numerics.constants.Database
import slick.jdbc.{JdbcBackend, PostgresProfile}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object HttpWebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()

    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val db: JdbcBackend.Database =
      slick.jdbc.JdbcBackend.Database.forConfig("db", system.settings.config)
    val repository =
      MeetUpRepository(PostgresProfile, PostgresMeetUpDao(PostgresProfile, db))
    val routes: Route = MeetUpRoutes(system).routes(repository)
    val bindingFuture =
      Http().bindAndHandle(routes, "0.0.0.0", 8085)

    bindingFuture.onComplete {
      _ match {
        case Failure(exception) =>
          println(
            s"Server failed binding at http://0.0.0.0:8085 ${exception.getMessage}"
          )
        case Success(value) =>
          println(s"Server online at http://0.0.0.0:8085")
      }
    }

    system.registerOnTermination(() => {

      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => println("Stopping an application..")) // trigger unbinding from the port

    })

  }
}
