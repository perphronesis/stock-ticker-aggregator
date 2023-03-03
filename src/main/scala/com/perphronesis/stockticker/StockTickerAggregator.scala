package com.perphronesis.stockticker

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import java.util.Date
import scala.jdk.CollectionConverters.MapHasAsScala
import scala.language.implicitConversions
import scala.math.pow

/**
 * Main entry point for the application.  This performs the following steps:
 * 1. Checks that the APIKEY env variable is set - if not, exits
 * 2. Pulls config info from the applicaiton.conf file that allows some customization of the lookup
 * 3. Calls the ASV API and returns a scala Either which allows us to determine success or failure of the call
 * 4. If success (Right) - calculate the Average price based on the price field in the config file.  If the price field
 *    isn't recognized, it defaults to close price.
 */
object StockTickerAggregator extends App with Mapper with LazyLogging {

  //Let's check that the APIKEY is set.  If not set, exit cause we can't do much
  private val envVars = System.getenv().asScala
  envVars.get("APIKEY") match {
    case Some(x) => logger.debug(s"APIKey is set to $x")
    case None => {
      logger.error("APIKEY env variable is not set.  Please set before running.")
      System.exit(1)
    }
  }

  //Gather some config information from application.conf file.
  private val config = ConfigFactory.load()
  val endpoint = config.getString(Mapper.endpoint)
  val lookback = config.getInt(Mapper.lookback_days)
  private val stock = config.getString(Mapper.symbol)
  private val priceField = config.getString(Mapper.price_field)

  logger.debug(s"Endpoint lookup: $endpoint")

  //This is the API call and will return an Either based on success or failure of the call
  private val stockData = AlphaVantageAPI.getStockInformation(endpoint, lookback)

  //Wrapped aggregate function to allow for future extension of aggregation functions on collections.
  implicit def agg[A: Fractional](self: Iterable[A]): AggregateFunctions[A] = new AggregateFunctions(self)

  //Based on the Either received, let's process it based on the arguments provided
  //Left means something went wrong and the message should contain more info.
  //Right means success and we can continue to calculate the average in question.
  stockData match {
    case Left(s) => println(s"Something went wrong with lookup: $s")
    case Right(i) => {
      //We we able to successfully process the StockFeed and have a collection of dates and prices.
      //Let's use the configurable price field to find the average for the field in question.
      val result = getStockMetrics(i, priceField).avg.rounded(2)
      println(s"${i.size} days average $priceField price for $stock is $$${result}")
    }
  }

  /*
  This function reduces the price data field of interest into an iterable collection to run aggregate calculations on.
   */
  private def getStockMetrics(source: Vector[(Date, Price)], price: String): Iterable[Double] = {

    price match {
      case Mapper.close => source.map(_._2.closePrice)
      case Mapper.open => source.map(_._2.openPrice)
      case Mapper.high => source.map(_._2.highPrice)
      case Mapper.low => source.map(_._2.lowPrice)
      case _ => {
        logger.info("This is an unknown price field - returning on stock close price instead.")
        source.map(_._2.closePrice)
      }
    }
  }

  /*
     Simple function that rounds double values to 2 digits
      */
  implicit class ExtendedDouble(n: Double) {
    def rounded(x: Int): Double = {
      val w = pow(10, x)
      (n * w).toLong.toDouble / w
    }
  }

}


