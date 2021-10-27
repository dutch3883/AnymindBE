package net.panuwach.tasks.models.internal

import net.panuwach.tasks.models.common.enums.{RecordType, RecordTypes}
import org.joda.time.DateTime

case class RecordInternal(recordType: RecordType, datetime: DateTime, amount: Double) {
  def apply(initialBalance: Double): Double = {
    recordType match {
      case RecordTypes.Deposit  => initialBalance + amount
      case RecordTypes.Withdraw => initialBalance - amount
      case _                    => initialBalance
    }
  }
}
