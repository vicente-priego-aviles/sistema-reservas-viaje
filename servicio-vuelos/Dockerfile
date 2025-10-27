# Imagen base ligera con JDK 21
FROM eclipse-temurin:21-jdk-alpine

# Crear usuario no root para mayor seguridad
RUN addgroup -S appuser && adduser -S -G appuser appuser
USER appuser

# Variable de build: ubicación del jar generado por Maven
ARG JAR_FILE=target/*.jar

# Copiar el JAR al contenedor
COPY ${JAR_FILE} app.jar

# Puerto que expone el microservicio (ajustar según corresponda)
EXPOSE 9081

# Configuración por defecto de la JVM
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de arranque: usa JAVA_OPTS y ejecuta el jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]