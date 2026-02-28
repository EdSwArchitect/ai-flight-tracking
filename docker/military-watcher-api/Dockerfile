FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY model/pom.xml model/
COPY common/pom.xml common/
COPY military-watcher-api/pom.xml military-watcher-api/
RUN mvn dependency:go-offline -pl military-watcher-api -am -B
COPY model/src model/src
COPY common/src common/src
COPY military-watcher-api/src military-watcher-api/src
RUN mvn package -pl military-watcher-api -am -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /build/military-watcher-api/target/military-watcher-api-*.jar app.jar
USER appuser
EXPOSE 7070 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
