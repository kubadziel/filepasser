FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /build

COPY .. /build

RUN mvn -pl router -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy router JAR correctly
COPY --from=builder /build/router/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
