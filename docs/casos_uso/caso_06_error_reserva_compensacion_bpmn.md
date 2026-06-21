# Caso 6: Error en Reserva con Compensación BPMN

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Los datos de entrada y la gestión de cliente son válidos, pero al procesar la reserva de vuelo falla la validación de datos del vuelo (`pasajeros=[]`). El subprocess embebido de reserva activa su event subprocess `subproceso-manejo-errores`, que guarda `motivoFallo` y dispara los eventos de compensación BPMN (Saga pattern) para cancelar las reservas completadas. El subprocess termina normalmente; el gateway exclusivo posterior detecta `motivoFallo != null` y enruta hacia la notificación al cliente.

> Este caso difiere del Caso 7: aquí la compensación la activa el error event subprocess del subproceso de reserva, no un mensaje del subproceso de pago.

## Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente ✅       → Juan Pérez García, ACTIVO → EN_PROCESO_RESERVA
3. Revisar Datos de Entrada (User Task) 👤
4. Proceso de Reserva (Paralelo, subprocess embebido)
   - Introducir Datos del Vuelo (User Task) 👤 → completar con pasajeros=[]
   - Reservar Vuelo ❌  → ERROR_VALIDACION_VUELO (lista de pasajeros vacía)
   - Event subprocess "Manejar Error Reserva" se activa:
     - Guarda motivoFallo = "La lista de pasajeros no puede estar vacía"
     - Compensar Vuelo 🔄  → ⚠️ no hay reservaVueloId (no llegó a reservarse)
     - Compensar Hotel 🔄  → ⚠️ no hay reservaHotelId
     - Compensar Coche 🔄  → ⚠️ no hay reservaCocheId
   - El subprocess termina normalmente (end event normal, no error)
5. Gateway "¿Reserva Exitosa?" → motivoFallo != null → rama de notificación
6. Notificar Cliente: Reserva Fallida 📧
7. Fin: Reserva Fallida ❌
```

---

## Paso 1: Iniciar el proceso

Hay tres formas equivalentes de iniciar el proceso:

### Opción A — Camunda REST API (Swagger)

Accede a http://localhost:8088/swagger-ui/index.html, endpoint `POST /v2/process-instances`, con el body:

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

Respuesta:

```json
{
  "processInstanceKey": 2251799813685256,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Opción C — Tasklist

Accede a http://localhost:8081 → pestaña **Processes** → selecciona "Proceso Principal de Reserva de Viaje" → pulsa **Start process**. Se abre el formulario de inicio (`iniciar-reserva`); rellénalo con los datos del caso y envía. El proceso arranca directamente desde la interfaz sin necesidad de cURL ni Swagger.

---

## Paso 2: Completar "Revisar Datos de Entrada"

En **Tasklist** (http://localhost:8082): aparecerá el User Task "Revisar Datos de Entrada". Complétalo con los datos que aparecen pre-rellenos.

---

## Paso 3: Obtener el `userTaskKey` del User Task de vuelo

Tras completar el paso anterior, el proceso avanza al subproceso de reserva y quedan activos tres User Tasks en paralelo (vuelo, hotel, coche). Necesitas el `userTaskKey` del task de vuelo para completarlo con `pasajeros: []` y forzar el error.

**Opción A — Tasklist** (http://localhost:8082):

1. Ve a la sección **Tasks**
2. Localiza "✈️ Introducir Datos del Vuelo"
3. El `userTaskKey` es el número que aparece en la URL al hacer click sobre el task: `.../tasks/<userTaskKey>`

**Opción B — Operate** (http://localhost:8081):

1. Entra en la instancia del proceso activa
2. Haz click sobre el nodo "✈️ Introducir Datos del Vuelo" (aparece resaltado en azul)
3. Se abre un popup — haz click en el enlace **View**
4. En la vista de detalle del nodo verás la metadata; el campo **Key** es el `userTaskKey`

---

## Paso 4: Forzar el error completando el task de vuelo con `pasajeros: []`

Sustituye `<userTaskKey>` por el valor obtenido en el paso anterior:

```bash
curl -X PATCH "http://localhost:8088/v2/user-tasks/<userTaskKey>/completion" \
  -H "Content-Type: application/json" \
  -d '{
    "variables": {
      "clienteId": "123e4567-e89b-12d3-a456-426655440000",
      "numeroVuelo": "IB1234",
      "aerolinea": "Iberia",
      "origen": "Madrid",
      "destino": "Barcelona",
      "fechaSalida": "2027-06-01T10:00:00+02:00",
      "fechaLlegada": "2027-06-01T11:00:00+02:00",
      "clase": "ECONOMICA",
      "precioVuelo": 150.0,
      "pasajeros": []
    }
  }'
```

> También puedes usar el Swagger de Zeebe en http://localhost:8088/swagger-ui/index.html, endpoint `PATCH /v2/user-tasks/{userTaskKey}/completion`.

**Alternativa — formulario en Tasklist**: abre el task "✈️ Introducir Datos del Vuelo" en Tasklist (http://localhost:8082), rellena el formulario con los datos del vuelo y deja el campo **Pasajeros** vacío (sin añadir ningún pasajero). Al enviar, el formulario completará el task con `pasajeros: []` con el mismo efecto.

Esto provoca `ERROR_VALIDACION_VUELO` en el worker `reservar-vuelo`, que activa el Saga de compensación.

---

## Logs a consultar

> El PI de la respuesta HTTP corresponde al **proceso principal**. La gestión de cliente corre bajo el PI del **subproceso-gestion-cliente** y la reserva bajo el PI del **subproceso-proceso-reserva** (ambos son hijos distintos).

**`docker logs servicio-clientes 2>&1 | grep "<PI-subproceso-gestion-cliente>"`** — gestión de cliente exitosa:

> `subproceso-gestion-cliente` es un **call activity** (proceso hijo independiente), así que su PI es distinto al del proceso principal. Encuéntralo en Operate entrando en la instancia → child process instances, o buscando `[subproceso-gestion-cliente]` en los logs de `servicio-clientes`.

```
🔗 Proceso: <PI-hijo> [subproceso-gestion-cliente] | Job: <jobKey>
🚀 Iniciando worker obtener-datos-cliente - Job Key: <jobKey>
🔍 Obteniendo datos del cliente: 123e4567-e89b-12d3-a456-426655440000
✅ Cliente encontrado: 123e4567-e89b-12d3-a456-426655440000 - Estado: ACTIVO - Email: juan.perez@example.com
📤 Datos del cliente preparados - Tarjetas: 1 - Puede reservar: true
🔗 Proceso: <PI> [subproceso-gestion-cliente] | Job: <jobKey>
🚀 Iniciando worker validar-tarjeta-credito - Job Key: <jobKey>
💳 Validando tarjeta de crédito para cliente: 123e4567-e89b-12d3-a456-426655440000
🔍 Tarjeta seleccionada: 11111111-1111-1111-1111-000000000001 - Tipo: VISA - Últimos 4 dígitos: 0366
✅ Fecha de expiración válida: 12/2027
🔐 Simulando validación con pasarela de pago - Monto: 1000.0€
✅ Pasarela de pago: Transacción APROBADA - Código: <código>
✅ Tarjeta validada correctamente - Código autorización: <código>
📤 Validación completada exitosamente
🔗 Proceso: <PI> [subproceso-gestion-cliente] | Job: <jobKey>
🔄 Iniciando actualización de estado de cliente - Job: <jobKey>
🔍 Actualizando estado de cliente: 123e4567-e89b-12d3-a456-426655440000 → Estado nuevo: EN_PROCESO_RESERVA - Reserva: <reservaId>
🚀 Proceso de reserva iniciado para cliente: 123e4567-e89b-12d3-a456-426655440000 - Reserva: <reservaId>
✅ Estado de cliente actualizado correctamente: 123e4567-e89b-12d3-a456-426655440000 - ACTIVO → EN_PROCESO_RESERVA
```

**`docker logs servicio-reservas 2>&1 | grep "<PI-proceso-principal>"`** — fallo en validación del vuelo y compensaciones BPMN:

> ⚠️ El subprocess de reserva es **embebido** (no un call activity), por lo que los workers se ejecutan dentro del mismo PI del proceso principal. El contexto que muestra el log es `[proceso-principal]`, no `[subproceso-proceso-reserva]`. Usa el PI devuelto por la API de inicio (`processInstanceKey`) directamente.

```
🔗 Proceso: <PI> [proceso-principal] | Job: <jobKey>
🚀 Iniciando worker de reserva de vuelo - Job Key: <jobKey>
🔍 Variables recibidas: {..., pasajeros=[], numeroVuelo=IB1234, aerolinea=Iberia,
    precioVuelo=150.0, clase=ECONOMICA, codigoAutorizacion=5AEDAE,
    estadoCliente=EN_PROCESO_RESERVA, ...}
   (el worker recibe TODO el scope del proceso principal: variables de cliente,
    de validación de tarjeta, del formulario de vuelo y las del inicio de reserva)
❌ Error de validación en reserva de vuelo: La lista de pasajeros no puede estar vacía
🛑 Iniciando worker de cancelación de vuelo (compensación) - Job Key: ...
⚠️ No se encontró ID de reserva de vuelo para cancelar. La reserva puede no haberse creado.
🛑 Iniciando worker de cancelación de hotel (compensación) - Job Key: ...
⚠️ No se encontró ID de reserva de hotel para cancelar. La reserva puede no haberse creado.
🛑 Iniciando worker de cancelación de coche (compensación) - Job Key: ...
⚠️ No se encontró ID de reserva de coche para cancelar. La reserva puede no haberse creado.
```

> Los mensajes `⚠️ No se encontró ID de reserva de X para cancelar` son **esperados** en este caso: el error ocurre antes de que ninguna reserva llegue a completarse, así que no hay nada que cancelar. Los workers de compensación finalizan limpiamente sin hacer llamadas al sistema externo.

**`docker logs servicio-clientes 2>&1 | grep "<PI-proceso-principal>"`** — validación inicial y notificación del fallo:

> El `NotificarReservaFallidaWorker` también pertenece a `servicio-clientes` y corre bajo el mismo PI del proceso principal. El mismo grep cubre tanto la validación de entrada (inicio del proceso) como la notificación final.

```
🔗 Proceso: <PI> [proceso-principal] | Job: <jobKey>
✅ Iniciando validación de datos de entrada - Job: <jobKey>
🔍 Variables recibidas: [reservaId, numeroPasajeros, clienteId, fechaInicio, ...]
✅ Datos de entrada válidos - Cliente: 123e4567-e89b-12d3-a456-426655440000

(~5 minutos después, tras los user tasks y el fallo en el worker de reserva)

🔗 Proceso: <PI> [proceso-principal] | Job: <jobKey>
📨 Iniciando notificación de reserva fallida - Job: <jobKey>
📧 Notificando fallo de reserva - Cliente: 123e4567-... - Reserva: <reservaId>
    - Motivo: Error de validación en los datos del vuelo: La lista de pasajeros no puede estar vacía
✅ Notificación de reserva fallida procesada correctamente
📝 Mensaje generado: Estimado/a cliente, ...
```

> El campo `motivoFallo` tiene el formato `"Error de validación en los datos del vuelo: <mensaje original>"`. El worker añade el prefijo contextual al lanzar el `ZeebeBpmnError` desde `ReservaVueloWorker`.

---

## Verificar en Camunda

1. **Operate** (http://localhost:8081): instancia terminada en `fin-reserva-fallida`; ver el evento de error y los tres eventos de compensación ejecutados; variable `motivoFallo` con el mensaje de error
2. **Tasklist** (http://localhost:8082): no hay User Tasks pendientes
