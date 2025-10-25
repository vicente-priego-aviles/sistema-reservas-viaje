#!/bin/bash

# Detener y eliminar TODO
docker stop $(docker ps -aq) 2>/dev/null || true
docker rm -f $(docker ps -aq) 2>/dev/null || true

# Eliminar TODAS las redes personalizadas
docker network prune -f

# Limpieza del sistema
docker system prune -f

# Verificar que solo queden las redes por defecto
docker network ls