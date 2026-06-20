# Caso 9: Actualización de Tarjeta en Paralelo

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Durante el proceso de reserva, el cliente actualiza su información de tarjeta de crédito. El evento de mensaje `tarjeta-proporcionada` es **no interrumpible**: activa un subproceso en paralelo sin detener el flujo principal.

La clave de correlación del mensaje es el `clienteId` (no el `processInstanceKey`).

## Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente ✅
3. Revisar Datos de Entrada (User Task) 👤  ← proceso principal queda esperando aquí
   │
   └── [Mensaje "tarjeta-proporcionada" llega con correlationKey=clienteId]
       └── Subproceso NO INTERRUMPIBLE se activa en paralelo:
           - Actualizar Información Tarjeta ✅
           - Fin subproceso paralelo
   │
   (proceso principal no se interrumpe)
4. Proceso de Reserva (Paralelo) ✅
5. Proceso de Pago ✅
6. Fin: Solicitud de Viaje Completada ✅
```

---

## Paso 1: Iniciar el proceso

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

Respuesta:

```json
{
  "processInstanceKey": 2251799813685259,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

---

## Paso 2: Publicar mensaje de actualización de tarjeta

Mientras el proceso espera el User Task "Revisar Datos de Entrada", publicar el mensaje desde **Zeebe Swagger** (http://localhost:8088/swagger-ui/index.html), endpoint `POST /v2/messages/publication`:

```json
{
  "messageName": "tarjeta-proporcionada",
  "correlationKey": "123e4567-e89b-12d3-a456-426655440000",
  "variables": {
    "tarjetaId": "11111111-1111-1111-1111-000000000001",
    "nuevaFechaExpiracion": "12/2029"
  }
}
```

> `correlationKey` es el `clienteId` de Juan Pérez. `tarjetaId` es el ID de su tarjeta VISA precargada en data.sql.

O vía cURL:

```bash
curl -X POST http://localhost:8088/v2/messages/publication \
  -H "Content-Type: application/json" \
  -d '{
    "messageName": "tarjeta-proporcionada",
    "correlationKey": "123e4567-e89b-12d3-a456-426655440000",
    "variables": {
      "tarjetaId": "11111111-1111-1111-1111-000000000001",
      "nuevaFechaExpiracion": "12/2029"
    }
  }'
```

**Respuesta**: 200/201 de Zeebe confirmando que el mensaje fue publicado y correlacionado.

---

## Logs a consultar

**`./logs.sh clientes`** — cuando el subproceso paralelo se activa y actualiza la tarjeta:
```
💳 Iniciando actualización de información de tarjeta - Job: ...
🔍 Actualizando tarjeta 11111111-... del cliente 123e4567-...
✅ Fecha de expiración actualizada
✅ Información de tarjeta actualizada correctamente: 11111111-... - Tipo: VISA
```

> El proceso principal continúa sin interrupción — los logs de reservas y pagos aparecen de forma normal (ver [Caso 1](caso_01_reserva_exitosa.md)).

## Verificar en Camunda

1. **Operate** (http://localhost:8080): ver el subproceso `subproceso-actualizar-tarjeta` activo en paralelo al flujo principal; verificar que el proceso principal continúa sin interrumpirse
2. **Tasklist** (http://localhost:8081): completar los User Tasks del flujo principal normalmente
