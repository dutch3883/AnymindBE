package net.panuwach.tasks.dataaccess.db

import net.panuwach.tasks.models.internal.RecordInternal
import org.joda.time.DateTime

import scala.concurrent.Future

trait RecordDBRepository {
  def saveRecord(record: RecordInternal): Future[Int]

  def searchRecordInRange(stateDateTime: DateTime, endDateTime: DateTime): Future[Seq[RecordInternal]]

  def getAllRecords(): Future[Seq[RecordInternal]]
}
