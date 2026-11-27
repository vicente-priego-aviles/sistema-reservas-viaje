# servicio-reservas

> Las convenciones comunes (comandos, estructura hexagonal, cómo se escribe un worker, configuración de Zeebe) están en el [CLAUDE.md](../CLAUDE.md) raíz. Este archivo documenta lo que contiene hoy este módulo.

- **Puerto**: 9090 · **BD**: H2 `reservas_db` · **Paquete**: `dev.javacadabra.reservasviaje.reserva`
- **Papel**: punto de entrada público del sistema; despliega los procesos BPMN y ejecuta los workers de reserva y cancelación de vuelos, hoteles y coches.

## API REST

- `IniciarReservaController` — `POST /api/reservas/iniciar` arranca el proceso principal.

## Procesos BPMN

Las copias de ejecución están en `src/main/resources/bpmn/` y las despliega al arrancar `infraestructura/configuracion/BpmnDeploymentService` (`proceso-principal` va en el mismo despliegue que `iniciar-reserva.form`). Hay 4 archivos:

1. `proceso-principal` — orquestador de nivel superior; invoca como call activity a `subproceso-gestion-cliente` y `subproceso-pago`, y lleva la reserva como **subproceso incrustado** propio.
2. `subproceso-gestion-cliente` — valida el cliente y la tarjeta de crédito
3. `subproceso-reserva.bpmn` (id `subproceso-proceso-reserva`) — reserva en paralelo de vuelo + hotel + coche con compensaciones de la Saga. Se despliega, pero hoy ningún proceso lo invoca.
4. `subproceso-pago` — procesamiento del pago con caminos de reversión

`src/main/resources/bpmn/forms/` guarda los formularios de Camunda (`iniciar-reserva`, `gestionar-reserva-vuelo|hotel|coche`). El directorio `bpmn/` de la raíz del repo guarda copias editables para Camunda Modeler; hoy le faltan `subproceso-reserva.bpmn` y `forms/`.

### Saga / patrón de compensación

El subproceso de reserva (el incrustado en `proceso-principal` y el de `subproceso-reserva.bpmn`) usa eventos de borde de compensación de BPMN. Cada tarea de reserva (`reservar-vuelo`, `reservar-hotel`, `reservar-coche`) tiene un evento de borde de compensación que dispara su tarea de cancelación (`cancelar-vuelo`, etc.) si falla el pago. La compensación la inicia el mensaje `compensar-reserva` que publica `servicio-pagos` (job `publicar-compensacion-reserva`), que arranca el subproceso de evento «Manejar compensación».

## Workers de Zeebe

| Tipo de job | Clase | Error BPMN |
|---|---|---|
| `reservar-vuelo` | `ReservaVueloWorker` | `ERROR_RESERVA_VUELO`, `ERROR_VALIDACION_VUELO` |
| `reservar-hotel` | `ReservaHotelWorker` | `ERROR_RESERVA_HOTEL`, `ERROR_VALIDACION_HOTEL` |
| `reservar-coche` | `ReservaCocheWorker` | `ERROR_RESERVA_COCHE`, `ERROR_VALIDACION_COCHE` |
| `cancelar-vuelo` / `cancelar-hotel` / `cancelar-coche` | `Cancelar*Worker` | — |

## Dominio

- **Agregados**: `ReservaVuelo`, `ReservaHotel`, `ReservaCoche` (este módulo guarda su propia copia de las tres reservas).
- **Entidades**: `DetalleReserva`, `Pasajero` · **Objetos de valor**: `DatosVuelo`, `DatosHotel`, `DatosCoche`, `EstadoReserva`, `PrecioReserva`, `ReservaId`.
- **Eventos**: `Reserva{Vuelo,Hotel,Coche}{Creada,Cancelada}Evento`, publicados en local con `ApplicationEventPublisher` de Spring a través de `infraestructura/adaptador/salida/cliente/EventoPublicadorAdaptador`.
- **Excepciones**: `CancelacionNoPermitidaException`, `ReservaDuplicadaException`, `ReservaInvalidaException`, `ReservaNoEncontradaException`.

## Particularidades

- Los mappers de persistencia están en `persistencia/mapeador/`, no en `persistencia/mapper/` como en los demás módulos.
- Tiene las mismas dependencias que `servicio-clientes` (PostgreSQL, Flyway, Resilience4j, Prometheus, Zipkin, surefire/failsafe), pero solo está configurado H2: hay un único `application.yml`.
