package net.panuwach.tasks.models.common.enums

import enumeratum.{Enum, EnumEntry}

sealed abstract class RecordType(val i: Int) extends EnumEntry {
  override def equals(obj:Any): Boolean = obj match {
    case r: RecordType => r.i == i
    case _ => false
  }
}

object RecordTypes extends Enum[RecordType] {
  override def values: IndexedSeq[RecordType] = findValues.toIndexedSeq
  case object Unknown extends RecordType(0)
  case object Deposit extends RecordType(1)
  case object Withdraw extends RecordType(2)

}
