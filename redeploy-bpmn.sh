#!/bin/bash
# ===========================================================
# Script: redeploy-bpmn.sh
# Descripción:
#   Recompila y redespliega servicio-reservas para que Zeebe
#   reciba los BPMN actualizados en servicio-reservas/src/main/resources/bpmn/
#
#   Cuándo usarlo:
#     - Tras modificar cualquier archivo BPMN
#     - El resto de microservicios NO se reconstruyen
#     - Camunda debe estar corriendo
# ===========================================================

GREEN="\e[32m"
RED="\e[31m"
YELLOW="\e[33m"
CYAN="\e[36m"
RESET="\e[0m"

echo -e "${CYAN}🔄 Redesplegando BPMN en Zeebe...${RESET}"
echo ""

# Verificar que Maven esté instalado
if ! command -v mvn &> /dev/null; then
  echo -e "${RED}❌ Maven no está instalado.${RESET}"
  exit 1
fi

# Verificar que Zeebe esté corriendo
if ! docker ps --filter "name=zeebe" --filter "status=running" | grep -q zeebe; then
  echo -e "${RED}❌ Zeebe no está corriendo. Levanta Camunda primero con ./start.sh o ./build-and-run.sh${RESET}"
  exit 1
fi

# Sincronizar copias del Modeler → runtime
echo -e "${YELLOW}📂 Sincronizando archivos BPMN al runtime...${RESET}"
BPMN_SRC="bpmn"
BPMN_DST="servicio-reservas/src/main/resources/bpmn"
for f in "$BPMN_SRC"/*.bpmn; do
  fname=$(basename "$f")
  if [ -f "$BPMN_DST/$fname" ]; then
    cat "$f" > "$BPMN_DST/$fname"
    echo -e "  → $fname"
  fi
done
echo -e "${GREEN}✅ Sincronización completada.${RESET}"

# Compilación completa (necesario para que MapStruct genere correctamente los mapeadores)
echo -e "${YELLOW}🔧 Compilando todos los módulos Maven...${RESET}"
mvn clean package -DskipTests -q

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Falló la compilación. Corrige los errores antes de continuar.${RESET}"
  exit 1
fi
echo -e "${GREEN}✅ Compilación completada.${RESET}"

# Reconstruir y reiniciar solo servicio-reservas
echo -e "${YELLOW}🐳 Reconstruyendo imagen de servicio-reservas...${RESET}"
docker-compose up -d --build servicio-reservas

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Falló el arranque de servicio-reservas.${RESET}"
  exit 1
fi

# Esperar a que arranque y despliegue los BPMN
echo -e "${YELLOW}⏳ Esperando a que servicio-reservas esté listo...${RESET}"
for i in $(seq 1 20); do
  STATUS=$(docker inspect --format='{{.State.Status}}' servicio-reservas 2>/dev/null)
  if [ "$STATUS" = "running" ]; then
    # Comprobar que el health endpoint responde
    if curl -sf http://localhost:9090/actuator/health > /dev/null 2>&1; then
      echo -e "${GREEN}✅ servicio-reservas listo.${RESET}"
      break
    fi
  elif [ "$STATUS" = "restarting" ] || [ "$STATUS" = "exited" ]; then
    echo -e "${RED}❌ servicio-reservas falló al arrancar. Revisa los logs:${RESET}"
    echo -e "   docker logs servicio-reservas --tail 30"
    exit 1
  fi
  sleep 2
done

echo ""
echo -e "${GREEN}🎉 BPMN desplegado correctamente en Zeebe.${RESET}"
echo -e "${CYAN}   Comprueba la nueva versión en Operate: http://localhost:8081${RESET}"
