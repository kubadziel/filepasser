FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /build

COPY .. /build

RUN mvn -pl uploader -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /build/uploader/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
