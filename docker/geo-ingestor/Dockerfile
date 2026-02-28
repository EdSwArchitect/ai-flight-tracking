FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY model/pom.xml model/
COPY geo-ingestor/pom.xml geo-ingestor/
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
