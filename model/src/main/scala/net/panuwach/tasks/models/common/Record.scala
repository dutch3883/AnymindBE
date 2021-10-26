package net.panuwach.tasks.models.common

import akka.http.scaladsl.model.DateTime

case class Transaction (datetime: DateTime,amount: Double)
