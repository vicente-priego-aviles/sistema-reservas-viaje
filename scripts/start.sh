#!/bin/bash
cd "$(dirname "$0")/.."  # Asegurar ejecución desde la raíz del proyecto

# 1. Crear la red (si no existe)
docker network create camunda-network

# 2. Levantar Camunda PRIMERO
docker compose -f docker-compose-camunda.yml up -d

# 3. Esperar a que Camunda esté listo
#    Puerto 9600 = management/health del contenedor unificado camunda/camunda
until curl -f http://localhost:9600/actuator/health/readiness >/dev/null 2>&1; do
    echo "⏳ Esperando a Camunda (Operate: http://localhost:8080/operate, Tasklist: http://localhost:8080/tasklist)..."
    sleep 10
done

# 4. Levantar tus microservicios
docker compose up -d