# Multi-stage build: the runtime image contains only the JRE and the jar.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY cloudstone-kernel/pom.xml cloudstone-kernel/
COPY cloudstone-runtime/pom.xml cloudstone-runtime/
COPY cloudstone-tenancy/pom.xml cloudstone-tenancy/
COPY cloudstone-persistence/pom.xml cloudstone-persistence/
COPY cloudstone-server/pom.xml cloudstone-server/
RUN mvn -q -B dependency:go-offline
COPY . .
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/cloudstone-server/target/cloudstone-server-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=cloud
EXPOSE 8080
USER 1000
ENTRYPOINT ["java", "-jar", "app.jar"]
