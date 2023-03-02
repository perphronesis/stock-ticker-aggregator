FROM openjdk:8u181-jre-slim

RUN mkdir -p /app

ARG JAR_FILE
COPY target/${JAR_FILE} /app/bin/stock-ticker.jar

WORKDIR /app
CMD ["java", "-jar", "/app/bin/stock-ticker.jar"]