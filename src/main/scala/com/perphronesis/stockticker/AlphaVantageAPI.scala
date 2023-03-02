package com.perphronesis.stockticker

import com.typesafe.scalalogging.LazyLogging
import org.json4s
import org.json4s.JNothing.Values
import org.json4s.{DefaultFormats, Extraction, Formats, JArray, JObject, JValue}
import org.json4s.native.{JsonParser, prettyJson}
import scalaj.http.Http

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.{Calendar, Date}
import scala.collection.immutable.Map
import scala.util.{Failure, Success, Try}

case class StockFeed(metadata: Metadata, timeseries: Map[Date, Price])
case class Metadata(information:String, symbol:String, lastrefreshed:Date, output:String, timezone:String)
case class Price(open:String, high:String, low:String, close: String, adjclose:String, volume:String, dividend:String,coefficient:String) {
  val closePrice = close.toDouble
}

object AlphaVantageAPI extends LazyLogging with Mapper {

//API Key BLY0WPDTWDJX9RZH

  implicit val formats: Formats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  }

  /*

   */
  def getTickerInformation(endpoint:String, lookback:Int = 5): Either[String, Double] = {

    val stockInfo = callApi(endpoint)

    val result = stockInfo match {
      case Success(s) => {
        //Sorts the timeseries data by the stock price date to ensure we are in desc order
        val sortedValues = s.timeseries.toVector.sortWith((x,y) => x._1.after(y._1))

        //Take the last X number of stock price data to process in the aggregation
        val lastTake = sortedValues.take(lookback)
        lastTake.foreach((x) => {
          logger.debug(s"Date: ${x._1} with Price: ${x._2.closePrice}")
        })
        val finalAvg = lastTake.map(_._2.closePrice).sum / lastTake.size
        Right(finalAvg)
      }
      case Failure(s) => Left(s.getMessage)
    }

    result
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
        case None => return Failure(new Exception("Error in calling the API: " + httpResponse.body))
      }
    } catch {
      case e: Throwable => return Failure(new Exception("Error in calling the API: " + e.toString))
    }
  }

  val ticker = """{
                 |    "Meta Data": {
                 |        "1. Information": "Daily Time Series with Splits and Dividend Events",
                 |        "2. Symbol": "GOOG",
                 |        "3. Last Refreshed": "2023-02-28",
                 |        "4. Output Size": "Compact",
                 |        "5. Time Zone": "US/Eastern"
                 |    },
                 |    "Time Series (Daily)": {
                 |        "2023-02-28": {
                 |            "1. open": "89.54",
                 |            "2. high": "91.45",
                 |            "3. low": "89.52",
                 |            "4. close": "90.3",
                 |            "5. adjusted close": "90.3",
                 |            "6. volume": "30546912",
                 |            "7. dividend amount": "0.0000",
                 |            "8. split coefficient": "1.0"
                 |        },
                 |        "2023-02-27": {
                 |            "1. open": "90.09",
                 |            "2. high": "90.4499",
                 |            "3. low": "89.61",
                 |            "4. close": "90.1",
                 |            "5. adjusted close": "90.1",
                 |            "6. volume": "22724262",
                 |            "7. dividend amount": "0.0000",
                 |            "8. split coefficient": "1.0"
                 |        }
                 |    }
                 |}""".stripMargin

}
