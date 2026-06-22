#!/bin/bash
cd "$(dirname "$0")/.."  # Asegurar ejecución desde la raíz del proyecto
# ===========================================================
# Script: build-and-run.sh
# Autor: Vicente Priego
# Descripción:
#   🧱 Compila todos los microservicios Spring Boot
#   🐳 Construye las imágenes Docker
#   ⚙️  Levanta Camunda Platform primero
#   🔁 Espera hasta que Camunda esté listo
#   🚀 Despliega el Sistema de Reservas de Viaje completo
# ===========================================================

# -------------------------------
# 🎨 COLORES Y EMOJIS
# -------------------------------
GREEN="\e[32m"
RED="\e[31m"
YELLOW="\e[33m"
CYAN="\e[36m"
RESET="\e[0m"
CHECK="${GREEN}✅${RESET}"
CROSS="${RED}❌${RESET}"
WAIT="${YELLOW}⏳${RESET}"
INFO="${CYAN}ℹ️${RESET}"

echo -e "${CYAN}=============================================="
echo -e "     🌍 SISTEMA DE RESERVAS DE VIAJES v1.0"
echo -e "==============================================${RESET}"

# -------------------------------
# 🧰 VERIFICACIÓN DE DEPENDENCIAS
# -------------------------------
echo -e "${INFO} Verificando dependencias..."

if ! command -v mvn &> /dev/null; then
  echo -e "${CROSS} Maven no está instalado. Instálalo y vuelve a intentarlo."
  exit 1
fi

if ! command -v docker &> /dev/null; then
  echo -e "${CROSS} Docker no está instalado o no está en PATH."
  exit 1
fi

if ! docker compose version &> /dev/null; then
  echo -e "${CROSS} docker compose no está disponible. Asegúrate de usar Docker Desktop o Docker CLI 2.x."
  exit 1
fi
echo -e "${CHECK} Dependencias verificadas correctamente."

# -------------------------------
# 🔧 COMPILACIÓN CON MAVEN
# -------------------------------
echo -e "\n${YELLOW}🚧 Compilando todos los microservicios con Maven...${RESET}"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
  echo -e "${CROSS} Falló la compilación. Corrige los errores y vuelve a intentarlo."
  exit 1
fi
echo -e "${CHECK} Compilación completada correctamente."

# -------------------------------
# 🌐 CREAR RED DE CAMUNDA (si no existe)
# -------------------------------
NETWORK_NAME="camunda-network"

if ! docker network ls | grep -q "$NETWORK_NAME"; then
  echo -e "\n${YELLOW}🌐 Creando red Docker: $NETWORK_NAME ...${RESET}"
  docker network create "$NETWORK_NAME"
  echo -e "${CHECK} Red '$NETWORK_NAME' creada."
else
  echo -e "${INFO} Red '$NETWORK_NAME' ya existe. Continuando..."
fi

# -------------------------------
# ⚙️  LEVANTAR CAMUNDA PRIMERO
# -------------------------------
echo -e "\n${YELLOW}🐪 Iniciando Camunda Platform (docker-compose-camunda.yml)...${RESET}"
docker compose -f docker-compose-camunda.yml up -d

if [ $? -ne 0 ]; then
  echo -e "${CROSS} Error al iniciar Camunda. Revisa tu archivo docker-compose-camunda.yml."
  exit 1
fi
echo -e "${WAIT} Esperando a que Camunda esté saludable..."

# -------------------------------
# 🕒 ESPERAR A CAMUNDA (salud HTTP)
# -------------------------------
CAMUNDA_URL="http://localhost:9600/actuator/health/status"

until curl -fs "$CAMUNDA_URL" >/dev/null 2>&1; do
    echo -e "${WAIT} Camunda aún no está lista... reintentando en 10s"
    sleep 10
done

echo -e "${CHECK} Camunda está lista y saludable ✔️"

# -------------------------------
# 🐳 CONSTRUIR IMÁGENES DE LOS SERVICIOS
# -------------------------------
echo -e "\n${YELLOW}🔨 Construyendo imágenes Docker de los microservicios...${RESET}"
docker compose build

if [ $? -ne 0 ]; then
  echo -e "${CROSS} Falló la construcción de las imágenes Docker."
  exit 1
fi
echo -e "${CHECK} Imágenes Docker construidas correctamente."

# -------------------------------
# 🚀 LEVANTAR SISTEMA COMPLETO
# -------------------------------
echo -e "\n${YELLOW}🚀 Iniciando todos los servicios del Sistema de Reservas...${RESET}"
docker compose up -d

if [ $? -eq 0 ]; then
  echo -e "${CHECK} Sistema de Reservas de Viajes iniciado correctamente."
  echo -e "${CYAN}🌐 Puertos disponibles:${RESET}"
  echo -e "  🧍‍♂️ Clientes:      http://localhost:9080"
  echo -e "  ✈️ Vuelos:        http://localhost:9081"
  echo -e "  🏨 Hoteles:        http://localhost:9082"
  echo -e "  🚗 Alquiler Coches: http://localhost:9083"
  echo -e "  💳 Pagos:          http://localhost:9084"
  echo -e "  📋 Reservas:       http://localhost:9090"
  echo -e "  🧠 Operate:        http://localhost:8080/operate"
  echo -e "  📋 Tasklist:       http://localhost:8080/tasklist"
else
  echo -e "${CROSS} Ocurrió un error al iniciar los contenedores."
  exit 1
fi

echo -e "\n${CHECK} Sistema completamente operativo."