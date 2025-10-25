#!/bin/bash

# 1. Crear la red (si no existe)
docker network create camunda-network

# 2. Levantar Camunda PRIMERO
docker-compose -f docker-compose-camunda.yml up -d

# 3. Esperar a que Camunda esté listo (importante!)
until curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; do
    echo "⏳ Esperando a Camunda..."
    sleep 10
done

# 4. Levantar tus microservicios
docker-compose up -d