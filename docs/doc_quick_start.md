# 🚀 Quick Start - Sistema de Reservas de Viaje

Esta guía te ayudará a tener el sistema funcionando en **menos de 5 minutos**.

---

## 📋 Pre-requisitos

Antes de comenzar, asegúrate de tener instalado:

- ☕ **Java 21** ([Descargar](https://openjdk.org/))
- 🐳 **Docker** y **Docker Compose** ([Descargar](https://www.docker.com/))
- 🔧 **Git** ([Descargar](https://git-scm.com/))

### Verificar Instalaciones

```bash
# Java
java -version
# Debería mostrar: openjdk version "21.x.x"

# Docker
docker --version
docker-compose --version

# Git
git --version
```

---

## ⚡ Inicio Rápido

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/sistema-reservas-viaje.git
cd sistema-reservas-viaje
```

### Paso 2: Levantar el Sistema

```bash
chmod +x start.sh
./start.sh
```

El script hará lo siguiente:
1. 🚀 Levanta Camunda Platform 8 (Zeebe, Operate, Tasklist)
2. ⏳ Espera 60 segundos para que Camunda esté listo
3. 🏗️ Construye y levanta los 6 microservicios

### Paso 3: Verificar que Todo Funciona

Abre tu navegador y verifica los siguientes endpoints:

#### Camunda Platform 8
- 📊 **Operate**: http://localhost:8080
  - Usuario: `demo`
  - Password: `demo`
- 📋 **Tasklist**: http://localhost:8081
  - Usuario: `demo`
  - Password: `demo`

#### Microservicios (Health Checks)
- 👥 **Clientes**: http://localhost:9080/actuator/health
- ✈️ **Vuelos**: http://localhost:9081/actuator/health
- 🏨 **Hoteles**: http://localhost:9082/actuator/health
- 🚗 **Coches**: http://localhost:9083/actuator/health
- 💳 **Pagos**: http://localhost:9084/actuator/health
- 🎯 **Reservas**: http://localhost:9090/actuator/health

**Todos deberían devolver**: `{"status":"UP"}`

---

## 🎯 Primera Reserva

### Opción 1: Usando Camunda Tasklist (Recomendado)

1. Accede a **Camunda Tasklist**: http://localhost:8081
2. Login: `demo` / `demo`
3. Click en **"Start Process"** (botón azul arriba a la derecha)
4. Selecciona **"Travel Booking Scenario"**
5. Completa el formulario con estos datos de prueba:

```
Cliente ID: CLI-001
Origen: Madrid
Destino: Barcelona
Fecha Inicio: 2025-12-01
Fecha Fin: 2025-12-05
Monto: 1500
```

6. Click en **"Start"**
7. Verás las tareas de usuario aparecer en tu lista
8. Completa cada tarea: ✈️ Revisar Vuelo, 🏨 Revisar Hotel, 🚗 Revisar Coche

### Opción 2: Usando cURL (API REST)

```bash
# Iniciar proceso de reserva
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI-001",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2025-12-01",
    "fechaFin": "2025-12-05",
    "monto": 1500
  }'
```

---

## 👁️ Monitorear el Proceso

### Camunda Operate

1. Accede a **Camunda Operate**: http://localhost:8080
2. Login: `demo` / `demo`
3. Verás tu instancia de proceso en ejecución
4. Click en ella para ver el flujo visual con el progreso actual

<!--
📸 Insertar imagen aquí:
![Camunda Operate](../images/screenshots/camunda-operate.png)
-->

### Ver Logs de un Microservicio

```bash
# Ver logs en tiempo real
docker-compose logs -f servicio-clientes
docker-compose logs -f servicio-vuelos
docker-compose logs -f servicio-pagos

# Ver últimas 100 líneas
docker-compose logs --tail=100 servicio-reservas
```

---

## 🧪 Datos de Prueba

El sistema viene con datos precargados para testing:

### Clientes Válidos

| Cliente | ID | Tarjeta | Resultado |
|---------|-----|---------|-----------|
| Vicente Priego | CLI-001 | ✅ Válida | Reserva exitosa |
| Verónica Lesmes | CLI-002 | ✅ Válida | Reserva exitosa |

### Escenarios de Error (para testing)

| Cliente | ID | Problema | Resultado Esperado |
|---------|-----|----------|-------------------|
| Juan Pérez | CLI-003 | ❌ Tarjeta inválida | Error en validación |
| - | CLI-999 | ❌ No existe | Cliente no encontrado |

### Escenarios por Monto

| Monto | Comportamiento |
|-------|---------------|
| < 5000 | ✅ Reserva exitosa |
| 5000 - 10000 | ⚠️ Genera advertencia pero confirma |
| > 10000 | ❌ Simula error de pago → Compensación |

---

## 🔍 Explorar las APIs

### Swagger UI (OpenAPI)

Cada microservicio tiene su propia documentación OpenAPI:

- 👥 **Clientes**: http://localhost:9080/swagger-ui.html
- ✈️ **Vuelos**: http://localhost:9081/swagger-ui.html
- 🏨 **Hoteles**: http://localhost:9082/swagger-ui.html
- 🚗 **Coches**: http://localhost:9083/swagger-ui.html
- 💳 **Pagos**: http://localhost:9084/swagger-ui.html
- 🎯 **Reservas**: http://localhost:9090/swagger-ui.html

### Ejemplos de Peticiones

#### Obtener Cliente

```bash
curl http://localhost:9080/api/clientes/CLI-001
```

#### Listar Vuelos Disponibles

```bash
curl http://localhost:9081/api/vuelos?origen=Madrid&destino=Barcelona
```

#### Verificar Estado de Reserva

```bash
curl http://localhost:9090/api/reservas/{reservaId}/estado
```

---

## 🛑 Detener el Sistema

```bash
# Detener todos los contenedores
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

---

## 🔧 Troubleshooting

### Problema: Puerto ya en uso

```bash
# Error: bind: address already in use
# Solución: Detener el proceso que usa el puerto

# Encontrar proceso en puerto 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Matar el proceso
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
```

### Problema: Camunda no arranca

```bash
# Verificar logs de Camunda
docker-compose -f docker-compose-camunda.yml logs zeebe
docker-compose -f docker-compose-camunda.yml logs operate

# Reiniciar Camunda
docker-compose -f docker-compose-camunda.yml restart
```

### Problema: Microservicio no se conecta a Zeebe

```bash
# Verificar que Zeebe esté escuchando
docker-compose -f docker-compose-camunda.yml ps

# Verificar logs del microservicio
docker-compose logs servicio-reservas | grep "zeebe"

# Reintentar conexión
docker-compose restart servicio-reservas
```

### Problema: Base de datos H2 corrupta

```bash
# H2 es en memoria, reiniciar el microservicio
docker-compose restart servicio-clientes
```

---

## 📚 Próximos Pasos

Ahora que tienes el sistema funcionando:

1. 📖 Lee la [Arquitectura Detallada](02-arquitectura.md)
2. 📊 Explora los [Procesos BPMN](05-procesos-bpmn.md)
3. 💡 Revisa los [Casos de Uso](casos-uso.md)
4. 🛠️ Aprende sobre cada [Microservicio](06-microservicios.md)
5. 🧪 Ejecuta los [Tests](07-testing.md)

---

## 🆘 ¿Necesitas Ayuda?

- 📖 Consulta la [documentación completa](../README.md)
- 🐛 Reporta issues en [GitHub](https://github.com/tu-usuario/sistema-reservas-viaje/issues)
- 💬 Pregunta en [Discussions](https://github.com/tu-usuario/sistema-reservas-viaje/discussions)

---

¡Felicidades! 🎉 Ya tienes el sistema funcionando. Ahora puedes empezar a explorar y experimentar.
