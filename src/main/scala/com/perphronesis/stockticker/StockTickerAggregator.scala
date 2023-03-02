package com.perphronesis.stockticker

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.math.pow

object StockTickerAggregator extends App with LazyLogging {

  implicit class ExtendedDouble(n: Double) {
    def rounded(x: Int) = {
      val w = pow(10, x)
      (n * w).toLong.toDouble / w
    }
  }

  val endpoint = ConfigFactory.load().getString("app.endpoint")
  val lookback = ConfigFactory.load().getInt("app.lookback_days")
  val stock = ConfigFactory.load().getString("app.symbol")
  logger.info(s"Endpoint lookup: $endpoint")
  val result = AlphaVantageAPI.getTickerInformation(endpoint, lookback)

  result match {
    case Left(s) => println(s"Something went wrong with lookup: $s")
    case Right(i) => {
      val avg = i.rounded(2) //round to 2 decimal places
      println(s"$lookback days avg stock price for $stock is $$${avg}")
    }
  }

}
