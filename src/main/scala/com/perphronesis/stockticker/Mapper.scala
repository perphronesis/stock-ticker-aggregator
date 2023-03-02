package com.perphronesis.stockticker

import org.json4s.JsonAST.JValue

trait Mapper {

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
    case ("6. volume", x) => ("volume", x)
    case ("7. dividend amount", x) => ("dividend", x)
    case ("8. split coefficient", x) => ("coefficient", x)
  }

}
