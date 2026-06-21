#!/bin/bash
cd "$(dirname "$0")/.."  # Asegurar ejecución desde la raíz del proyecto
# ===========================================================
# Script: stop.sh
# Descripción: Para los microservicios (Camunda sigue corriendo)
# ===========================================================

GREEN="\e[32m"
YELLOW="\e[33m"
RESET="\e[0m"

echo -e "${YELLOW}🛑 Parando microservicios...${RESET}"
docker-compose down

echo -e "${GREEN}✅ Microservicios parados. Camunda sigue activo.${RESET}"
echo -e "${GREEN}   Para volver a levantarlos: ./scripts/start.sh${RESET}"
