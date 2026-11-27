#!/usr/bin/env bash
# Arranque del entorno y smoke test (rutina de inicio de sesión).
# Generado por meta-harness-factory; ajústalo a tu proyecto.
set -euo pipefail
cd "$(dirname "$0")"

# Infraestructura detectada: descomenta lo que la aplicación necesite para arrancar.
# docker compose -f docker-compose-camunda.yml up -d
# docker compose -f docker-compose.yml up -d

echo "› Compilando…"
node .mh/scripts/mvnq.mjs -q compile

# Smoke test: ajusta el puerto de tu aplicación
# curl -fsS http://localhost:8080/actuator/health

echo "✔ Entorno listo"
