package net.panuwach.tasks.util

import org.joda.time.{DateTime, Days, Hours, LocalDate}

trait DateTimeHelper {
  protected def buildHourlyBreakdownBetween(startTime: DateTime, endTime: DateTime): Seq[DateTime] = {
    val roundDownStart = roundUpHour(startTime)
    val roundDownEnd   = roundDownHour(endTime)
    val diffHours      = Hours.hoursBetween(roundDownStart, roundDownEnd).getHours
    (0 to diffHours).map(incrementHour => roundDownStart.plusHours(incrementHour))
  }

  protected def buildDailyBreakdown(startDate: LocalDate, endDate: LocalDate): Seq[LocalDate] = {
    val diffHours      = Days.daysBetween(startDate, endDate).getDays
    (0 to diffHours).map(incrementHour => startDate.plusDays(incrementHour))
  }

  protected def roundDownHour(time: DateTime): DateTime = {
    // to round down the detail value less than hour
    time.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
  }

  protected def roundUpHour(time: DateTime): DateTime = {
    if(time.minuteOfHour().get() + time.secondOfMinute().get() + time.millisOfSecond().get() > 0){
      roundDownHour(time).plusHours(1)
    } else time
  }


  protected def roundDownDate(time: DateTime): DateTime = {
    // to round down the detail value less than hour
    roundDownHour(time).withHourOfDay(0)
  }


}