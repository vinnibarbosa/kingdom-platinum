FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system pokefichas \
    && useradd --system --gid pokefichas --home-dir /app --shell /usr/sbin/nologin pokefichas

COPY --from=build --chown=pokefichas:pokefichas /app/target/api-poke-fichas-0.0.1-SNAPSHOT.jar /app/app.jar

USER pokefichas

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
