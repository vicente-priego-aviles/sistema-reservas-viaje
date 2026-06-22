#!/bin/bash
cd "$(dirname "$0")/.."  # Asegurar ejecución desde la raíz del proyecto
# ===========================================================
# Script: build-all.sh
# Autor: Vicente Priego
# Descripción:
#   - Compila todos los microservicios del sistema de reservas
#   - Construye sus imágenes Docker
#   - Levanta todo el entorno con docker compose
# ===========================================================

# Colores para mensajes
GREEN="\e[32m"
RED="\e[31m"
YELLOW="\e[33m"
RESET="\e[0m"

echo -e "${GREEN}🔧 Iniciando build del Sistema de Reservas de Viajes...${RESET}"

# Verificar que Maven esté instalado
if ! command -v mvn &> /dev/null; then
  echo -e "${RED}❌ Maven no está instalado. Instálalo y vuelve a intentarlo.${RESET}"
  exit 1
fi

# Verificar que Docker esté instalado
if ! command -v docker &> /dev/null; then
  echo -e "${RED}❌ Docker no está instalado o no está en PATH.${RESET}"
  exit 1
fi

# Verificar que docker compose esté disponible
if ! docker compose version &> /dev/null; then
  echo -e "${RED}❌ docker compose no está disponible. Asegúrate de usar Docker Desktop o el nuevo CLI.${RESET}"
  exit 1
fi

echo -e "${YELLOW}🚧 Compilando todos los microservicios con Maven...${RESET}"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Falló la compilación con Maven. Corrige los errores antes de continuar.${RESET}"
  exit 1
fi

echo -e "${GREEN}✅ Compilación completada correctamente.${RESET}"

# Construir imágenes Docker
echo -e "${YELLOW}🐳 Construyendo imágenes Docker para todos los servicios...${RESET}"
docker compose build

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Falló la construcción de imágenes Docker.${RESET}"
  exit 1
fi

echo -e "${GREEN}✅ Imágenes Docker construidas correctamente.${RESET}"

# Levantar Camunda primero (crea la red camunda-platform que necesitan los microservicios)
echo -e "${YELLOW}🚀 Levantando Camunda Platform...${RESET}"
docker compose -f docker-compose-camunda.yml up -d

echo -e "${YELLOW}⏳ Esperando a que Camunda esté listo...${RESET}"
until curl -sf http://localhost:9600/actuator/health/readiness >/dev/null 2>&1; do
  echo "   Camunda no está listo aún, reintentando en 10s..."
  sleep 10
done
echo -e "${GREEN}✅ Camunda listo.${RESET}"

# Levantar los microservicios
echo -e "${YELLOW}🚀 Iniciando los microservicios con docker compose...${RESET}"
docker compose up -d

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Sistema de Reservas levantado correctamente.${RESET}"
  echo -e "${GREEN}🌐 Microservicios disponibles en los siguientes puertos:${RESET}"
  echo -e "  - Clientes: http://localhost:9080"
  echo -e "  - Vuelos:   http://localhost:9081"
  echo -e "  - Hoteles:  http://localhost:9082"
  echo -e "  - Coches:   http://localhost:9083"
  echo -e "  - Pagos:    http://localhost:9084"
  echo -e "  - Reservas: http://localhost:9090"
else
  echo -e "${RED}❌ Ocurrió un error al iniciar los contenedores.${RESET}"
  exit 1
fi
