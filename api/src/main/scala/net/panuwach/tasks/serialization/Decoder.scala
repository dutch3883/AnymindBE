package net.panuwach.tasks.serialization

import io.circe.generic.semiauto._
import io.circe.{Decoder => CirceDecoder}
import net.panuwach.tasks.models.common.Record
import net.panuwach.tasks.models.request.ViewHistoryRequest
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.util.control.NonFatal

object Decoder {

  val dateFormatter = ISODateTimeFormat.dateTimeParser()
  implicit val decodeDateTime: CirceDecoder[DateTime] = CirceDecoder.decodeString.emap { s =>
    try {
      Right(DateTime.parse(s, dateFormatter))
    } catch {
      case NonFatal(e) => Left(e.getMessage)
    }
  }
  implicit val recordDecoder: CirceDecoder[Record] = deriveDecoder[Record]
  implicit val RetrieveHistoryRequestEncoder: CirceDecoder[ViewHistoryRequest] =
    deriveDecoder[ViewHistoryRequest]
}
