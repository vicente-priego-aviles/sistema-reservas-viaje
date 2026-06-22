#!/bin/bash

echo "🚀 Iniciando Camunda Platform 8.9..."
docker compose -f docker-compose-camunda.yml up -d

echo "⏳ Esperando a que Camunda esté listo..."
until curl -f http://localhost:9600/actuator/health/readiness >/dev/null 2>&1; do
    sleep 10
done

echo "🏗️ Construyendo y levantando microservicios..."
docker compose up -d --build

echo ""
echo "✅ Sistema completo iniciado!"
echo ""
echo "📊 Camunda Operate:  http://localhost:8080/operate  (demo/demo)"
echo "📋 Camunda Tasklist: http://localhost:8080/tasklist (demo/demo)"
echo ""
echo "🔧 Microservicios del Sistema de Reservas:"
echo "   📦 Reservas (Coordinador): http://localhost:9090/actuator/health"
echo "   👤 Clientes: http://localhost:9080/actuator/health"
echo "   ✈️  Vuelos: http://localhost:9081/actuator/health"
echo "   🏨 Hoteles: http://localhost:9082/actuator/health"
echo "   🚗 Alquiler de Coches: http://localhost:9083/actuator/health"
echo "   💳 Pagos: http://localhost:9084/actuator/health"
echo ""
echo "📝 Para ver logs en tiempo real:"
echo "   docker compose logs -f servicio-reservas"
echo "   docker compose logs -f servicio-vuelos"
echo "   docker compose logs -f servicio-hoteles"
echo "   docker compose logs -f servicio-alquiler-coches"
echo "   docker compose logs -f servicio-pagos"
