package net.panuwach.tasks.dataaccess.db.helper

import java.util.UUID

import doobie.{Read, Write}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DBParsingHelper {
  val mssqlDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
  implicit val datetimeRead: Read[DateTime] = Read[String].map(datetime => DateTime.parse(datetime, mssqlDateTimeFormat))
  implicit val datetimeWrite: Write[DateTime] = Write[String].contramap(datetime => datetime.toString(mssqlDateTimeFormat))
  implicit val uuidRead: Read[UUID] = Read[String].map(UUID.fromString)
  implicit val uuidWrite: Write[UUID] = Write[String].contramap(_.toString)
}
