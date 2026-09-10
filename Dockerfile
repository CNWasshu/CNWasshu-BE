FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system --gid 10001 spring \
    && useradd --system --uid 10001 --gid spring --no-create-home spring
COPY --from=builder --chown=spring:spring /workspace/build/libs/*.jar app.jar

USER spring
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
