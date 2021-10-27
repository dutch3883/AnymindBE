package net.panuwach.tasks.services

import net.panuwach.tasks.dataaccess.cache.StatementCacheRepository
import net.panuwach.tasks.dataaccess.db.RecordDBRepository
import net.panuwach.tasks.models.common.Record
import net.panuwach.tasks.models.common.enums.RecordTypes
import net.panuwach.tasks.models.internal.RecordInternal

import scala.concurrent.{ExecutionContext, Future}

/**
  * Responsible for business logic and model mapping
  * @param recordRepository
  * @param executionContext
  */
class RecordService(recordRepository: RecordDBRepository, statementCacheRepository: StatementCacheRepository)(implicit executionContext: ExecutionContext) {
  def addRecord(record: Record): Future[Int] = {
    val internalRecord = RecordInternal(recordType = RecordTypes.Deposit, record.datetime, record.amount)
    for {
        count <- recordRepository.saveRecord(internalRecord)
        _ <- statementCacheRepository.updateWithRecord(internalRecord)
    } yield count
  }
}
