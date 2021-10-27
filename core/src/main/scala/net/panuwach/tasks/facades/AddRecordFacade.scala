package net.panuwach.tasks.facades

import net.panuwach.tasks.models.common.Record
import net.panuwach.tasks.models.error.ApplicationError
import net.panuwach.tasks.services.RecordService
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The scope of this class should only cover validation request and error handling with purpose of separation
  * of concern from Services to let them handler only business logic and let ServiceRoute handle only routing and serialization.
  */
class AddRecordFacade(recordService: RecordService)(implicit ec: ExecutionContext) {

  def addRecord(record: Record): Future[Either[ApplicationError, Int]] = {
    recordService.addRecord(record).map(a => Right(a)) recoverWith {
      case e => Future.successful(Either.left[ApplicationError, Int](ApplicationError(e.getMessage)))
    }
  }
}
