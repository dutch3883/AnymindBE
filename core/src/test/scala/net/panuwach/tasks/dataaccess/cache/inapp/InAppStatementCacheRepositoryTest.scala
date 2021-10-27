package net.panuwach.tasks.dataaccess.cache.inapp

import net.panuwach.tasks.dataaccess.cache.inapp.InAppStatementCacheRepository.FirstTransactionDate
import net.panuwach.tasks.{TestHelper, TestHelperAsync}
import net.panuwach.tasks.dataaccess.db.RecordDBRepository
import net.panuwach.tasks.dataaccess.db.model.StatementCache
import net.panuwach.tasks.models.common.enums.RecordTypes.Deposit
import net.panuwach.tasks.models.internal.RecordInternal
import org.joda.time.{DateTime, LocalDate}

import scala.collection.SortedMap
import scala.concurrent.Future

class InAppStatementCacheRepositoryTest extends TestHelperAsync {
  def getCacheRepo(): InAppStatementCacheRepository = {
    val mockDBRepository = mock[RecordDBRepository]
    val executionContext = scala.concurrent.ExecutionContext.Implicits.global

    val mockRecords = Seq(
      RecordInternal(Deposit, DateTime.parse("2019-10-06T10:48:01+00:00"), 100),
      RecordInternal(Deposit, DateTime.parse("2019-10-07T10:48:01+00:00"), 20),
      RecordInternal(Deposit, DateTime.parse("2019-10-10T13:48:01+00:00"), 24),
      RecordInternal(Deposit, DateTime.parse("2019-10-10T23:59:01+00:00"), 7)
    )
    when(mockDBRepository.getAllRecords()).thenReturn(Future.successful(mockRecords))
    (new InAppStatementCacheRepository(mockDBRepository)(executionContext))
  }

  "initializeCache" when {
    "have multiple price in the same day" should {
      "build cache correctly" in {

        val cacheRepo = getCacheRepo()
        cacheRepo
          .initializeCache()
          .map(cache =>
            cache shouldBe SortedMap(
              LocalDate.parse("2019-10-05") -> StatementCache(1000.0),
              LocalDate.parse("2019-10-06") -> StatementCache(1100.0),
              LocalDate.parse("2019-10-07") -> StatementCache(1120.0),
              LocalDate.parse("2019-10-08") -> StatementCache(1120.0),
              LocalDate.parse("2019-10-09") -> StatementCache(1120.0),
              LocalDate.parse("2019-10-10") -> StatementCache(1151.0)
            )
          )(executionContext)
      }
    }
  }

  "getDateStatement" when {
    "date request is before first transaction" should {
      "return default value" in {
        val cacheRepo = getCacheRepo()
        cacheRepo.getDateStatement(LocalDate.parse("2020-10-11")).map(
          statement => statement.amount shouldBe 1151.0
        )
      }
    }

    "date request after the last transaction" should {
      "return latest updated value" in {
        val cacheRepo = getCacheRepo()
        cacheRepo.getDateStatement(FirstTransactionDate.minusDays(2)).map(
          statement => statement.amount shouldBe 1000.0
        )
      }
    }
  }
}
