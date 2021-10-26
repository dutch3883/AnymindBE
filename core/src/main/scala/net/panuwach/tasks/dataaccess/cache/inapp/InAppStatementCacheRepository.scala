package net.panuwach.tasks.dataaccess.cache.inapp

import net.panuwach.tasks.dataaccess.cache.StatementCacheRepository
import net.panuwach.tasks.dataaccess.db.RecordDBRepository
import net.panuwach.tasks.dataaccess.model.StatementCache
import net.panuwach.tasks.models.error.ApplicationError
import net.panuwach.tasks.models.internal.RecordInternal
import net.panuwach.tasks.util.DateTimeHelper
import org.joda.time.LocalDate

import scala.collection.immutable.SortedMap
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

object InAppStatementCacheRepository {
  val FirstTransactionDate = LocalDate.parse("2019-10-05T14:48:01+01:00")
  val InitialAmount        = 1000
  val initializeTimeOut    = 5 seconds
}

class InAppStatementCacheRepository(recordDBRepository: RecordDBRepository)
    extends StatementCacheRepository
    with DateTimeHelper {
  import InAppStatementCacheRepository._

  var dateStatementMap: SortedMap[LocalDate, StatementCache] =
    SortedMap[LocalDate, StatementCache](FirstTransactionDate -> StatementCache(InitialAmount))

  override def updateWithRecord(
      record: RecordInternal
  )(implicit executionContext: ExecutionContext): Future[Unit] = {
    val stateToBeUpdate = dateStatementMap.collect {
      case (date, StatementCache(amount))
          if date.isEqual(record.datetime.toLocalDate) || date.isAfter(record.datetime.toLocalDate) =>
        date -> StatementCache(record.apply(amount))
    }
    dateStatementMap = dateStatementMap ++ stateToBeUpdate
    Future.successful(())
  }

  /**
    * This method was created to get the statement at the specific date
    */
  override def getDateStatement(
      date: LocalDate
  )(implicit executionContext: ExecutionContext): Future[StatementCache] = {
    dateStatementMap.get(date) match {
      case Some(state) => Future.successful(state)
      case None =>
        dateStatementMap.lastOption match {
          case Some(last) if last._1.isBefore(date) => Future.successful(last._2)
          case _                                    => Future.successful(StatementCache(InitialAmount))
        }
    }
  }

  /**
    * This method will load all the transaction from DB and calculate statement in daily value to be access for search
    * feature
    */

  def initializeCache()(implicit executionContext: ExecutionContext): Unit = {

    val mapResult = recordDBRepository.getAllRecords().map {
      case allRecords @ _ :+ last => {
        val dateBreakdown =
          buildDailyBreakdown(FirstTransactionDate, last.datetime.toLocalDate).map(_ -> StatementCache(1000))
        val initialFold = FoldingState(
          dateCache = SortedMap(dateBreakdown: _*),
          amount = 1000,
          lastDate = FirstTransactionDate
        )

        val finalState = allRecords.foldLeft(initialFold)((state, record) => {
          val recordDate = record.datetime.toLocalDate
          val updateFromLast = updateMap(
            state.dateCache,
            date => date.isBefore(recordDate) && date.isAfter(state.lastDate),
            StatementCache(state.amount)
          )
          val updateFromCurrent =
            updateMap(updateFromLast, date => date.isEqual(recordDate), StatementCache(state.amount))
          FoldingState(updateFromCurrent, record.apply(state.amount), recordDate)
        })
        finalState.dateCache
      }
      case _ => SortedMap(FirstTransactionDate -> StatementCache(1000))
    }

    dateStatementMap = Await.result(mapResult, initializeTimeOut)

  }

  def updateMap(
      dateCache: SortedMap[LocalDate, StatementCache],
      predicate: LocalDate => Boolean,
      updateValue: StatementCache
  ): SortedMap[LocalDate, StatementCache] = {
    dateCache ++ dateCache.collect { case (date, _) if predicate(date) => date -> updateValue }
  }

  case class FoldingState(dateCache: SortedMap[LocalDate, StatementCache], amount: Double, lastDate: LocalDate)
}
