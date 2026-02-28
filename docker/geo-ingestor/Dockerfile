FROM maven:3-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY model/pom.xml model/
COPY common/pom.xml common/
COPY military-aircraft-svc/pom.xml military-aircraft-svc/
COPY aircraft-db-ingestor/pom.xml aircraft-db-ingestor/
COPY geo-ingestor/pom.xml geo-ingestor/
COPY military-watcher-api/pom.xml military-watcher-api/
RUN mvn dependency:go-offline -pl geo-ingestor -am -B
COPY model/src model/src
COPY geo-ingestor/src geo-ingestor/src
RUN mvn package -pl geo-ingestor -am -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /build/geo-ingestor/target/geo-ingestor-*.jar app.jar
USER appuser
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
