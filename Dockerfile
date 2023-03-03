FROM openjdk:17-jdk-slim

RUN mkdir -p /app

ARG JAR_FILE
COPY target/${JAR_FILE} /app/bin/stock-ticker.jar

ARG APIKEY
ENV APIKEY=${APIKEY}

WORKDIR /app
CMD ["java", "-jar", "/app/bin/stock-ticker.jar"]