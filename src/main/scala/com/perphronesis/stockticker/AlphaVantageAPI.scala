package com.perphronesis.stockticker

import com.typesafe.scalalogging.LazyLogging
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.{JsonParser}
import scalaj.http.Http

import java.text.SimpleDateFormat

import java.util.Date
import scala.util.{Failure, Success, Try}

//Below are the case classes that are used to serialize the data into scala classes to make them easier to work with.
case class StockFeed(metadata: Metadata, timeseries: Map[Date, Price])
case class Metadata(information:String, symbol:String, lastrefreshed:Date, output:String, timezone:String)
case class Price(open:String, high:String, low:String, close: String, adjclose:String, volumestr:String, dividend:String,coefficient:String) {
  val closePrice: Double = close.toDouble
  val openPrice: Double = open.toDouble
  val highPrice: Double = high.toDouble
  val lowPrice: Double = low.toDouble
  val volume: Long = volumestr.toLong
}

/**
 * The purpose of this object is to perform the Rest API call to ASV and if possible, serialize it into a useful case class
 * and ultimately a collection to run functions against.
 * If the API call or serialization is unsuccessful, the methods use Eithers and/or Try objects to capture the failure
 * or the success for the caller to determine how to handle.
 */
object AlphaVantageAPI extends LazyLogging with Mapper {

  implicit val formats: Formats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  }

  /**
   * getStockInformation takes a valid Rest API endpoint from ASV and processes it (if possible) into StockFeed case class
   * and wraps it in an Either (flattened into a simplier collection) in case it doesn't work.
   *
   * @param endpoint - right now it only supports function=TIME_SERIES_DAILY_ADJUSTED function endpoint
   * @param lookback - how many days to process.  If the returned collection is less than the lookback days, we default to the size of the
   *                 collection
   * @return an Either where Left is an error message and Right is an iterable collection of Dates and Price objects
   */
  def getStockInformation(endpoint:String, lookback:Int = 5): Either[String, Vector[(Date, Price)]] = {
    val stockInfo = callApi(endpoint)

    //This is an Either which will contain either a failure message or a valid collection result to the returning
    //caller to decide what to do with.
    stockInfo match {
      case Success(s) => {
        //Sorts the timeseries data by the stock price date to ensure we are in desc order
        val sortedValues = s.timeseries.toVector.sortWith((x,y) => x._1.after(y._1))

        //We don't want to run into condition where lookback value is MORE than in the collection.
        //If we run into this, let's just use the entire collection
        val takeDays = if (lookback > sortedValues.size) {
          sortedValues.size
        } else {
          lookback
        }
        //Take the last X number of stock price data to process in the aggregation
        val lastTake = sortedValues.take(takeDays)
        lastTake.foreach((x) => {
          logger.debug(s"Date: ${x._1} with Price: ${x._2.closePrice}")
        })
        //Wrap the collection in the Either Right
        Right(lastTake)
      }
      case Failure(s) => Left(s.getMessage)
    }
  }

  /*
  This function calls the Rest API and returns a wrapped Try object to capture the success or failure
  of the API call.  The calling function must handle whether we get a successful StockFeed object or
  a failure message from the API call.  The failure could be anything from API not available to access
  error, etc.
   */
  private def callApi(endpoint: String): Try[StockFeed] = {

    //Wrap in try/catch to catch issue with endpoint (invalid, etc)
    try {
      val httpResponse = Http(endpoint)
        .header("content-type", "application/json")
        .header("Charset", "UTF-8")
        .timeout(connTimeoutMs = 15000, readTimeoutMs = 15000)
        .asString

      val result = JsonParser.parse(httpResponse.body)
      val parsedJson = result.transformField(mappings)
      parsedJson.extractOpt[StockFeed] match {
        case Some(x) => return Success(x)
        case None => return Failure(new Exception("Are you calling the TIME_SERIES_DAILY_ADJUSTED function with valid APIKEY? " + httpResponse.body))
      }
    } catch {
      case e: Throwable => return Failure(new Exception("Error in calling the API: " + e.toString))
    }
  }
}
