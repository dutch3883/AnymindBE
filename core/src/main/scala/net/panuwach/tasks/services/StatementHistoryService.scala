package net.panuwach.tasks.services

import net.panuwach.tasks.dataaccess.cache.StatementCacheRepository
import net.panuwach.tasks.dataaccess.db.RecordDBRepository
import net.panuwach.tasks.dataaccess.db.model.StatementCache
import net.panuwach.tasks.models.common.Statement
import net.panuwach.tasks.models.internal.RecordInternal
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
      calculateHourlyBreakdownAmount(start, end, previousDateStatement, records)
    }
  }

  private def calculateHourlyBreakdownAmount(
                                              start: DateTime,
                                              end: DateTime,
                                              latestCachePrice: StatementCache,
                                              records: Seq[RecordInternal]
  ): List[Statement] = {
    val hourlyBreakdown = buildHourlyBreakdownBetween(start, end).map(datetime => datetime -> Statement(datetime, 0))
    val (before, between) = records.partition({ record =>
      record.datetime.isBefore(start) || record.datetime.isEqual(start)
    })
    /* This is like for loop with FoldingState as parameter carrying each iteration
    *   Implementation Detail
    * 1.) First we calculate the latest price we can get from cache of the day before start datetime.
    * 2.) We use the end of day before startDateTime as PinPoint to calculate price at startDateTime.
    * 3.) We generate hourly bin with empty value and iterate through records in the interval client request.
    * 4.) We update hourly price with filter condition only those that would get effect by the iterating record.
    * 5.) At the end, there will be hourly bin after last record haven't been yet calculate, so we update those at the end.
    *  */

    val startAmount = before.map(_.amount).sum + latestCachePrice.amount
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
