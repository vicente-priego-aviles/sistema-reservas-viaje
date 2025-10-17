# 📦 Guía de Instalación Completa

Esta guía proporciona instrucciones detalladas para instalar y configurar el Sistema de Reservas de Viaje en diferentes entornos.

---

## 📋 Tabla de Contenidos

- [Requisitos del Sistema](#-requisitos-del-sistema)
- [Instalación en Desarrollo](#-instalación-en-desarrollo)
- [Instalación en Producción](#-instalación-en-producción)
- [Verificación de la Instalación](#-verificación-de-la-instalación)
- [Troubleshooting](#-troubleshooting)

---

## 💻 Requisitos del Sistema

### Requisitos Mínimos

| Componente | Mínimo | Recomendado |
|-----------|--------|-------------|
| CPU | 4 cores | 8 cores |
| RAM | 8 GB | 16 GB |
| Disco | 20 GB | 50 GB |
| OS | Linux/macOS/Windows | Linux |

### Software Requerido

#### 1. Java 21

**Linux (Ubuntu/Debian)**:
```bash
# Instalar OpenJDK 21
sudo apt update
sudo apt install openjdk-21-jdk

# Verificar
java -version
```

**macOS**:
```bash
# Con Homebrew
brew install openjdk@21

# Agregar al PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Verificar
java -version
```

**Windows**:
1. Descargar desde [Adoptium](https://adoptium.net/)
2. Ejecutar instalador
3. Configurar JAVA_HOME:
   ```cmd
   setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.x.x"
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```

#### 2. Maven 3.9+

**Linux**:
```bash
sudo apt install maven

# Verificar
mvn -version
```

**macOS**:
```bash
brew install maven

# Verificar
mvn -version
```

**Windows**:
1. Descargar desde [Apache Maven](https://maven.apache.org/download.cgi)
2. Extraer en `C:\Program Files\Maven`
3. Configurar PATH:
   ```cmd
   setx M2_HOME "C:\Program Files\Maven\apache-maven-3.9.x"
   setx PATH "%PATH%;%M2_HOME%\bin"
   ```

#### 3. Docker y Docker Compose

**Linux (Ubuntu)**:
```bash
# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Añadir usuario al grupo docker
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo apt install docker-compose-plugin

# Verificar
docker --version
docker compose version
```

**macOS**:
```bash
# Instalar Docker Desktop
brew install --cask docker

# Iniciar Docker Desktop desde aplicaciones
# Verificar
docker --version
docker compose version
```

**Windows**:
1. Descargar [Docker Desktop](https://www.docker.com/products/docker-desktop)
2. Ejecutar instalador
3. Reiniciar sistema
4. Verificar en PowerShell:
   ```powershell
   docker --version
   docker compose version
   ```

#### 4. Git

**Linux**:
```bash
sudo apt install git
```

**macOS**:
```bash
brew install git
```

**Windows**:
Descargar desde [git-scm.com](https://git-scm.com/)

---

## 🛠️ Instalación en Desarrollo

### Paso 1: Clonar el Repositorio

```bash
# Clonar
git clone https://github.com/tu-usuario/sistema-reservas-viaje.git
cd sistema-reservas-viaje

# Verificar estructura
ls -la
```

### Paso 2: Configurar Variables de Entorno (Opcional)

Crea un archivo `.env` en la raíz:

```bash
# .env
CAMUNDA_ZEEBE_ADDRESS=localhost:26500
CAMUNDA_OPERATE_URL=http://localhost:8080
CAMUNDA_TASKLIST_URL=http://localhost:8081

# Puertos de microservicios
CLIENTES_PORT=9080
VUELOS_PORT=9081
HOTELES_PORT=9082
COCHES_PORT=9083
PAGOS_PORT=9084
RESERVAS_PORT=9090
```

### Paso 3: Compilar el Proyecto

```bash
# Compilar todos los módulos
mvn clean install

# Saltar tests para compilación rápida
mvn clean install -DskipTests

# Verificar compilación exitosa
echo $?  # Debe retornar 0
```

**Salida esperada**:
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] sistema-reservas-viaje-parent ...................... SUCCESS [  0.123 s]
[INFO] servicio-clientes .................................. SUCCESS [ 12.456 s]
[INFO] servicio-vuelos .................................... SUCCESS [ 10.234 s]
[INFO] servicio-hoteles ................................... SUCCESS [  9.876 s]
[INFO] servicio-alquiler-coches ........................... SUCCESS [  8.654 s]
[INFO] servicio-pagos ..................................... SUCCESS [ 11.234 s]
[INFO] servicio-reservas .................................. SUCCESS [ 13.567 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Paso 4: Levantar Camunda Platform 8

```bash
# Levantar solo Camunda
docker-compose -f docker-compose-camunda.yml up -d

# Ver logs
docker-compose -f docker-compose-camunda.yml logs -f

# Esperar a que esté listo (60 segundos aproximadamente)
```

**Verificar Camunda**:
```bash
# Verificar Zeebe
docker-compose -f docker-compose-camunda.yml ps

# Acceder a Operate
curl http://localhost:8080
# Debería retornar HTML

# Acceder a Tasklist
curl http://localhost:8081
# Debería retornar HTML
```

### Paso 5: Levantar Microservicios

```bash
# Construir imágenes Docker
docker-compose build

# Levantar servicios
docker-compose up -d

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f servicio-clientes
```

### Paso 6: Usar el Script Automatizado

```bash
# Dar permisos de ejecución
chmod +x start.sh

# Ejecutar script completo
./start.sh
```

**El script realiza**:
1. ✅ Levanta Camunda Platform 8
2. ⏳ Espera 60 segundos
3. ✅ Construye y levanta microservicios
4. ✅ Muestra URLs de acceso

---

## 🏭 Instalación en Producción

### Consideraciones de Producción

- ✅ Usar base de datos persistente (PostgreSQL/MongoDB)
- ✅ Configurar HTTPS
- ✅ Implementar API Gateway
- ✅ Configurar secrets management
- ✅ Implementar monitoreo y alertas
- ✅ Configurar backups automáticos

### Opción 1: Docker Compose (Producción Ligera)

#### Crear docker-compose.prod.yml

```yaml
version: '3.8'

services:
  servicio-clientes:
    build: ./servicio-clientes
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/clientes
      - CAMUNDA_ZEEBE_ADDRESS=zeebe:26500
    restart: always
    networks:
      - app-network
    depends_on:
      - db
      - zeebe

  # ... más servicios
  
  db:
    image: postgres:16
    environment:
      - POSTGRES_DB=reservas
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - app-network

volumes:
  postgres-data:

networks:
  app-network:
    driver: bridge
```

#### Desplegar

```bash
# Cargar variables de entorno
export DB_PASSWORD=tu_password_seguro

# Levantar
docker-compose -f docker-compose.prod.yml up -d

# Verificar
docker-compose -f docker-compose.prod.yml ps
```

### Opción 2: Kubernetes (Recomendado para Producción)

Ver [docs/08-deployment.md](08-deployment.md) para instrucciones detalladas de Kubernetes.

---

## ✅ Verificación de la Instalación

### 1. Verificar Contenedores

```bash
# Listar todos los contenedores
docker ps

# Verificar estado
docker-compose ps
```

**Salida esperada**:
```
NAME                          STATUS
camunda-zeebe                 Up 2 minutes
camunda-operate               Up 2 minutes
camunda-tasklist              Up 2 minutes
servicio-clientes             Up 1 minute
servicio-vuelos               Up 1 minute
servicio-hoteles              Up 1 minute
servicio-alquiler-coches      Up 1 minute
servicio-pagos                Up 1 minute
servicio-reservas             Up 1 minute
```

### 2. Health Checks

```bash
# Script de verificación completa
#!/bin/bash

echo "🔍 Verificando health checks..."

services=(
  "clientes:9080"
  "vuelos:9081"
  "hoteles:9082"
  "coches:9083"
  "pagos:9084"
  "reservas:9090"
)

for service in "${services[@]}"; do
  name="${service%%:*}"
  port="${service##*:}"
  
  echo -n "Verificando $name... "
  
  response=$(curl -s -o /dev/null -w "%{http_code}" \
    http://localhost:$port/actuator/health)
  
  if [ "$response" = "200" ]; then
    echo "✅ OK"
  else
    echo "❌ FAIL (HTTP $response)"
  fi
done

echo ""
echo "🔍 Verificando Camunda..."

echo -n "Verificando Operate... "
if curl -s http://localhost:8080 > /dev/null; then
  echo "✅ OK"
else
  echo "❌ FAIL"
fi

echo -n "Verificando Tasklist... "
if curl -s http://localhost:8081 > /dev/null; then
  echo "✅ OK"
else
  echo "❌ FAIL"
fi
```

Guardar como `verify.sh` y ejecutar:

```bash
chmod +x verify.sh
./verify.sh
```

### 3. Verificar Conexión a Zeebe

```bash
# Verificar logs del coordinador
docker-compose logs servicio-reservas | grep -i "zeebe"

# Debería mostrar:
# ✅ Connected to Zeebe at localhost:26500
```

### 4. Prueba End-to-End

```bash
# Iniciar una reserva de prueba
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI-001",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2025-12-01",
    "fechaFin": "2025-12-05",
    "monto": 1500
  }'

# Verificar en Camunda Operate
# http://localhost:8080
```

### 5. Verificar Base de Datos H2

```bash
# Acceder a consola H2 de clientes
docker exec -it servicio-clientes curl http://localhost:9080/h2-console

# O desde navegador:
# http://localhost:9080/h2-console
# JDBC URL: jdbc:h2:mem:clientes_db
# User: sa
# Password: (vacío)
```

---

## 🔧 Troubleshooting

### Problema: Puerto ya en uso

**Síntoma**:
```
Error: bind: address already in use
```

**Solución**:
```bash
# Encontrar proceso usando el puerto
sudo lsof -i :8080  # Linux/macOS
netstat -ano | findstr :8080  # Windows

# Matar proceso
kill -9 <PID>  # Linux/macOS
taskkill /PID <PID> /F  # Windows

# O cambiar puerto en application.yml
```

### Problema: Camunda no arranca

**Síntoma**:
```
Zeebe broker is not ready
```

**Solución**:
```bash
# Ver logs detallados
docker-compose -f docker-compose-camunda.yml logs zeebe

# Verificar recursos
docker stats

# Aumentar memoria de Docker Desktop
# Settings > Resources > Memory: 8GB mínimo

# Reiniciar Camunda
docker-compose -f docker-compose-camunda.yml restart
```

### Problema: Microservicio no se conecta a Zeebe

**Síntoma**:
```
Failed to connect to Zeebe gateway
```

**Solución**:
```bash
# Verificar que Zeebe esté escuchando
docker-compose -f docker-compose-camunda.yml ps zeebe

# Verificar red Docker
docker network ls
docker network inspect sistema-reservas-viaje_default

# Verificar configuración
cat servicio-reservas/src/main/resources/application.yml | grep zeebe

# Reintentar conexión
docker-compose restart servicio-reservas
```

### Problema: Compilación Maven falla

**Síntoma**:
```
[ERROR] Failed to execute goal
```

**Solución**:
```bash
# Limpiar cache de Maven
mvn clean

# Eliminar .m2/repository y recompilar
rm -rf ~/.m2/repository
mvn clean install

# Verificar versión de Java
java -version
# Debe ser Java 21

# Compilar sin tests
mvn clean install -DskipTests
```

### Problema: Docker out of space

**Síntoma**:
```
no space left on device
```

**Solución**:
```bash
# Limpiar imágenes no usadas
docker system prune -a

# Limpiar volúmenes
docker volume prune

# Ver uso de espacio
docker system df

# Eliminar todo y reconstruir
docker-compose down -v
docker system prune -a --volumes
./start.sh
```

### Problema: Tests fallan

**Síntoma**:
```
Tests run: 10, Failures: 2
```

**Solución**:
```bash
# Ejecutar tests con más información
mvn test -X

# Ejecutar solo un test
mvn test -Dtest=ClienteServicioTest

# Saltar tests temporalmente
mvn install -DskipTests

# Verificar que H2 esté disponible
mvn dependency:tree | grep h2
```

---

## 🔐 Configuración de Seguridad (Producción)

### 1. Configurar HTTPS

```yaml
# application-prod.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### 2. Configurar Secrets

```bash
# Usar variables de entorno
export DB_PASSWORD=$(openssl rand -base64 32)
export JWT_SECRET=$(openssl rand -base64 64)

# O usar Docker secrets
echo "mi_password_seguro" | docker secret create db_password -
```

### 3. Configurar Firewall

```bash
# Permitir solo puertos necesarios
sudo ufw allow 8080/tcp  # Operate
sudo ufw allow 8081/tcp  # Tasklist
sudo ufw deny 26500/tcp  # Zeebe (solo interno)
```

---

## 📊 Monitoreo Post-Instalación

### Verificar Métricas

```bash
# Actuator endpoints
curl http://localhost:9080/actuator/metrics
curl http://localhost:9080/actuator/health/liveness
curl http://localhost:9080/actuator/health/readiness
```

### Verificar Logs

```bash
# Seguir logs en tiempo real
docker-compose logs -f --tail=100

# Buscar errores
docker-compose logs | grep ERROR

# Exportar logs
docker-compose logs > logs-$(date +%Y%m%d).txt
```

---

## 🎯 Próximos Pasos

Después de una instalación exitosa:

1. 📖 Lee la [Guía de Configuración](04-configuracion.md)
2. 🧪 Ejecuta los [Tests](07-testing.md)
3. 📊 Explora [Procesos BPMN](05-procesos-bpmn.md)
4. 💡 Revisa [Casos de Uso](casos-uso.md)

---

**Última actualización**: Diciembre 2024