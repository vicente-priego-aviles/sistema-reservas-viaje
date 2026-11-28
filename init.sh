#!/usr/bin/env bash
# Arranque del entorno y smoke test (rutina de inicio de sesión).
# Generado por meta-harness-factory y adaptado a sistema-reservas-viaje.
#
# Uso:
#   ./init.sh                  compila, levanta Camunda y los 6 microservicios y comprueba su salud
#   ./init.sh --solo-compilar  solo compila (sin Docker)
#
# Variables opcionales:
#   ESPERA_MAX=300   segundos máximos de espera por Camunda y por cada microservicio
set -euo pipefail
cd "$(dirname "$0")"

ESPERA_MAX="${ESPERA_MAX:-300}"
# Los dos compose comparten nombre de proyecto (el del directorio): sin esto, cada uno avisa de que los contenedores del otro están huérfanos
export COMPOSE_IGNORE_ORPHANS=true

# Resultado de la última ejecución, para el Panel del proyecto (brain/Panel.md): logs/init-ultimo.json (ignorado por git)
MODO="completo"; [[ "${1:-}" == "--solo-compilar" ]] && MODO="solo-compilar"
FASE="compilar"
SANOS=()
registrar() {
  local codigo=$? resultado="ok" sanos=""
  (( codigo )) && resultado="fallo"
  (( ${#SANOS[@]} )) && sanos=$(printf '"%s",' "${SANOS[@]}") && sanos="${sanos%,}"
  mkdir -p logs
  printf '{"fecha":"%s","modo":"%s","resultado":"%s","fase":"%s","sanos":[%s]}\n' "$(date -Iseconds)" "$MODO" "$resultado" "$FASE" "$sanos" > logs/init-ultimo.json
}
trap registrar EXIT

# Puerto de cada microservicio (docker-compose.yml)
SERVICIOS=(
  "servicio-clientes:9080"
  "servicio-vuelos:9081"
  "servicio-hoteles:9082"
  "servicio-alquiler-coches:9083"
  "servicio-pagos:9084"
  "servicio-reservas:9090"
)

# esperar <nombre> <url>: reintenta cada 5 s hasta que la URL responde 2xx o se agota ESPERA_MAX
esperar() {
  local nombre="$1" url="$2" inicio=$SECONDS
  until curl -fsS -o /dev/null "$url" 2>/dev/null; do
    if (( SECONDS - inicio >= ESPERA_MAX )); then
      echo "✘ $nombre no responde en $url tras ${ESPERA_MAX} s" >&2
      return 1
    fi
    sleep 5
  done
  echo "  ✔ $nombre ($url)"
  SANOS+=("$nombre")
}

# Los Dockerfile copian el jar de target/, así que hace falta package, no solo compile.
# clean evita empaquetar clases viejas que el IDE haya dejado en target/ y Maven no recompile.
echo "› Empaquetando desde cero (sin tests)…"
node .mh/scripts/mvnq.mjs -q clean package -DskipTests

if [[ "${1:-}" == "--solo-compilar" ]]; then
  FASE="listo"
  echo "✔ Entorno listo (solo compilado; Docker no ejecutado)"
  exit 0
fi

FASE="docker"
if ! docker info >/dev/null 2>&1; then
  echo "✘ Docker no está disponible: arráncalo o usa ./init.sh --solo-compilar" >&2
  exit 1
fi

# Camunda primero: crea la red sistema-reservas-viaje_camunda-platform que usan los microservicios
FASE="camunda"
echo "› Levantando Camunda…"
docker compose -f docker-compose-camunda.yml up -d
esperar "Camunda" "http://localhost:9600/actuator/health/readiness"

FASE="microservicios"
echo "› Levantando los microservicios (reconstruye las imágenes que hayan cambiado)…"
docker compose up -d --build

FASE="smoke test"
echo "› Smoke test: /actuator/health de cada microservicio…"
for s in "${SERVICIOS[@]}"; do
  esperar "${s%%:*}" "http://localhost:${s##*:}/actuator/health"
done

FASE="listo"
echo "✔ Entorno listo (Operate: http://localhost:8080/operate · Tasklist: http://localhost:8080/tasklist)"
