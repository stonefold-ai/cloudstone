# Multi-stage build: the runtime image contains only the JRE and the jar.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY gateway-core/pom.xml gateway-core/
COPY gateway-stele/pom.xml gateway-stele/
COPY gateway-tenancy/pom.xml gateway-tenancy/
COPY gateway-persistence/pom.xml gateway-persistence/
COPY gateway-server/pom.xml gateway-server/
RUN mvn -q -B dependency:go-offline
COPY . .
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/gateway-server/target/gateway-server-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=cloud
EXPOSE 8080
USER 1000
ENTRYPOINT ["java", "-jar", "app.jar"]
