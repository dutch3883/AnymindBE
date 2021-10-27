package net.panuwach.tasks.rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import net.panuwach.tasks.facades.{AddRecordFacade, ViewHistoryFacade}
import net.panuwach.tasks.models.common.Record
import net.panuwach.tasks.models.request.ViewHistoryRequest
import net.panuwach.tasks.models.response.BaseResponse

import scala.concurrent.ExecutionContext
class ServiceRoutes(addRecordFacade: AddRecordFacade, viewHistoryFacade: ViewHistoryFacade)(implicit
    ec: ExecutionContext
) extends FailFastCirceSupport {

  import net.panuwach.tasks.serialization.Decoder._
  import net.panuwach.tasks.serialization.Encoder._
  val routes: Route =
    pathPrefix("api") {
      concat(
        path("save-record") {
          post {
            entity(as[Record]) { record =>
              onSuccess(addRecordFacade.addRecord(record)) {
                case Right(_)    => complete(BaseResponse(true, None))
                case Left(error) => complete(BaseResponse(false, Some(error.message)))
              }
            }
          }
        },
        path("history") {
          post {
            entity(as[ViewHistoryRequest]) { request =>
              onSuccess(viewHistoryFacade.viewStatementHistory(request)) {
                case Right(value)    => complete(value)
                case Left(error) => complete(BaseResponse(false, Some(error.message)))
              }
            }
          }
        }
      )
    }
}
