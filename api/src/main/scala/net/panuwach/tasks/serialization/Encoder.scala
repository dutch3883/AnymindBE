package net.panuwach.tasks.serialization

import io.circe.generic.semiauto._
import io.circe.{Encoder => CirceEncoder}
import net.panuwach.tasks.models.common.Statement
import net.panuwach.tasks.models.response.BaseResponse
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object Encoder {

  implicit val datetimeEncoder: CirceEncoder[DateTime] = CirceEncoder.encodeString.contramap { s =>
    s.toString(ISODateTimeFormat.dateTime())
  }
  implicit val statementEncoder: CirceEncoder[Statement]            = deriveEncoder[Statement]
  implicit val addRecordResponseEncoder: CirceEncoder[BaseResponse] = deriveEncoder[BaseResponse]
}