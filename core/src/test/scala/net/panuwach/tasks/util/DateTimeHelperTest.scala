package net.panuwach.tasks.util

import net.panuwach.tasks.TestHelper
import org.joda.time.DateTime

class DateTimeHelperTest extends TestHelper with DateTimeHelper {

  "buildHourlyBreakdown" should {
    "get list of breakdown correctly" when {
      "calculate non-rounded hour" in {
        val start  = DateTime.parse("2021-10-25T18:22:00+00:00")
        val end    = DateTime.parse("2021-10-25T21:54:00+00:00")
        val actual = buildHourlyBreakdownBetween(start, end)
        val expect = Seq(
          "2021-10-25T19:00:00.000Z",
          "2021-10-25T20:00:00.000Z",
          "2021-10-25T21:00:00.000Z"
        ).map(DateTime.parse)
        actual shouldBe expect
      }

      "calculate rounded hour" in {
        val start  = DateTime.parse("2021-10-25T18:00:00+00:00")
        val end    = DateTime.parse("2021-10-25T21:00:00+00:00")
        val actual = buildHourlyBreakdownBetween(start, end)
        val expect = Seq(
          "2021-10-25T18:00:00.000Z",
          "2021-10-25T19:00:00.000Z",
          "2021-10-25T20:00:00.000Z",
          "2021-10-25T21:00:00.000Z"
        ).map(DateTime.parse)
        actual shouldBe expect
      }

      "calculate over midnight with timezone" in {
        val start  = DateTime.parse("2021-10-25T22:01:00+00:00")
        val end    = DateTime.parse("2021-10-26T03:33:00+02:00")
        val actual = buildHourlyBreakdownBetween(start, end)
        val expect = Seq(
          "2021-10-25T23:00:00.000Z",
          "2021-10-26T00:00:00.000Z",
          "2021-10-26T01:00:00.000Z"
        ).map(DateTime.parse)
        actual shouldBe expect
      }
    }
  }

//  test("testRoundDownHour") {}

}
