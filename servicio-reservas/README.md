# 🎟️ servicio-reservas

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Camunda](https://img.shields.io/badge/Camunda-8.9.6-blue.svg)](https://camunda.com/)

> Punto de entrada del sistema de reservas de viaje: expone la API pública, despliega los procesos BPMN en Camunda 8 y ejecuta los workers que reservan y cancelan vuelos, hoteles y coches.

## 📋 Contenido

- [Qué hace](#-qué-hace)
- [API REST](#-api-rest)
- [Procesos BPMN](#-procesos-bpmn)
- [Workers de Zeebe](#-workers-de-zeebe)
- [Modelo de dominio](#-modelo-de-dominio)
- [Configuración](#️-configuración)
- [Ejecución](#-ejecución)
- [Tests](#-tests)
- [Particularidades](#️-particularidades)

## 🎯 Qué hace

- Recibe la solicitud de viaje por REST y arranca una instancia de `proceso-principal` en Zeebe.
- Al arrancar, despliega en Camunda los procesos BPMN y los formularios de `src/main/resources/bpmn/`.
- Atiende los jobs de reserva y de cancelación (compensación) de vuelo, hotel y coche, y guarda esas reservas en su propia base de datos.

La gestión del cliente la hace `servicio-clientes` y el pago `servicio-pagos`; este servicio solo los invoca a través de los procesos BPMN.

## 🌐 API REST

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/reservas/iniciar` | Arranca `proceso-principal` con los datos de la solicitud. Responde `202 Accepted`. |

Cuerpo de la petición (`IniciarReservaDTO`):

```json
{
  "clienteId": "CLI-001",
  "origen": "Madrid",
  "destino": "Roma",
  "fechaInicio": "2026-12-10",
  "fechaFin": "2026-12-15",
  "numeroPasajeros": 2,
  "emailContacto": "cliente@example.com",
  "telefonoContacto": "+34600000000"
}
```

`origen` es opcional; `numeroPasajeros` va de 1 a 10; el resto de campos son obligatorios. La respuesta (`IniciarReservaRespuestaDTO`) devuelve `processInstanceKey`, `estado` (`INICIADA`) y `mensaje`. El seguimiento de la instancia se hace en Operate (http://localhost:8080/operate).

## 📊 Procesos BPMN

`BpmnDeploymentService` despliega al arrancar, con hasta 3 reintentos, en este orden:

1. `subproceso-gestion-cliente.bpmn` — obtiene el cliente, valida su tarjeta y lo marca «en proceso».
2. `subproceso-reserva.bpmn` (id `subproceso-proceso-reserva`) — reserva en paralelo de vuelo, hotel y coche con compensaciones.
3. `subproceso-pago.bpmn` — procesa el pago, confirma la reserva o revierte el estado del cliente.
4. Los formularios `gestionar-reserva-vuelo|hotel|coche.form`.
5. `proceso-principal.bpmn` junto con `iniciar-reserva.form`, en un mismo despliegue: Camunda solo resuelve el formulario del evento de inicio si va en el mismo despliegue que el proceso.

`proceso-principal` carga y valida los datos de entrada, llama como call activity a `subproceso-gestion-cliente` y a `subproceso-pago`, y entre ambas ejecuta la reserva como **subproceso incrustado** (su propia copia del flujo de reserva, con las tareas de usuario «Introducir datos del vuelo/hotel/coche»). `subproceso-proceso-reserva` se despliega, pero hoy ningún proceso lo invoca.

### Saga y compensación

Cada tarea de reserva (`reservar-vuelo`, `reservar-hotel`, `reservar-coche`) tiene un evento de borde de compensación que dispara su cancelación (`cancelar-vuelo`, `cancelar-hotel`, `cancelar-coche`). Si el pago falla, `servicio-pagos` ejecuta `publicar-compensacion-reserva`, que publica el mensaje `compensar-reserva`; ese mensaje arranca el subproceso de evento «Manejar compensación», que deshace las reservas hechas y revierte el estado del cliente.

## 🔧 Workers de Zeebe

Están en `infraestructura/adaptador/entrada/camunda/`. Los errores de negocio se lanzan con `BpmnError.bpmnError(...)`.

| Tipo de job | Clase | Errores BPMN |
|---|---|---|
| `reservar-vuelo` | `ReservaVueloWorker` | `ERROR_VALIDACION_VUELO`, `ERROR_RESERVA_VUELO` |
| `reservar-hotel` | `ReservaHotelWorker` | `ERROR_VALIDACION_HOTEL`, `ERROR_RESERVA_HOTEL` |
| `reservar-coche` | `ReservaCocheWorker` | `ERROR_VALIDACION_COCHE`, `ERROR_RESERVA_COCHE` |
| `cancelar-vuelo` | `CancelarVueloWorker` | — |
| `cancelar-hotel` | `CancelarHotelWorker` | — |
| `cancelar-coche` | `CancelarCocheWorker` | — |

Los workers de reserva leen los datos que el usuario introduce en los formularios (por ejemplo, en el vuelo: `numeroVuelo`, `aerolinea`, `origen`, `destino`, `fechaSalida`, `fechaLlegada`, `clase`, `precioVuelo` y la lista `pasajeros`) y llaman a su caso de uso. Los de cancelación buscan el id de la reserva en las variables del proceso y la cancelan con un motivo.

## 🧩 Modelo de dominio

Paquete base: `dev.javacadabra.reservasviaje.reserva`, con arquitectura hexagonal (`dominio` → `aplicacion` → `infraestructura`).

- **Agregados**: `ReservaVuelo`, `ReservaHotel`, `ReservaCoche`.
- **Entidades**: `DetalleReserva`, `Pasajero`.
- **Objetos de valor**: `ReservaId`, `EstadoReserva`, `PrecioReserva`, `DatosVuelo`, `DatosHotel`, `DatosCoche`.
- **Eventos**: `ReservaVueloCreadaEvento`, `ReservaVueloCanceladaEvento` y sus equivalentes de hotel y coche. `EventoPublicadorAdaptador` los publica en local con `ApplicationEventPublisher` de Spring.
- **Excepciones**: `ReservaInvalidaException`, `ReservaDuplicadaException`, `ReservaNoEncontradaException`, `CancelacionNoPermitidaException`.

## ⚙️ Configuración

Todo está en `src/main/resources/application.yml`:

| Elemento | Valor |
|---|---|
| Puerto | `9090` |
| Base de datos | H2 en memoria `reservas_db` (`ddl-auto: create-drop`), consola en `/h2-console` |
| Zeebe | gRPC `http://127.0.0.1:26500`, REST `http://127.0.0.1:8080`, usuario `demo` |
| OpenAPI | `/api-docs` y Swagger UI en `/swagger-ui.html` |
| Actuator | `/actuator/health`, `/actuator/info`, `/actuator/metrics` |

En Docker (`docker-compose.yml`), `CAMUNDA_CLIENT_ZEEBE_GRPCADDRESS` y `CAMUNDA_CLIENT_ZEEBE_RESTADDRESS` apuntan al contenedor `camunda`.

## 🚀 Ejecución

```bash
# 1. Camunda (desde la raíz del repo)
docker-compose -f docker-compose-camunda.yml up -d

# 2. Este servicio
./mvnw spring-boot:run -pl servicio-reservas

# 3. Comprobar que responde
curl http://localhost:9090/actuator/health
```

Para el flujo completo hacen falta también `servicio-clientes` y `servicio-pagos`; `./scripts/start.sh` levanta todo el sistema. Ver el [README raíz](../README.md).

## 🧪 Tests

Este módulo todavía no tiene tests: `src/test/java` está vacío. Las dependencias (`spring-boot-starter-test`, JUnit 5, ArchUnit, Testcontainers) ya están en el `pom.xml`.

## ⚠️ Particularidades

- Los mappers de persistencia están en `persistencia/mapeador/`, no en `persistencia/mapper/` como en los demás módulos.
- El `pom.xml` trae PostgreSQL, Flyway, Resilience4j, Prometheus y Zipkin, pero solo hay un `application.yml` con H2: no hay perfil `prod`.
- La copia de `bpmn/` en la raíz del repo, pensada para Camunda Modeler, no tiene `subproceso-reserva.bpmn` ni `forms/`.
