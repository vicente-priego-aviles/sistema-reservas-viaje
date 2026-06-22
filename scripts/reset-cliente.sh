#!/bin/bash
# ===========================================================
# Script: reset-cliente.sh
# Descripción: Cancela instancias BPMN activas del cliente y
# resetea su estado a ACTIVO.
#
# Orden de operaciones:
#   1. Busca instancias activas de 'proceso-principal' en Operate
#   2. Las cancela vía Zeebe REST API
#   3. Resetea el estado del cliente a ACTIVO en la BD
#
# Uso:
#   ./reset-cliente.sh              → lista clientes atascados y resetea
#   ./reset-cliente.sh <clienteId>  → resetea directamente ese cliente
# ===========================================================

GREEN="\e[32m"
YELLOW="\e[33m"
RED="\e[31m"
CYAN="\e[36m"
RESET="\e[0m"

BASE_URL="http://localhost:9080"
OPERATE_URL="http://localhost:8081"   # Operate corre en 8081 (docker: 8081->8080)
ZEEBE_REST_URL="http://localhost:8088"
OPERATE_USER="demo"
OPERATE_PASS="demo"
OPERATE_COOKIE="/tmp/reset-operate-cookie.txt"

# Obtiene cookie de sesión de Operate (necesaria para su REST API)
operate_login() {
  HTTP=$(curl -s -c "${OPERATE_COOKIE}" \
    -o /dev/null -w "%{http_code}" \
    -X POST "${OPERATE_URL}/api/login?username=${OPERATE_USER}&password=${OPERATE_PASS}")
  [ "$HTTP" = "204" ] || [ "$HTTP" = "200" ]
}

# Cancela todas las instancias activas de 'proceso-principal' que tengan
# clienteId == <id>. Evita que workers de Zeebe sobreescriban el reset.
cancelar_procesos_activos() {
  local clienteId="$1"

  echo -e "${CYAN}🔍 Buscando instancias activas en Operate para cliente ${clienteId}...${RESET}"

  if ! operate_login; then
    echo -e "${YELLOW}⚠️  No se pudo conectar a Operate (${OPERATE_URL}). Continuando solo con reset de BD.${RESET}"
    return
  fi

  # Buscar variables 'clienteId' con ese valor. El valor va entre comillas (JSON string)
  VARS_JSON=$(curl -s -b "${OPERATE_COOKIE}" \
    -X POST "${OPERATE_URL}/v1/variables/search" \
    -H "Content-Type: application/json" \
    -d "{\"filter\": {\"name\": \"clienteId\", \"value\": \"\\\"${clienteId}\\\"\"}}")

  # Extraer processInstanceKey únicos (pueden aparecer duplicados por subprocesos)
  INSTANCE_KEYS=$(echo "$VARS_JSON" | \
    python3 -c "
import json, sys
data = json.load(sys.stdin)
items = data if isinstance(data, list) else data.get('items', [])
keys = sorted(set(str(v['processInstanceKey']) for v in items if 'processInstanceKey' in v))
print('\n'.join(keys))
" 2>/dev/null)

  if [ -z "$INSTANCE_KEYS" ]; then
    echo -e "${GREEN}✅ No se encontraron instancias de proceso para este cliente${RESET}"
    return
  fi

  COUNT=$(echo "$INSTANCE_KEYS" | grep -c '[0-9]')
  echo -e "${YELLOW}⚠️  Cancelando ${COUNT} instancia(s)...${RESET}"

  while IFS= read -r key; do
    [ -z "$key" ] && continue
    HTTP=$(curl -s -o /dev/null -w "%{http_code}" \
      -X POST "${ZEEBE_REST_URL}/v2/process-instances/${key}/cancellation")
    case "$HTTP" in
      200|202|204)
        echo -e "   ${GREEN}✅ Instancia ${key} cancelada${RESET}" ;;
      404)
        echo -e "   ${CYAN}ℹ️  Instancia ${key} ya finalizada${RESET}" ;;
      *)
        echo -e "   ${YELLOW}⚠️  Instancia ${key}: HTTP ${HTTP}${RESET}" ;;
    esac
  done <<< "$INSTANCE_KEYS"

  sleep 2
}

reset_cliente() {
  local id="$1"
  echo ""
  echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
  echo -e "${YELLOW}🔧 Reseteando cliente: ${id}${RESET}"
  echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

  cancelar_procesos_activos "$id"

  echo -e "${YELLOW}🔄 Reseteando estado del cliente a ACTIVO...${RESET}"

  HTTP=$(curl -s -o /tmp/reset-cliente-response.json -w "%{http_code}" \
    -X POST "${BASE_URL}/dev/clientes/${id}/reset-estado")

  if [ "$HTTP" = "200" ]; then
    ESTADO_ANTERIOR=$(python3 -c "
import json
d = json.load(open('/tmp/reset-cliente-response.json'))
print(d.get('estadoAnterior', '?'))
" 2>/dev/null)
    echo -e "${GREEN}✅ Estado reseteado a ACTIVO (era: ${ESTADO_ANTERIOR})${RESET}"
  elif [ "$HTTP" = "404" ]; then
    echo -e "${RED}❌ Cliente no encontrado: ${id}${RESET}"
    exit 1
  else
    echo -e "${RED}❌ Error inesperado (HTTP ${HTTP}):${RESET}"
    cat /tmp/reset-cliente-response.json
    exit 1
  fi
}

# Devuelve los clientes atascados como lista de líneas "id|nombre|email"
listar_atascados_parsed() {
  HTTP=$(curl -s -o /tmp/atascados.json -w "%{http_code}" \
    "${BASE_URL}/dev/clientes/atascados")
  echo "$HTTP"
}

# ── Con argumento: reset directo ──────────────────────────────────────────────
if [ -n "$1" ]; then
  reset_cliente "$1"
  exit 0
fi

# ── Sin argumento: listar y seleccionar ──────────────────────────────────────
echo -e "${CYAN}🔍 Buscando clientes con estado no ACTIVO...${RESET}"

HTTP_CODE=$(listar_atascados_parsed)

if [ "$HTTP_CODE" != "200" ]; then
  echo -e "${RED}❌ No se pudo conectar al servicio (HTTP ${HTTP_CODE})${RESET}"
  exit 1
fi

# Parsear JSON con python3 (Map.of() no garantiza orden de campos)
CLIENTES_INFO=$(python3 -c "
import json
data = json.load(open('/tmp/atascados.json'))
for c in data:
    print(c['clienteId'] + '|' + c['nombre'] + '|' + c['email'])
" 2>/dev/null)

if [ -z "$CLIENTES_INFO" ]; then
  echo -e "${GREEN}✅ Todos los clientes están en estado ACTIVO${RESET}"
  exit 0
fi

echo ""
echo -e "${YELLOW}Clientes con estado no ACTIVO:${RESET}"
echo "──────────────────────────────────────────────────────────────"

IDS=()
i=1
while IFS='|' read -r id nombre email; do
  echo -e "  ${CYAN}[$i]${RESET} ${nombre} (${email})"
  echo -e "       ID: ${id}"
  IDS+=("$id")
  ((i++))
done <<< "$CLIENTES_INFO"

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
