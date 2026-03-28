# Phase 8 — fat JAR + JRE (Jammy base for amd64/arm64; Spring Boot jar excludes *-plain.jar)
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl unzip \
  && rm -rf /var/lib/apt/lists/*

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src

RUN chmod +x mvnw && sed -i 's/\r$//' mvnw \
  && ./mvnw -B -DskipTests package \
  && JAR=$(ls /app/target/*.jar | grep -v '\-plain\.jar$' | head -n1) \
  && test -n "$JAR" && cp "$JAR" /app/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
