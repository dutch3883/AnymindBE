package net.panuwach.tasks.models.request

import org.joda.time.DateTime

case class ViewHistoryRequest(startDatetime: DateTime, endDatetime: DateTime)
