FROM eclipse-temurin:21-jdk-alpine
RUN addgroup -S appuser && adduser -S -G appuser appuser
USER appuser
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]