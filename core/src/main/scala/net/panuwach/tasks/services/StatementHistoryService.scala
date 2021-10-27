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
    import org.joda.time.DateTimeZone
    DateTimeZone.setDefault(DateTimeZone.UTC)
    for {
      previousDateStatement <- statementCache.getDateStatement(start.toLocalDate.minusDays(1))
      records               <- recordRepository.searchRecordInRange(roundDownDate(start), end)
    } yield {
      val hourlyBreakdown = buildHourlyBreakdownBetween(start, end).map(datetime => datetime -> Statement(datetime, 0))
      val (before, between) = records.partition({ record =>
        record.datetime.isBefore(start) || record.datetime.isEqual(start)
      })
      val startAmount = before.map(_.amount).sum + previousDateStatement.amount
      val initialFold = FoldingState(
        dateCache = SortedMap(hourlyBreakdown: _*),
        amount = startAmount,
        lastHour = start
      )
      val finalState = between.foldLeft(initialFold)((prev, record) => {
        val recordDatetime = roundUpHour(record.datetime)
        val updateFromLast = updateMap(
          prev.dateCache,
          date => date.isBefore(recordDatetime) && date.isAfter(prev.lastHour),
          statement => Statement(statement.datetime, prev.amount)
        )

        val updateFromCurrent =
          updateMap(
            updateFromLast,
            _.isEqual(recordDatetime),
            statement => Statement(statement.datetime, record.apply(prev.amount))
          )
        FoldingState(updateFromCurrent, record.apply(prev.amount), recordDatetime)
      })

      updateMap(
        finalState.dateCache,
        dateToCheck => dateToCheck.isAfter(finalState.lastHour),
        statement => Statement(statement.datetime, finalState.amount)
      ).values.toList
    }
  }

  private def updateMap(
      dateCache: SortedMap[DateTime, Statement],
      predicate: DateTime => Boolean,
      fUpdate: Statement => Statement
  ): SortedMap[DateTime, Statement] = {
    dateCache ++ dateCache.collect {
      case (datetime, statement) if predicate(datetime) => datetime -> fUpdate(statement)
    }
  }

  private case class FoldingState(dateCache: SortedMap[DateTime, Statement], amount: Double, lastHour: DateTime)
}
