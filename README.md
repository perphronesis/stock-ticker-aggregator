# stock-ticker-aggregator

Simple scala application that calls the Alpha Vantage API (www.alphavantage.co) to retrieve individual stock data.  It extracts the json payload into a set of Scala case classes and calculates the average closing price of that stock.

### Building and running stock-ticker-aggregator

Prerequisites

* sbt 1.8.2 (for building)
* An APIKey from Alpha Vantage for use in the API calls
* Java 17 (if running locally) or Docker if running in a container

```
# Checkout code and build
git clone https://github.com/perphronesis/stock-ticker-aggregator.git and cd stock-ticker-aggregator

# Customize and properties in src/main/resources/application.conf
# symbol -> ticker symbol to use in the API
# price_field -> the price field to aggregation on.  Valid values are close, open, high, low
# lookback_days -> number of days to calculate average
# api_key -> AVS api key if needed in the API call.  Can set as env var
# endpoint -> The endpoint to call in the API.  Currently this only works on the function=TIME_SERIES_DAILY_ADJUSTED endpoint
 
sbt assembly
```

### Running application

#### Option 1: Run locally
```
# If needed set API_KEY from AVS as an env variable:
export APIKEY=XXXX
java -jar target/stock-ticker-aggregator-0.1.0.jar
```

#### Option 2: Run through docker
```
#NOTE: Substitute your APIKey in the docker build command below
docker build -t ticker-app --build-arg JAR_FILE=stock-ticker-aggregator-0.1.0.jar --build-arg APIKEY=<APIKEY> .
docker run --rm --name id-ticker-app ticker-app
```
