package net.panuwach.tasks.dataaccess.cache.inapp

import com.typesafe.scalalogging.LazyLogging
import net.panuwach.tasks.dataaccess.cache.StatementCacheRepository
import net.panuwach.tasks.dataaccess.db.RecordDBRepository
import net.panuwach.tasks.dataaccess.db.model.StatementCache
import net.panuwach.tasks.models.internal.RecordInternal
import net.panuwach.tasks.util.DateTimeHelper
import org.joda.time.LocalDate

import scala.collection.immutable.SortedMap
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

object InAppStatementCacheRepository {
  val FirstTransactionDate = LocalDate.parse("2019-10-05")
  val InitialAmount        = 1000
  val initializeTimeOut    = 20 seconds
}

class InAppStatementCacheRepository(recordDBRepository: RecordDBRepository)(implicit ec: ExecutionContext)
    extends StatementCacheRepository
    with DateTimeHelper
    with LazyLogging {
  import InAppStatementCacheRepository._

  var dateStatementMap: SortedMap[LocalDate, StatementCache] = Await.result(initializeCache(), initializeTimeOut)

  override def updateWithRecord(
      record: RecordInternal
  ): Future[Unit] = {
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
  ): Future[StatementCache] = {
    dateStatementMap.get(date) match {
      case Some(state) => Future.successful(state)
      case None =>
        dateStatementMap.lastOption match {
          case Some(last) if date.isAfter(last._1) => Future.successful(last._2)
          case _ => Future.successful(StatementCache(InitialAmount))
        }
    }
  }

  /**
    * This method will load all the transaction from DB and calculate statement in daily value to be access for search
    * feature
    */

  def initializeCache(): Future[SortedMap[LocalDate, StatementCache]] = {
    logger.info("start initialize cache")
    recordDBRepository
      .getAllRecords()
      .map {
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
            val updateFromCurrent = updateMap(updateFromLast, date => date.isEqual(recordDate), StatementCache(record.apply(state.amount)))
            FoldingState(updateFromCurrent, record.apply(state.amount), recordDate)
          })
          finalState.dateCache
        }
        case _ => SortedMap(FirstTransactionDate -> StatementCache(1000))
      }
      .map(result => {
        logger.info("initialize cache complete")
        result
      })

  }

  private def updateMap(
      dateCache: SortedMap[LocalDate, StatementCache],
      predicate: LocalDate => Boolean,
      updateValue: StatementCache
  ): SortedMap[LocalDate, StatementCache] = {
    dateCache ++ dateCache.collect { case (date, _) if predicate(date) => date -> updateValue }
  }

  private case class FoldingState(dateCache: SortedMap[LocalDate, StatementCache], amount: Double, lastDate: LocalDate)
}
