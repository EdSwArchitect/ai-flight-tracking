FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY model/pom.xml model/
COPY common/pom.xml common/
COPY aircraft-db-ingestor/pom.xml aircraft-db-ingestor/
RUN mvn dependency:go-offline -pl aircraft-db-ingestor -am -B
COPY model/src model/src
COPY common/src common/src
COPY aircraft-db-ingestor/src aircraft-db-ingestor/src
RUN mvn package -pl aircraft-db-ingestor -am -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /build/aircraft-db-ingestor/target/aircraft-db-ingestor-*.jar app.jar
USER appuser
EXPOSE 8081 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
