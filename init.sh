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
}

# Los Dockerfile copian el jar de target/, así que hace falta package, no solo compile.
# clean evita empaquetar clases viejas que el IDE haya dejado en target/ y Maven no recompile.
echo "› Empaquetando desde cero (sin tests)…"
node .mh/scripts/mvnq.mjs -q clean package -DskipTests

if [[ "${1:-}" == "--solo-compilar" ]]; then
  echo "✔ Entorno listo (solo compilado; Docker no ejecutado)"
  exit 0
fi

if ! docker info >/dev/null 2>&1; then
  echo "✘ Docker no está disponible: arráncalo o usa ./init.sh --solo-compilar" >&2
  exit 1
fi

# Camunda primero: crea la red sistema-reservas-viaje_camunda-platform que usan los microservicios
echo "› Levantando Camunda…"
docker compose -f docker-compose-camunda.yml up -d
esperar "Camunda" "http://localhost:9600/actuator/health/readiness"

echo "› Levantando los microservicios (reconstruye las imágenes que hayan cambiado)…"
docker compose up -d --build

echo "› Smoke test: /actuator/health de cada microservicio…"
for s in "${SERVICIOS[@]}"; do
  esperar "${s%%:*}" "http://localhost:${s##*:}/actuator/health"
done

echo "✔ Entorno listo (Operate: http://localhost:8080/operate · Tasklist: http://localhost:8080/tasklist)"
