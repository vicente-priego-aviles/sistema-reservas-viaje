#!/bin/bash
# ===========================================================
# Script: reset-cliente.sh
# Descripción: Resetea el estado de un cliente a ACTIVO.
# Útil cuando un proceso BPMN se cancela a mitad del flujo y
# el cliente queda bloqueado en EN_PROCESO_RESERVA.
# ===========================================================

GREEN="\e[32m"
YELLOW="\e[33m"
RED="\e[31m"
RESET="\e[0m"

# Cliente de prueba por defecto (Juan Pérez García)
DEFAULT_ID="123e4567-e89b-12d3-a456-426655440000"
CLIENTE_ID="${1:-$DEFAULT_ID}"

echo -e "${YELLOW}🔧 Reseteando estado del cliente ${CLIENTE_ID}...${RESET}"

RESPONSE=$(curl -s -o /tmp/reset-cliente-response.json -w "%{http_code}" \
  -X POST "http://localhost:9080/dev/clientes/${CLIENTE_ID}/reset-estado")

if [ "$RESPONSE" = "200" ]; then
  ESTADO_ANTERIOR=$(cat /tmp/reset-cliente-response.json | grep -o '"estadoAnterior":"[^"]*"' | cut -d'"' -f4)
  echo -e "${GREEN}✅ Estado reseteado a ACTIVO (era: ${ESTADO_ANTERIOR})${RESET}"
elif [ "$RESPONSE" = "404" ]; then
  echo -e "${RED}❌ Cliente no encontrado: ${CLIENTE_ID}${RESET}"
  exit 1
else
  echo -e "${RED}❌ Error inesperado (HTTP ${RESPONSE}):${RESET}"
  cat /tmp/reset-cliente-response.json
  exit 1
fi
