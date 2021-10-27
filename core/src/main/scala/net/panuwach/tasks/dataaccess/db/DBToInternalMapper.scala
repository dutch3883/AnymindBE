package net.panuwach.tasks.dataaccess.db

import net.panuwach.tasks.dataaccess.db.model.RecordDB
import net.panuwach.tasks.models.common.enums.RecordTypes
import net.panuwach.tasks.models.internal.RecordInternal

object DBToInternalMapper {

  def mapRecord(recordDB: RecordDB): RecordInternal = {
    RecordInternal(
      recordType = RecordTypes.valueMap(recordDB.recordType),
      datetime = recordDB.datetime,
      amount = recordDB.recordType
    )
  }
}
