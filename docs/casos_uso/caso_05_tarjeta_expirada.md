# Caso 5: Tarjeta Expirada

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Cliente activo pero con tarjeta de crédito expirada. El worker `validar-tarjeta-credito` detecta la expiración y lanza `ERROR_TARJETA_INVALIDA`, que es capturado por el boundary event del task de validación dentro del subproceso de gestión de cliente.

**Nota**: Raquel Iglesias Márquez tiene tarjeta VISA expirada (08/2023).

## Datos de Entrada

```json
{
  "clienteId": "0e3e4567-e89b-12d3-a456-426655440015",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2027-06-01",
  "fechaFin": "2027-06-08",
  "numeroPasajeros": 1,
  "emailContacto": "raquel.iglesias@example.com",
  "telefonoContacto": "+34666789012"
}
```

## Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅ → Raquel Iglesias Márquez (ACTIVO)
   - Validar tarjeta ❌       → VISA *6474 EXPIRADA (08/2023) → ERROR_TARJETA_INVALIDA
   - Boundary Event en validar-tarjeta-credito → error gestión cliente
3. Fin: Error en Gestión de Cliente ❌
```

## Iniciar el proceso

### Opción A — Camunda REST API (Swagger)

Accede a http://localhost:8080/swagger-ui/index.html, endpoint `POST /v2/process-instances`, con el body:

```json
{
  "processDefinitionId": "proceso-principal",
  "variables": {
    "clienteId": "0e3e4567-e89b-12d3-a456-426655440015",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 1,
    "emailContacto": "raquel.iglesias@example.com",
    "telefonoContacto": "+34666789012"
  }
}
```

> `processDefinitionId` es el `id` del elemento `<bpmn:process>` — en este caso `proceso-principal`. Lanza siempre la última versión desplegada. No uses `processDefinitionKey` (numérico) a la vez que `processDefinitionId`; son alternativos.

### Opción B — API de `servicio-reservas` (cURL)

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "0e3e4567-e89b-12d3-a456-426655440015",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 1,
    "emailContacto": "raquel.iglesias@example.com",
    "telefonoContacto": "+34666789012"
  }'
```

### Opción C — Tasklist

Accede a http://localhost:8080/operate → pestaña **Processes** → selecciona "Proceso Principal de Reserva de Viaje" → pulsa **Start process**. Se abre el formulario de inicio (`iniciar-reserva`); rellénalo con el `clienteId` de Raquel Iglesias (tarjeta expirada) y envía.

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685255,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

## Logs a consultar

> El PI que devuelve el REST endpoint corresponde al **proceso principal**. Los workers de gestión de cliente corren bajo el PI del **subproceso-gestion-cliente** (un PI hijo distinto). Filtra por el PI hijo que aparece en los logs, no por el de la respuesta HTTP.

**`docker logs servicio-clientes 2>&1 | grep "<PI-subproceso-gestion-cliente>"`**:
```
🔗 Proceso: <PI> [subproceso-gestion-cliente] | Job: <jobKey>
🚀 Iniciando worker obtener-datos-cliente - Job Key: <jobKey>
🔍 Variables recibidas: {clienteId=0e3e4567-e89b-12d3-a456-426655440015, ..., datosValidos=true}
✅ ClienteId validado: 0e3e4567-e89b-12d3-a456-426655440015
🔍 Obteniendo datos del cliente: 0e3e4567-e89b-12d3-a456-426655440015
🔍 Buscando cliente por ID: 0e3e4567-e89b-12d3-a456-426655440015
🔍 Buscando cliente por ID: 0e3e4567-e89b-12d3-a456-426655440015
✅ Cliente encontrado: 0e3e4567-e89b-12d3-a456-426655440015
✅ Cliente encontrado: 0e3e4567-e89b-12d3-a456-426655440015 - Estado: ACTIVO - Email: raquel.iglesias@example.com
✅ Output construido con 13 variables
📤 Datos del cliente preparados - Tarjetas: 1 - Puede reservar: false
🔗 Proceso: <PI> [subproceso-gestion-cliente] | Job: <jobKey>
🚀 Iniciando worker validar-tarjeta-credito - Job Key: <jobKey>
🔍 Variables recibidas: {..., tieneTarjetasValidas=false, puedeRealizarPagos=false}
⚠️ No se proporcionó montoReserva, usando monto por defecto para validación
💳 Validando tarjeta de crédito para cliente: 0e3e4567-e89b-12d3-a456-426655440015
🔍 Buscando cliente por ID: 0e3e4567-e89b-12d3-a456-426655440015
🔍 Buscando cliente por ID: 0e3e4567-e89b-12d3-a456-426655440015
✅ Cliente encontrado: 0e3e4567-e89b-12d3-a456-426655440015
❌ El cliente 0e3e4567-e89b-12d3-a456-426655440015 no tiene tarjetas válidas
```

> El proceso no avanza más allá de la gestión de cliente — no habrá trazas en `reservas` ni `pagos`.

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-error-gestion-cliente`; variable `tarjetaValida=false`
2. **Tasklist** (http://localhost:8080/tasklist): no hay User Tasks pendientes
