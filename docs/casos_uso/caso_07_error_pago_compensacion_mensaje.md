# Caso 7: Error en Pago con Compensación por Mensaje

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Las reservas se completan correctamente pero el pago falla porque el monto total supera el límite del sistema (€10.000). El subproceso de pago lanza `ERROR_PROCESAR_PAGO`, que envía el mensaje `compensar-reserva` al subproceso de reserva. El subproceso de reserva captura el mensaje y ejecuta la compensación manual de las tres reservas.

El error de pago se simula cuando la suma de los precios calculados por los workers supera el límite. Para forzarlo, introducir precios altos al completar los User Tasks de gestión de reserva.

## Datos de Entrada

```json
{
  "clienteId": "123e4567-e89b-12d3-a456-426655440000",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2027-06-01",
  "fechaFin": "2027-06-08",
  "numeroPasajeros": 2,
  "emailContacto": "juan.perez@example.com",
  "telefonoContacto": "+34600123456"
}
```

Para forzar el error: al completar los User Tasks de revisión de vuelo/hotel/coche en Tasklist, introducir precios que sumen más de €10.000 (ej. vuelo: €4.001, hotel: €3.000, coche: €3.000 → total €10.001).

## Flujo del Proceso

```
1. Validar Datos ✅
2. Gestión de Cliente ✅
3. Revisar Datos 👤
4. Proceso de Reserva (Paralelo)
   - Reservar Vuelo ✅
   - Reservar Hotel ✅
   - Reservar Coche ✅
   - User Tasks completados con precios altos 👤
5. Proceso de Pago
   - Procesar Pago ❌ (monto total > €10.000 → MontoExcedeLimiteException)
   - Boundary Event: ERROR_PROCESAR_PAGO
   - Envía mensaje "compensar-reserva" al subproceso de reserva
   - Notificar Cliente 📧
6. Subproceso de Reserva recibe mensaje de compensación:
   - Compensar Vuelo 🔄
   - Compensar Hotel 🔄
   - Compensar Coche 🔄
   - Actualizar registro cliente 🔄
7. Fin: Reserva No Completada ❌
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

Completar los User Tasks en **Tasklist** (http://localhost:8081) introduciendo precios altos para superar el límite de €10.000.

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685257,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

## Logs a consultar

**`./logs.sh pagos`** — el fallo del pago:
```
🔄 Worker: procesar-pago - Reserva: ... - Monto: 10001.0€
❌ Monto excede límite: 10001.0€
```

**`./logs.sh reservas`** — las compensaciones que se disparan a continuación:
```
🛑 Iniciando worker de cancelación de vuelo (compensación) - Job Key: ...
❌ Cancelando reserva de vuelo: ... - Motivo: ...
✅ Reserva de vuelo cancelada exitosamente: ...
🛑 Iniciando worker de cancelación de hotel (compensación) - Job Key: ...
❌ Cancelando reserva de hotel: ... - Motivo: ...
✅ Reserva de hotel cancelada exitosamente: ...
🛑 Iniciando worker de cancelación de coche (compensación) - Job Key: ...
❌ Cancelando reserva de coche: ... - Motivo: ...
✅ Reserva de coche cancelada exitosamente: ...
```

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-reserva-no-completada`; ver el subproceso de compensación manual activado; variables `pagoRealizado=false`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes tras el error
