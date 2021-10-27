package net.panuwach.tasks.dataaccess.db.mssql

import java.util.UUID

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.implicits._
import net.panuwach.tasks.dataaccess.db.{DBToInternalMapper, RecordDBRepository}
import net.panuwach.tasks.dataaccess.db.model.RecordDB
import net.panuwach.tasks.models.internal.RecordInternal
import org.joda.time.DateTime

import scala.concurrent.Future
class MSSQLRecordDBRepository extends RecordDBRepository{
  import net.panuwach.tasks.dataaccess.db.helper.DBParsingHelper._

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](1)
      xa <- HikariTransactor.newHikariTransactor[IO](
              "com.microsoft.sqlserver.jdbc.SQLServerDriver",
              "jdbc:sqlserver://localhost:1433;database=APPDB",
              "sa",
              "P@ssw0rd",
              ce
            )
    } yield xa

  def saveRecord(record: RecordInternal): Future[Int] = {
    val uuid                           = UUID.randomUUID()
    val (recordType, datetime, amount) = RecordInternal.unapply(record).get
    val recordTypeNo                   = recordType.i
    val transactionResult = transactor.use { xa =>
      for {
        count <-
          sql"insert into records (record_id, datetime, amount, record_type) values ($uuid, $datetime, $amount, $recordTypeNo);".update.run
            .transact(xa)
      } yield count
    }

    transactionResult.unsafeToFuture()

  }

  def searchRecordInRange(
      startDateTime: DateTime,
      endDateTime: DateTime
  ): Future[Seq[RecordInternal]] = {
    val transactionResult = transactor.use { xa =>
      for {
        record <-
          sql"select record_id,record_type,datetime,amount from APPDB.dbo.records where datetime <= $endDateTime and datetime >= $startDateTime"
            .query[RecordDB]
            .to[List]
            .transact(xa)
      } yield record.map(DBToInternalMapper.mapRecord)
    }

    transactionResult.unsafeToFuture()
  }

  def getAllRecords(): Future[Seq[RecordInternal]] = {
    val transactionResult = transactor.use { xa =>
      for {
        record <- sql"select record_id,record_type,datetime,amount from APPDB.dbo.records "
                    .query[RecordDB]
                    .to[List]
                    .transact(xa)
      } yield record.map(DBToInternalMapper.mapRecord)
    }

    transactionResult.unsafeToFuture()
  }
}
