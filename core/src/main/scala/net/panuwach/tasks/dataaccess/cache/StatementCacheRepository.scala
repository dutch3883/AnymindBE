package net.panuwach.tasks.dataaccess.cache

import net.panuwach.tasks.dataaccess.db.model.StatementCache
import net.panuwach.tasks.models.internal.RecordInternal
import org.joda.time.LocalDate

import scala.concurrent.Future

trait StatementCacheRepository {
  def updateWithRecord(record: RecordInternal): Future[Unit]

  /**
    * This method was created to get the statement at the specific date
    */
  def getDateStatement(date: LocalDate): Future[StatementCache]

}
