#!/bin/bash
cd "$(dirname "$0")/.."  # Asegurar ejecución desde la raíz del proyecto
# ===========================================================
# Script: stop-all.sh
# Descripción: Para microservicios y Camunda Platform completo
# ===========================================================

GREEN="\e[32m"
YELLOW="\e[33m"
RESET="\e[0m"

echo -e "${YELLOW}🛑 Parando microservicios...${RESET}"
docker-compose down

echo -e "${YELLOW}🛑 Parando Camunda Platform...${RESET}"
docker-compose -f docker-compose-camunda.yml down

echo -e "${GREEN}✅ Sistema completamente parado.${RESET}"
echo -e "${GREEN}   Para volver a levantar todo: ./scripts/build-and-run.sh${RESET}"
