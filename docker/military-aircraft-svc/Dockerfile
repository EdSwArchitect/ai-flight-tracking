FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY model/pom.xml model/
COPY common/pom.xml common/
COPY military-aircraft-svc/pom.xml military-aircraft-svc/
RUN mvn dependency:go-offline -pl military-aircraft-svc -am -B
COPY model/src model/src
COPY common/src common/src
COPY military-aircraft-svc/src military-aircraft-svc/src
RUN mvn package -pl military-aircraft-svc -am -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /build/military-aircraft-svc/target/military-aircraft-svc-*.jar app.jar
USER appuser
EXPOSE 8081 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
