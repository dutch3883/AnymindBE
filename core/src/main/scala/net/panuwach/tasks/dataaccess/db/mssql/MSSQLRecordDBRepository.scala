package net.panuwach.tasks.dataaccess.db.mssql

import java.util.UUID

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.scalalogging.LazyLogging
import doobie.hikari.HikariTransactor
import doobie.implicits._
import net.panuwach.tasks.dataaccess.db.model.RecordDB
import net.panuwach.tasks.dataaccess.db.{DBToInternalMapper, RecordDBRepository}
import net.panuwach.tasks.models.internal.RecordInternal
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
class MSSQLRecordDBRepository(implicit ec: ExecutionContext) extends RecordDBRepository with LazyLogging {

  import net.panuwach.tasks.dataaccess.db.helper.DBParsingHelper._

  var transactor: HikariTransactor[IO] = HikariTransactor
    .newHikariTransactor[IO](
      "com.microsoft.sqlserver.jdbc.SQLServerDriver",
      "jdbc:sqlserver://localhost:1433;database=APPDB",
      "sa",
      "P@ssw0rd",
      ec
    ).allocated.unsafeRunSync()._1

  def saveRecord(record: RecordInternal): Future[Int] = {
    val uuid                           = UUID.randomUUID()
    val (recordType, datetime, amount) = RecordInternal.unapply(record).get
    val recordTypeNo                   = recordType.i
    val transactionResult = for {
      count <-
        sql"insert into records (record_id, datetime, amount, record_type) values ($uuid, $datetime, $amount, $recordTypeNo);".update.run
          .transact(transactor)
    } yield count

    transactionResult.unsafeToFuture()
  }

  def searchRecordInRange(
      startDateTime: DateTime,
      endDateTime: DateTime
  ): Future[Seq[RecordInternal]] = {
    val transactionResult = for {
      record <-
        sql"select record_id,record_type,datetime,amount from APPDB.dbo.records where datetime <= $endDateTime and datetime >= $startDateTime order by datetime ASC"
          .query[RecordDB]
          .to[List]
          .transact(transactor)
    } yield record.map(DBToInternalMapper.mapRecord)

    transactionResult.unsafeToFuture()
  }

  def getAllRecords(): Future[Seq[RecordInternal]] = {
    val transactionResult = for {
      record <-
        sql"select record_id,record_type,datetime,amount from APPDB.dbo.records order by datetime ASC"
          .query[RecordDB]
          .to[List]
          .transact(transactor)
    } yield record.map(DBToInternalMapper.mapRecord)

    transactionResult.unsafeToFuture()
  }
}