package net.panuwach.tasks.services

import net.panuwach.tasks.dataaccess.cache.StatementCacheRepository
import net.panuwach.tasks.dataaccess.db.RecordDBRepository
import net.panuwach.tasks.models.common.Statement
import net.panuwach.tasks.models.request.ViewHistoryRequest
import net.panuwach.tasks.util.DateTimeHelper
import org.joda.time.DateTime

import scala.collection.immutable.SortedMap
import scala.concurrent.{ExecutionContext, Future}

/**
  * Responsible for business logic and model mapping
  * @param recordRepository
  * @param executionContext
  */

class StatementHistoryService(recordRepository: RecordDBRepository, statementCache: StatementCacheRepository)(implicit
    ec: ExecutionContext
) extends DateTimeHelper {
  def searchHourlyStatementHistory(request: ViewHistoryRequest): Future[List[Statement]] = {
    buildHourlyStatement(request.startDatetime, request.endDatetime)
  }

  private def buildHourlyStatement(start: DateTime, end: DateTime): Future[List[Statement]] = {
    // This method we get the latest state before the date as a checkpoint and building history from there
    for {
      previousDateStatement <- statementCache.getDateStatement(start.toLocalDate.minusDays(1))
      records               <- recordRepository.searchRecordInRange(roundDownDate(start), end)
    } yield {
      val hourlyBreakdown = buildHourlyBreakdown(start, end).map(datetime => datetime -> Statement(datetime, 0))
      val (before, between) = records.partition({ record =>
        record.datetime.isBefore(start) || record.datetime.isEqual(start)
      })
      val startAmount = before.map(_.amount).sum + previousDateStatement.amount
      val initialFold = FoldingState(
        dateCache = SortedMap(hourlyBreakdown: _*),
        amount = startAmount,
        lastHour = start
      )
      val finalState = between.foldLeft(initialFold)((state, record) => {
        val recordDatetime = record.datetime
        val updateFromLast = updateMap(
          state.dateCache,
          date => date.isBefore(recordDatetime) && date.isAfter(state.lastHour),
          Statement(recordDatetime, state.amount)
        )
        val updateFromCurrent =
          updateMap(updateFromLast, date => date.isEqual(recordDatetime), Statement(recordDatetime, state.amount))
        FoldingState(updateFromCurrent, record.apply(state.amount), recordDatetime)
      })
      finalState.dateCache.values.toList
    }
  }

  private def updateMap(
      dateCache: SortedMap[DateTime, Statement],
      predicate: DateTime => Boolean,
      updateValue: Statement
  ): SortedMap[DateTime, Statement] = {
    dateCache ++ dateCache.collect { case (datetime, _) if predicate(datetime) => datetime -> updateValue }
  }

  private case class FoldingState(dateCache: SortedMap[DateTime, Statement], amount: Double, lastHour: DateTime)
}
