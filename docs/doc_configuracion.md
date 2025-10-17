# ⚙️ Configuración del Sistema

Guía completa de configuración para todos los componentes del sistema.

---

## 📋 Tabla de Contenidos

- [Configuración de Microservicios](#-configuración-de-microservicios)
- [Configuración de Camunda](#-configuración-de-camunda)
- [Perfiles de Spring](#-perfiles-de-spring)
- [Variables de Entorno](#-variables-de-entorno)
- [Configuración de Base de Datos](#-configuración-de-base-de-datos)

---

## 🔧 Configuración de Microservicios

Cada microservicio tiene su propio `application.yml` en `src/main/resources/`.

### Configuración Base (Común a Todos)

```yaml
# application.yml
spring:
  application:
    name: servicio-<nombre>
  
  # Base de datos H2
  datasource:
    url: jdbc:h2:mem:<nombre>_db
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  # JPA
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
  
  # Consola H2
  h2:
    console:
      enabled: true
      path: /h2-console

# Servidor
server:
  port: 90XX
  shutdown: graceful

# Camunda Zeebe
camunda:
  client:
    zeebe:
      gateway-address: ${ZEEBE_ADDRESS:localhost:26500}
      rest-address: ${ZEEBE_REST:http://localhost:8080}
      prefer-rest-over-grpc: false
    mode: simple
    auth:
      username: demo
      password: demo

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# OpenAPI
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

# Logging
logging:
  level:
    dev.javacadabra: DEBUG
    io.camunda.zeebe: INFO
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Configuración Específica por Servicio

#### servicio-clientes (9080)

```yaml
server:
  port: 9080

spring:
  datasource:
    url: jdbc:h2:mem:clientes_db

# Configuración específica de negocio
cliente:
  validacion:
    tarjeta:
      algoritmo: luhn
      intentos-maximos: 3
    email:
      patron: "^[A-Za-z0-9+_.-]+@(.+)$"
```

#### servicio-vuelos (9081)

```yaml
server:
  port: 9081

spring:
  datasource:
    url: jdbc:h2:mem:vuelos_db

# Configuración de vuelos
vuelo:
  reserva:
    timeout-minutos: 15
    asientos-por-fila: 6
  precios:
    base: 100
    factor-demanda: 1.2
```

#### servicio-hoteles (9082)

```yaml
server:
  port: 9082

spring:
  datasource:
    url: jdbc:h2:mem:hoteles_db

# Configuración de hoteles
hotel:
  reserva:
    check-in: "14:00"
    check-out: "12:00"
    minimo-noches: 1
  cancelacion:
    plazo-dias: 7
    penalizacion-porcentaje: 20
```

#### servicio-alquiler-coches (9083)

```yaml
server:
  port: 9083

spring:
  datasource:
    url: jdbc:h2:mem:coches_db

# Configuración de alquiler
alquiler:
  edad-minima: 21
  licencia:
    validez-minima-meses: 12
  seguro:
    incluido: true
    franquicia: 500
```

#### servicio-pagos (9084)

```yaml
server:
  port: 9084

spring:
  datasource:
    url: jdbc:h2:mem:pagos_db

# Configuración de pagos
pago:
  procesamiento:
    timeout-segundos: 30
    reintentos: 3
  limites:
    monto-minimo: 10
    monto-maximo: 50000
    monto-advertencia: 5000
  tarjeta:
    tipos-aceptados:
      - VISA
      - MASTERCARD
      - AMEX
```

#### servicio-reservas (9090)

```yaml
server:
  port: 9090

spring:
  datasource:
    url: jdbc:h2:mem:reservas_db

# Configuración del coordinador
reserva:
  proceso:
    timeout-minutos: 30
  bpmn:
    auto-deploy: true
    resources: classpath*:bpmn/*.bpmn
```

---

## 🎯 Configuración de Camunda

### docker-compose-camunda.yml

```yaml
version: '3.8'

services:
  zeebe:
    image: camunda/zeebe:8.7.0
    container_name: zeebe
    ports:
      - "26500:26500"
      - "9600:9600"
    environment:
      - ZEEBE_LOG_LEVEL=info
      - ZEEBE_BROKER_GATEWAY_ENABLE=true
      - ZEEBE_BROKER_NETWORK_HOST=0.0.0.0
    volumes:
      - zeebe-data:/usr/local/zeebe/data
    networks:
      - camunda-network

  operate:
    image: camunda/operate:8.7.0
    container_name: operate
    ports:
      - "8080:8080"
    environment:
      - CAMUNDA_OPERATE_ZEEBE_GATEWAYADDRESS=zeebe:26500
      - CAMUNDA_OPERATE_ELASTICSEARCH_URL=http://elasticsearch:9200
      - CAMUNDA_OPERATE_ZEEBEELASTICSEARCH_URL=http://elasticsearch:9200
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      - zeebe
      - elasticsearch
    networks:
      - camunda-network

  tasklist:
    image: camunda/tasklist:8.7.0
    container_name: tasklist
    ports:
      - "8081:8080"
    environment:
      - CAMUNDA_TASKLIST_ZEEBE_GATEWAYADDRESS=zeebe:26500
      - CAMUNDA_TASKLIST_ELASTICSEARCH_URL=http://elasticsearch:9200
      - CAMUNDA_TASKLIST_ZEEBEELASTICSEARCH_URL=http://elasticsearch:9200
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      - zeebe
      - elasticsearch
    networks:
      - camunda-network

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.9.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - camunda-network

volumes:
  zeebe-data:
  elasticsearch-data:

networks:
  camunda-network:
    driver: bridge
```

---

## 🔀 Perfiles de Spring

### application-dev.yml (Desarrollo)

```yaml
spring:
  config:
    activate:
      on-profile: dev

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop

  h2:
    console:
      enabled: true

logging:
  level:
    dev.javacadabra: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# Sin seguridad para desarrollo
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### application-test.yml (Testing)

```yaml
spring:
  config:
    activate:
      on-profile: test

  datasource:
    url: jdbc:h2:mem:testdb
  
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: create-drop

camunda:
  client:
    mode: simple
    zeebe:
      gateway-address: localhost:26500

logging:
  level:
    dev.javacadabra: INFO
```

### application-prod.yml (Producción)

```yaml
spring:
  config:
    activate:
      on-profile: prod

  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

  h2:
    console:
      enabled: false

server:
  port: ${SERVER_PORT:8080}
  ssl:
    enabled: ${SSL_ENABLED:false}

camunda:
  client:
    zeebe:
      gateway-address: ${ZEEBE_ADDRESS}
    auth:
      username: ${CAMUNDA_USER}
      password: ${CAMUNDA_PASSWORD}

logging:
  level:
    dev.javacadabra: INFO
    io.camunda: WARN
  file:
    name: /var/log/app/application.log

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## 🌍 Variables de Entorno

### Archivo .env (Desarrollo)

```bash
# .env

# Camunda
ZEEBE_ADDRESS=localhost:26500
ZEEBE_REST=http://localhost:8080
CAMUNDA_USER=demo
CAMUNDA_PASSWORD=demo

# Puertos de Microservicios
CLIENTES_PORT=9080
VUELOS_PORT=9081
HOTELES_PORT=9082
COCHES_PORT=9083
PAGOS_PORT=9084
RESERVAS_PORT=9090

# Base de Datos (Producción)
DATABASE_URL=jdbc:postgresql://localhost:5432/reservas
DATABASE_USER=admin
DATABASE_PASSWORD=secure_password

# Logging
LOG_LEVEL=INFO

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
```

### Variables por Servicio

#### Servicio Clientes
```bash
export CLIENTES_PORT=9080
export CLIENTES_DB_URL=jdbc:h2:mem:clientes_db
export CLIENTES_LOG_LEVEL=DEBUG
```

#### Servicio Reservas
```bash
export RESERVAS_PORT=9090
export ZEEBE_ADDRESS=localhost:26500
export BPMN_AUTO_DEPLOY=true
```

---

## 🗄️ Configuración de Base de Datos

### H2 (Desarrollo)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:<nombre>_db
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: false
```

**Acceder a la consola**:
- URL: `http://localhost:90XX/h2-console`
- JDBC URL: `jdbc:h2:mem:<nombre>_db`
- User: `sa`
- Password: (vacío)

### PostgreSQL (Producción)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

# Flyway para migraciones
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### MongoDB (Opcional para Catálogos)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USER}:${MONGO_PASSWORD}@${MONGO_HOST}:27017/${MONGO_DB}
      database: ${MONGO_DB}
      
# Configuración adicional
  mongodb:
    embedded:
      version: 6.0.0
```

---

## 🐳 Configuración Docker

### docker-compose.yml (Microservicios)

```yaml
version: '3.8'

services:
  servicio-clientes:
    build: ./servicio-clientes
    container_name: servicio-clientes
    ports:
      - "9080:9080"
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
      - ZEEBE_ADDRESS=zeebe:26500
    depends_on:
      - zeebe
    networks:
      - app-network
    restart: unless-stopped

  servicio-vuelos:
    build: ./servicio-vuelos
    container_name: servicio-vuelos
    ports:
      - "9081:9081"
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
      - ZEEBE_ADDRESS=zeebe:26500
    depends_on:
      - zeebe
    networks:
      - app-network
    restart: unless-stopped

  # ... más servicios

networks:
  app-network:
    external: true
    name: camunda-network
```

### Dockerfile (Común)

```dockerfile
FROM eclipse-temurin:21-jre-alpine

# Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copiar JAR
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Exponer puerto
EXPOSE ${SERVER_PORT:-8080}

# Variables de entorno
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

---

## 🔐 Configuración de Seguridad

### application-secure.yml

```yaml
spring:
  security:
    user:
      name: ${ADMIN_USER}
      password: ${ADMIN_PASSWORD}

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized

server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
```

---

## 📊 Configuración de Monitoreo

### Prometheus

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:dev}
```

### Micrometer

```yaml
management:
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      service: ${spring.application.name}
```

---

## 🔍 Configuración de Logging

### logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <property name="LOG_FILE" value="${LOG_FILE:-${LOG_PATH:-${LOG_TEMP:-${java.io.tmpdir:-/tmp}}}/spring.log}"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
    
    <logger name="dev.javacadabra" level="DEBUG"/>
    <logger name="io.camunda.zeebe" level="INFO"/>
</configuration>
```

---

## 📚 Referencias

- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [Camunda Configuration](https://docs.camunda.io/docs/self-managed/platform-deployment/overview/)
- [Docker Compose](https://docs.docker.com/compose/)

---

**Última actualización**: Diciembre 2024
