#!/bin/bash

SERVICIO=$1

declare -A CONTENEDORES=(
  [clientes]="servicio-clientes"
  [vuelos]="servicio-vuelos"
  [hoteles]="servicio-hoteles"
  [coches]="servicio-alquiler-coches"
  [pagos]="servicio-pagos"
  [reservas]="servicio-reservas"
)

if [ -z "$SERVICIO" ]; then
  echo "Uso: ./logs.sh <servicio>"
  echo "Servicios disponibles: ${!CONTENEDORES[@]}"
  exit 1
fi

CONTENEDOR=${CONTENEDORES[$SERVICIO]}

if [ -z "$CONTENEDOR" ]; then
  echo "Servicio desconocido: $SERVICIO"
  echo "Servicios disponibles: ${!CONTENEDORES[@]}"
  exit 1
fi

docker logs -f "$CONTENEDOR"
