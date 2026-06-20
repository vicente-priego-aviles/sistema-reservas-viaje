# Caso 4: Cliente Bloqueado

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

El cliente existe pero está en estado BLOQUEADO. La validación de tarjeta pasa correctamente (la tarjeta en sí es válida), pero al intentar actualizar el estado del cliente a `EN_PROCESO_RESERVA`, el worker lanza `ERROR_CLIENTE_BLOQUEADO`. El error se propaga al proceso padre, que lo captura en el boundary del subproceso de gestión de cliente.

**Nota**: Roberto Morales Gil está BLOQUEADO por "Actividad sospechosa detectada".

## Datos de Entrada

```json
{
  "clienteId": "b23e4567-e89b-12d3-a456-426655440010",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2027-06-01",
  "fechaFin": "2027-06-08",
  "numeroPasajeros": 1,
  "emailContacto": "roberto.morales@example.com",
  "telefonoContacto": "+34611234567"
}
```

## Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅ → Roberto Morales Gil (BLOQUEADO) - puedeRealizarPagos=false
   - Validar tarjeta ✅       → VISA *5727 — tarjeta válida, pasa la pasarela
   - Actualizar estado ❌     → ERROR_CLIENTE_BLOQUEADO (ACTIVO→EN_PROCESO_RESERVA denegado)
   - Error propagado al proceso padre → boundary en call-activity-gestion-cliente
3. Fin: Error en Gestión de Cliente ❌
```

## Ejecutar con cURL

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "b23e4567-e89b-12d3-a456-426655440010",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 1,
    "emailContacto": "roberto.morales@example.com",
    "telefonoContacto": "+34611234567"
  }'
```

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685254,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

## Logs a consultar

**`./logs.sh clientes`**:
```
🚀 Iniciando worker obtener-datos-cliente - Job Key: ...
🔍 Obteniendo datos del cliente: b23e4567-...
✅ Cliente encontrado: b23e4567-... - Estado: BLOQUEADO - Email: roberto.morales@example.com
📤 Datos del cliente preparados - Tarjetas: 1 - Puede reservar: false
🚀 Iniciando worker validar-tarjeta-credito - Job Key: ...
💳 Validando tarjeta de crédito para cliente: b23e4567-...
🔍 Tarjeta seleccionada: ... - Tipo: VISA - Últimos 4 dígitos: 5727
✅ Fecha de expiración válida: 12/2027
🔐 Simulando validación con pasarela de pago - Monto: 1000.0€
✅ Pasarela de pago: Transacción APROBADA - Código: ...
✅ Tarjeta validada correctamente - Código autorización: ...
📤 Validación completada exitosamente
🔄 Iniciando actualización de estado de cliente - Job: ...
🔍 Actualizando estado de cliente: b23e4567-... → Estado nuevo: EN_PROCESO_RESERVA - Reserva: ...
❌ Transición de estado inválida: Transición de estado no permitida: BLOQUEADO → EN_PROCESO_RESERVA. Consulte la documentación para ver las transiciones válidas.
```

> El proceso no avanza más allá de la gestión de cliente — no habrá trazas en `reservas` ni `pagos`.

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-error-gestion-cliente`; variable `estadoCliente=BLOQUEADO`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes
