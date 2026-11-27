# servicio-pagos

> Las convenciones comunes (comandos, estructura hexagonal, cómo se escribe un worker, configuración de Zeebe) están en el [CLAUDE.md](../CLAUDE.md) raíz. Este archivo documenta lo que contiene hoy este módulo.

- **Puerto**: 9084 · **BD**: H2 `pagos_db` · **Paquete**: `dev.javacadabra.reservasviaje.pago`
- **Papel**: procesamiento del pago en `subproceso-pago`, confirmación de la reserva y arranque de la compensación de la Saga.
- Sin controladores REST (el paquete `rest/` está vacío).

## Workers de Zeebe

| Tipo de job | Clase | Notas |
|---|---|---|
| `procesar-pago` | `PagoWorker` | `ERROR_PROCESAR_PAGO` |
| `confirmar-reserva` | `PagoWorker` | `ERROR_CONFIRMAR_RESERVA` |
| `revertir-estado-cliente` | `PagoWorker` | |
| `marcar-reserva-advertencia` | `PagoWorker` | |
| `publicar-compensacion-reserva` | `CompensacionReservaWorker` | publica el mensaje `compensar-reserva` con `CamundaClient` |

## Dominio

- **Agregado**: `Pago` · **Objetos de valor**: `PagoId`, `Monto`, `MetodoPago`, `EstadoPago`.
- **Excepciones**: `MontoExcedeLimiteException`, `MontoInvalidoException`, `PagoNoEncontradoException`.
