package net.panuwach.tasks.facades

import net.panuwach.tasks.models.common.Statement
import net.panuwach.tasks.models.error.ApplicationError
import net.panuwach.tasks.models.request.ViewHistoryRequest
import net.panuwach.tasks.services.StatementHistoryService
import cats.implicits._
import scala.concurrent.{ExecutionContext, Future}

class ViewHistoryFacade(service: StatementHistoryService)(implicit ec: ExecutionContext) {
  def viewStatementHistory(request: ViewHistoryRequest): Future[Either[ApplicationError, List[Statement]]] = {
    validate(request) match {
      case Right(value) =>
        service
          .searchHourlyStatementHistory(value)
          .map(history => Right(history))
          .recoverWith({
            case e => Future.successful(Either.left[ApplicationError, List[Statement]](ApplicationError(e.getMessage)))
          })

      case Left(e) => Future.successful(Left(e))
    }

  }

  def validate(request: ViewHistoryRequest): Either[ApplicationError, ViewHistoryRequest] = {
    if (request.startDatetime.isBefore(request.endDatetime) || request.startDatetime.isEqual(request.endDatetime)) {
      Right(request)
    } else {
      Left(ApplicationError("Invalid request startDateTime should be before endDateTime"))
    }
  }

}
