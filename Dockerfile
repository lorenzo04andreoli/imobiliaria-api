FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src src

RUN ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring \
    && adduser -S spring -G spring \
    && mkdir -p /app/uploads/imoveis \
    && chown -R spring:spring /app

COPY --from=build /app/target/*.jar app.jar

USER spring:spring

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
