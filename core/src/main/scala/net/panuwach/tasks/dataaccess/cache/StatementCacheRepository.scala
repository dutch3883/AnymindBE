package net.panuwach.tasks.dataaccess.cache

import net.panuwach.tasks.dataaccess.model.StatementCache
import net.panuwach.tasks.models.internal.RecordInternal
import org.joda.time.LocalDate

import scala.concurrent.{ExecutionContext, Future}

trait StatementCacheRepository {
  def updateWithRecord(record: RecordInternal)(implicit executionContext: ExecutionContext): Future[Unit]

  /**
    * This method was created to get the statement at the specific date
    */
  def getDateStatement(date: LocalDate)(implicit executionContext: ExecutionContext): Future[StatementCache]

}
