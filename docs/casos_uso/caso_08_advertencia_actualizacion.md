# Caso 8: Advertencia en Actualización

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

El pago se procesa correctamente pero falla la actualización del estado del cliente a `RESERVA_CONFIRMADA`. El subproceso de pago ejecuta la compensación parcial (revertir estado y marcar con advertencia). El proceso termina con éxito pero señalizado para intervención manual.

> **Nota de implementación**: el boundary event del BPMN captura el error code `ERROR_ACTUALIZACION_CLIENTE`, pero el worker `actualizar-estado-cliente` actualmente lanza otros códigos (`ERROR_TRANSICION_INVALIDA`, `ERROR_CLIENTE_BLOQUEADO`, etc.). Para que este caso funcione tal como está diseñado en el BPMN, hay dos opciones: (a) modificar el BPMN para que el boundary event sea catch-all (sin error code), o (b) forzar el error directamente vía H2 console como se describe abajo.

## Datos de Entrada

Mismos datos que el Caso 1. El error se fuerza **manipulando la base de datos del cliente** usando la H2 console mientras el proceso está en ejecución, justo antes de que se ejecute `actualizar-estado-confirmado`.

**Pasos para forzar el error**:

1. Iniciar el proceso con datos válidos (Caso 1)
2. Completar los User Tasks hasta llegar al subproceso de pago
3. Mientras el proceso espera en el payment worker, abrir **H2 Console** del servicio-clientes:
   - URL: http://localhost:9080/h2-console
   - JDBC URL: `jdbc:h2:mem:cliente_db`
   - Usuario: `sa`, sin contraseña
4. Ejecutar para poner al cliente en estado inválido:
   ```sql
   UPDATE clientes SET estado = 'INACTIVO'
   WHERE id = '123e4567-e89b-12d3-a456-426655440000';
   ```
5. Cuando el proceso intente `actualizar-estado-confirmado` (EN_PROCESO_RESERVA → RESERVA_CONFIRMADA), el worker rechazará la transición

## Flujo del Proceso

```
1-4. Flujo normal hasta Pago ✅
5. Proceso de Pago
   - Procesar Pago ✅
   - Confirmar Reserva ✅
   - Actualizar Estado ❌    → transición de estado inválida
   - Boundary Event (según diseño BPMN): Error Actualización
   - Revertir Estado del Cliente 🔄
   - Marcar Reserva con Advertencia ⚠️
6. Fin: Reserva Confirmada con Advertencia ⚠️
```

## Ejecutar con cURL

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "123e4567-e89b-12d3-a456-426655440000",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 2,
    "emailContacto": "juan.perez@example.com",
    "telefonoContacto": "+34600123456"
  }'
```

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685258,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

## Variables de Salida

```json
{
  "reservaConfirmada": true,
  "estadoReservaFinal": "CONFIRMADA_CON_ADVERTENCIA",
  "requiereIntervencionManual": true
}
```

## Logs a consultar

**`./logs.sh pagos`** — pago y confirmación exitosos, seguidos del fallo de actualización y compensación parcial:
```
🔄 Worker: procesar-pago - Reserva: ... - Monto: ...€
✅ Pago procesado - Transacción: ...
🔄 Worker: confirmar-reserva - Reserva: ...
✅ Reserva confirmada - Número: ...
🔄 Worker: revertir-estado-cliente - Reserva: ...
✅ Estado del cliente revertido
🔄 Worker: marcar-reserva-advertencia - Reserva: ...
⚠️ Reserva marcada con advertencia
```

**`./logs.sh clientes`** — el error en la actualización de estado que desencadena el boundary event:
```
🔄 Iniciando actualización de estado de cliente - Job: ...
❌ Transición de estado inválida: ...
```

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-reserva-con-advertencia`; variables `reservaConfirmada=true`, `estadoReservaFinal=CONFIRMADA_CON_ADVERTENCIA`, `requiereIntervencionManual=true`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes
