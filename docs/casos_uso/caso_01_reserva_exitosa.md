# Caso 1: Reserva Exitosa (Happy Path)

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Cliente activo con tarjeta válida realiza una reserva completa exitosamente.

## Datos de Entrada

> ⚠️ `fechaInicio` y `fechaFin` deben ser fechas **futuras**. El worker de validación rechazará fechas pasadas con `ERROR_DATOS_INVALIDOS`.

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

## Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅ → Juan Pérez García (ACTIVO)
   - Validar tarjeta ✅       → VISA *0366 válida
   - Actualizar estado ✅     → EN_PROCESO_RESERVA
3. Revisar Datos de Entrada (User Task) 👤
4. Proceso de Reserva (Paralelo)
   - Reservar Vuelo ✅  → Revisar Vuelo 👤
   - Reservar Hotel ✅  → Revisar Hotel 👤
   - Reservar Coche ✅  → Revisar Coche 👤
5. Proceso de Pago
   - Procesar Pago ✅
   - Confirmar Reserva ✅
   - Actualizar Estado: RESERVA_CONFIRMADA ✅
6. Fin: Solicitud de Viaje Completada 🎉
```

## Iniciar el proceso

### Opción A — Camunda REST API (Swagger)

Accede a http://localhost:8080/swagger-ui/index.html, endpoint `POST /v2/process-instances`, con el body:

```json
{
  "processDefinitionId": "proceso-principal",
  "variables": {
    "clienteId": "123e4567-e89b-12d3-a456-426655440000",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 2,
    "emailContacto": "juan.perez@example.com",
    "telefonoContacto": "+34600123456"
  }
}
```

> `processDefinitionId` es el `id` del elemento `<bpmn:process>` — en este caso `proceso-principal`. Lanza siempre la última versión desplegada. No uses `processDefinitionKey` (numérico) a la vez que `processDefinitionId`; son alternativos.

### Opción B — API de `servicio-reservas` (cURL)

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

### Opción C — Tasklist

Accede a http://localhost:8080/operate → pestaña **Processes** → selecciona "Proceso Principal de Reserva de Viaje" → pulsa **Start process**. Se abre el formulario de inicio (`iniciar-reserva`); rellénalo con los datos del caso y envía. El proceso arranca directamente desde la interfaz sin necesidad de cURL ni Swagger.

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685251,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

## Logs a consultar

**`./scripts/logs.sh clientes`** — validación, gestión de cliente y tarjeta:
```
✅ Iniciando validación de datos de entrada - Job: ...
✅ Datos de entrada válidos - Cliente: 123e4567-...

🚀 Iniciando worker obtener-datos-cliente - Job Key: ...
🔍 Obteniendo datos del cliente: 123e4567-e89b-12d3-a456-426655440000
✅ Cliente encontrado: 123e4567-... - Estado: ACTIVO - Email: juan.perez@example.com
📤 Datos del cliente preparados - Tarjetas: 1 - Puede reservar: true
   (el worker escribe 13 variables al scope del proceso: nombreCompleto, emailCliente,
    paisCliente, ciudadCliente, estadoCliente, dniCliente [enmascarado: 123****8Z],
    cantidadTarjetas, tieneTarjetasValidas, puedeRealizarPagos, estaBloqueado, etc.)

🚀 Iniciando worker validar-tarjeta-credito - Job Key: ...
⚠️ No se proporcionó montoReserva, usando monto por defecto para validación
   (esperado: el importe real se calcula en los User Tasks de reserva; aquí se usa 1.000 € como proxy)
🔍 Tarjeta seleccionada: 11111111-... - Tipo: VISA - Últimos 4 dígitos: 0366
✅ Fecha de expiración válida: 12/2027
🔐 Simulando validación con pasarela de pago - Monto: 1000.0€
✅ Pasarela de pago: Transacción APROBADA - Código: 5AEDAE
✅ Tarjeta validada correctamente - Código autorización: 5AEDAE
   (el worker escribe 7 variables: tarjetaId, tipoTarjeta, ultimosDigitosTarjeta,
    fechaExpiracionTarjeta, codigoAutorizacion, tarjetaValidada, montoValidado)

🔄 Iniciando actualización de estado de cliente - Job: ...
🔍 Actualizando estado de cliente: 123e4567-... → Estado nuevo: EN_PROCESO_RESERVA - Reserva: ...
✅ Estado de cliente actualizado correctamente: 123e4567-... - ACTIVO → EN_PROCESO_RESERVA
```

> **Nota**: el código de autorización de la pasarela es un string alfanumérico de 6 caracteres generado aleatoriamente (ej. `5AEDAE`). En producción sería el código real devuelto por la pasarela de pago.
>
> **Nota**: el DNI del cliente aparece enmascarado en los logs (`123****8Z`) por privacidad; el dato completo solo existe en la base de datos.

**`./scripts/logs.sh reservas`** — reservas de vuelo, hotel y coche (en paralelo):
```
🚀 Iniciando worker de reserva de vuelo - Job Key: ...
✈️ Procesando reserva de vuelo ... para cliente: 123e4567-...
✅ Reserva de vuelo completada exitosamente - ID: ...
🚀 Iniciando worker de reserva de hotel - Job Key: ...
🏨 Procesando reserva de hotel ... en ... para cliente: 123e4567-...
✅ Reserva de hotel completada exitosamente - ID: ...
🚀 Iniciando worker de reserva de coche - Job Key: ...
🚗 Procesando reserva de coche ... de ... para cliente: 123e4567-...
✅ Reserva de coche completada exitosamente - ID: ...
```

**`./scripts/logs.sh pagos`** — pago y confirmación:
```
🔄 Worker: procesar-pago - Reserva: ... - Monto: ...€
✅ Pago procesado - Transacción: ...
🔄 Worker: confirmar-reserva - Reserva: ...
✅ Reserva confirmada - Número: ...
🔄 Iniciando actualización de estado de cliente - Job: ...
✅ Estado de cliente actualizado correctamente: 123e4567-... → RESERVA_CONFIRMADA
```

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia completada en `fin-solicitud-completada`
2. **Tasklist** (http://localhost:8080/tasklist): completar los User Tasks para avanzar el flujo
