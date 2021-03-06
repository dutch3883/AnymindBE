package net.panuwach.tasks.dataaccess.db.model

import java.util.UUID

import org.joda.time.DateTime

case class RecordDB(recordId: UUID, recordType: Int, datetime: DateTime, amount: Double)
