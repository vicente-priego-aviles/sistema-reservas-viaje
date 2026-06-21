#!/bin/bash
# ===========================================================
# Script: reset-cliente.sh
# Descripción: Resetea el estado de un cliente a ACTIVO.
# Útil cuando un proceso BPMN se cancela a mitad del flujo y
# el cliente queda bloqueado en EN_PROCESO_RESERVA.
#
# Uso:
#   ./reset-cliente.sh              → lista clientes atascados y resetea el primero
#   ./reset-cliente.sh <clienteId>  → resetea directamente ese cliente
# ===========================================================

GREEN="\e[32m"
YELLOW="\e[33m"
RED="\e[31m"
CYAN="\e[36m"
RESET="\e[0m"

BASE_URL="http://localhost:9080"

reset_cliente() {
  local id="$1"
  echo -e "${YELLOW}🔧 Reseteando estado del cliente ${id}...${RESET}"

  RESPONSE=$(curl -s -o /tmp/reset-cliente-response.json -w "%{http_code}" \
    -X POST "${BASE_URL}/dev/clientes/${id}/reset-estado")

  if [ "$RESPONSE" = "200" ]; then
    ESTADO_ANTERIOR=$(grep -o '"estadoAnterior":"[^"]*"' /tmp/reset-cliente-response.json | cut -d'"' -f4)
    echo -e "${GREEN}✅ Estado reseteado a ACTIVO (era: ${ESTADO_ANTERIOR})${RESET}"
  elif [ "$RESPONSE" = "404" ]; then
    echo -e "${RED}❌ Cliente no encontrado: ${id}${RESET}"
    exit 1
  else
    echo -e "${RED}❌ Error inesperado (HTTP ${RESPONSE}):${RESET}"
    cat /tmp/reset-cliente-response.json
    exit 1
  fi
}

listar_atascados() {
  curl -s "${BASE_URL}/dev/clientes/atascados" -o /tmp/atascados.json -w "%{http_code}"
}

# ── Con argumento: reset directo ──────────────────────────────────────────────
if [ -n "$1" ]; then
  reset_cliente "$1"
  exit 0
fi

# ── Sin argumento: listar y seleccionar ──────────────────────────────────────
echo -e "${CYAN}🔍 Buscando clientes atascados en EN_PROCESO_RESERVA...${RESET}"

HTTP_CODE=$(listar_atascados)

if [ "$HTTP_CODE" != "200" ]; then
  echo -e "${RED}❌ No se pudo conectar al servicio (HTTP ${HTTP_CODE})${RESET}"
  exit 1
fi

# Extraer IDs y nombres usando grep básico
CLIENTES=$(grep -o '"clienteId":"[^"]*","nombre":"[^"]*","email":"[^"]*"' /tmp/atascados.json)

if [ -z "$CLIENTES" ] || [ "$(cat /tmp/atascados.json)" = "[]" ]; then
  echo -e "${GREEN}✅ No hay clientes atascados en EN_PROCESO_RESERVA${RESET}"
  exit 0
fi

echo ""
echo -e "${YELLOW}Clientes atascados en EN_PROCESO_RESERVA:${RESET}"
echo "──────────────────────────────────────────────────────────────"

# Parsear y mostrar lista numerada
IDS=()
i=1
while IFS= read -r linea; do
  ID=$(echo "$linea" | grep -o '"clienteId":"[^"]*"' | cut -d'"' -f4)
  NOMBRE=$(echo "$linea" | grep -o '"nombre":"[^"]*"' | cut -d'"' -f4)
  EMAIL=$(echo "$linea" | grep -o '"email":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$ID" ]; then
    echo -e "  ${CYAN}[$i]${RESET} ${NOMBRE} (${EMAIL})"
    echo -e "       ID: ${ID}"
    IDS+=("$ID")
    ((i++))
  fi
done < <(cat /tmp/atascados.json | grep -o '{[^}]*}')

echo "──────────────────────────────────────────────────────────────"
echo ""

if [ "${#IDS[@]}" -eq 1 ]; then
  echo -e "${YELLOW}Solo hay 1 cliente atascado. Reseteando automáticamente...${RESET}"
  reset_cliente "${IDS[0]}"
else
  echo -n "Selecciona el número del cliente a resetear (o 'a' para resetear todos): "
  read -r SELECCION

  if [ "$SELECCION" = "a" ]; then
    for id in "${IDS[@]}"; do
      reset_cliente "$id"
    done
  elif [[ "$SELECCION" =~ ^[0-9]+$ ]] && [ "$SELECCION" -ge 1 ] && [ "$SELECCION" -le "${#IDS[@]}" ]; then
    reset_cliente "${IDS[$((SELECCION - 1))]}"
  else
    echo -e "${RED}❌ Selección inválida${RESET}"
    exit 1
  fi
fi
