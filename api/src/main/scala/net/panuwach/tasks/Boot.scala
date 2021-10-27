package net.panuwach.tasks

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import net.panuwach.tasks.dataaccess.cache.inapp.InAppStatementCacheRepository
import net.panuwach.tasks.dataaccess.db.mssql.MSSQLRecordDBRepository
import net.panuwach.tasks.facades.{AddRecordFacade, ViewHistoryFacade}
import net.panuwach.tasks.rest.ServiceRoutes
import net.panuwach.tasks.services.{RecordService, StatementHistoryService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

object Boot extends App {

  def boot(): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val recordDBRepository      = new MSSQLRecordDBRepository
      val statementCache          = new InAppStatementCacheRepository(recordDBRepository)
      val recordService           = new RecordService(recordDBRepository, statementCache)
      val statementHistoryService = new StatementHistoryService(recordDBRepository, statementCache)
      val addRecordFacade         = new AddRecordFacade(recordService)
      val viewHistoryFacade       = new ViewHistoryFacade(statementHistoryService)
      val routes                  = new ServiceRoutes(addRecordFacade, viewHistoryFacade)
      startHttpServer(routes.routes)(context.system)
      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {

    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  boot()
}
