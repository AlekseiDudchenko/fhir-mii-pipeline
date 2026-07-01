FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y --no-install-recommends maven && \
    mvn package -DskipTests --batch-mode && \
    rm -rf /root/.m2

FROM eclipse-temurin:21-jre
RUN groupadd --system appuser && useradd --system --gid appuser appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh
USER appuser
EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
