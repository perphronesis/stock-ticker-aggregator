package com.perphronesis.stockticker

import org.json4s.JsonAST.JValue

trait Mapper {

  /*
  The purpose of this mapping is that when we extract the JSON data into the case classes, field
  names cannot support spaces - so we map the JSON fields to case class fields
   */
  val mappings: PartialFunction[(String, JValue),(String, JValue)] = {
    case ("Meta Data", x) => ("metadata", x)
    case ("1. Information", x) => ("information", x)
    case ("2. Symbol", x) => ("symbol", x)
    case ("3. Last Refreshed", x) => ("lastrefreshed", x)
    case ("4. Output Size", x) => ("output", x)
    case ("5. Time Zone", x) => ("timezone", x)
    case ("Time Series (Daily)", x) => ("timeseries", x)
    case ("1. open", x) => ("open", x)
    case ("2. high", x) => ("high", x)
    case ("3. low", x) => ("low", x)
    case ("4. close", x) => ("close", x)
    case ("5. adjusted close", x) => ("adjclose", x)
    case ("6. volume", x) => ("volumestr", x)
    case ("7. dividend amount", x) => ("dividend", x)
    case ("8. split coefficient", x) => ("coefficient", x)
  }

}

/*
Some constant values used within the application
 */
object Mapper {
  val open = "open"
  val close = "close"
  val low = "low"
  val volume = "volume"
  val high = "high"

  val price_field = "app.price_field"
  val lookback_days = "app.lookback_days"
  val api_key = "app.lookback_days"
  val endpoint = "app.endpoint"
  val symbol = "app.symbol"
}
