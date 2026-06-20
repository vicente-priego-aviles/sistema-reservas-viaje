# Casos de Uso

Ejemplos prácticos de uso del sistema con diferentes escenarios.

---

## Escenarios Disponibles

| # | Escenario | Resultado | Complejidad |
|---|-----------|-----------|-------------|
| 1 | Reserva Exitosa | ✅ Éxito | Básico |
| 2 | Datos de Entrada Inválidos | ❌ Error | Básico |
| 3 | Cliente No Encontrado | ❌ Error | Básico |
| 4 | Cliente Bloqueado | ❌ Error | Básico |
| 5 | Tarjeta Expirada | ❌ Error | Básico |
| 6 | Error en Reserva con Compensación BPMN | 🔄 Compensación | Avanzado |
| 7 | Error en Pago con Compensación por Mensaje | 🔄 Compensación | Avanzado |
| 8 | Advertencia en Actualización | ⚠️ Advertencia | Avanzado |
| 9 | Actualización de Tarjeta en Paralelo | 🔄 Evento No Interrumpible | Avanzado |

---

## Datos de Prueba

### Clientes Precargados (data.sql)

| UUID | Nombre | Estado | Tarjeta | Usar para |
|------|--------|--------|---------|-----------|
| `123e4567-e89b-12d3-a456-426655440000` | Juan Pérez García | ✅ ACTIVO | VISA válida (*0366) | Casos exitosos |
| `223e4567-e89b-12d3-a456-426655440001` | María López Martínez | ✅ ACTIVO | MASTERCARD válida | Casos exitosos |
| `323e4567-e89b-12d3-a456-426655440002` | Carlos Rodríguez Sánchez | ✅ ACTIVO | AMEX válida | Casos exitosos |
| `b23e4567-e89b-12d3-a456-426655440010` | Roberto Morales Gil | 🚫 BLOQUEADO | VISA válida (*5727) | Caso 4: cliente bloqueado |
| `0e3e4567-e89b-12d3-a456-426655440015` | Raquel Iglesias Márquez | ✅ ACTIVO | ❌ VISA expirada (2023) | Caso 5: tarjeta expirada |

### Variables del proceso según monto de pago

El pago se calcula como la suma de `precioVueloFinal + precioHotelFinal + precioCocheFinal`, que son los precios que devuelven los workers de reserva (simulados).

- Límite del sistema: **€10.000**
- Para forzar error de pago (Caso 7): introducir precios que sumen más de €10.000 en los User Tasks de revisión

---

## Cómo iniciar el proceso

Hay dos formas de crear una instancia del proceso `proceso-principal`:

### Opción A: REST API del microservicio (puerto 9090)

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

Swagger UI disponible en: http://localhost:9090/swagger-ui.html

### Opción B: Zeebe REST API directamente (puerto 8088)

Swagger UI: http://localhost:8088/swagger-ui/index.html — endpoint `POST /v2/process-instances`

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

> `processDefinitionId` es el ID del proceso definido en el BPMN (el atributo `id` del elemento `<process>`).
> También puedes usar `processDefinitionKey` con el número que aparece en los logs de arranque de `servicio-reservas`:
> `BpmnDeploymentService - Proceso deployado: proceso-principal | key: <NUMBER>`

---

## ✅ Caso 1: Reserva Exitosa (Happy Path)

### Descripción
Cliente activo con tarjeta válida realiza una reserva completa exitosamente.

### Datos de Entrada

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

### Flujo del Proceso

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

### Ejecutar con cURL

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

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685251,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Logs a consultar

**`./logs.sh clientes`** — validación, gestión de cliente y tarjeta:
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

**`./logs.sh reservas`** — reservas de vuelo, hotel y coche (en paralelo):
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

**`./logs.sh pagos`** — pago y confirmación:
```
🔄 Worker: procesar-pago - Reserva: ... - Monto: ...€
✅ Pago procesado - Transacción: ...
🔄 Worker: confirmar-reserva - Reserva: ...
✅ Reserva confirmada - Número: ...
🔄 Iniciando actualización de estado de cliente - Job: ...
✅ Estado de cliente actualizado correctamente: 123e4567-... → RESERVA_CONFIRMADA
```

### Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia completada en `fin-solicitud-completada`
2. **Tasklist** (http://localhost:8081): completar los User Tasks para avanzar el flujo

---

## ❌ Caso 2: Datos de Entrada Inválidos

### Descripción
Los datos de entrada no superan la validación inicial del proceso. El proceso termina inmediatamente sin llegar a consultar el cliente.

El worker `validar-datos-entrada` rechaza: fechas en el pasado, `fechaFin` anterior a `fechaInicio`, UUID inválido, email con formato incorrecto, `numeroPasajeros` fuera del rango 1–10, o destino vacío.

### Datos de Entrada

```json
{
  "clienteId": "123e4567-e89b-12d3-a456-426655440000",
  "origen": "Madrid",
  "destino": "X",
  "fechaInicio": "2027-06-01",
  "fechaFin": "2027/06/08",
  "numeroPasajeros": 11,
  "emailContacto": "no-es-un-email",
  "telefonoContacto": "+34600123456"
}
```

Este JSON acumula 4 errores simultáneos: `destino` de 1 carácter, formato de `fechaFin` incorrecto (barras en vez de guiones), `numeroPasajeros` fuera del límite máximo de 10, y `emailContacto` inválido.

### Flujo del Proceso

```
1. Validar Datos de Entrada ❌ → ERROR_DATOS_INVALIDOS
2. Fin: Datos Inválidos ❌
   (el proceso termina aquí — no se consulta el cliente ni se crea reserva)
```

### Ejecutar con cURL

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "123e4567-e89b-12d3-a456-426655440000",
    "origen": "Madrid",
    "destino": "X",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027/06/08",
    "numeroPasajeros": 11,
    "emailContacto": "no-es-un-email",
    "telefonoContacto": "+34600123456"
  }'
```

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685252,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

> La API devuelve 202 porque el proceso inicia de forma asíncrona. El error ocurre dentro del motor BPMN inmediatamente después.

### Logs a consultar

**`./logs.sh clientes`** — el worker de validación rechaza los datos:
```
✅ Iniciando validación de datos de entrada - Job: ...
❌ Datos de entrada inválidos - 4 errores encontrados
❌ El formato de 'fechaFin' es inválido. Use yyyy-MM-dd (ejemplo: 2025-12-22)
❌ El 'destino' debe tener al menos 2 caracteres
❌ El 'numeroPasajeros' no puede superar 10
❌ El 'emailContacto' tiene un formato inválido
```

> No habrá trazas en `./logs.sh reservas` ni `./logs.sh pagos` — el proceso no avanza.

### Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en el end event `fin-datos-invalidos`; variables `datosValidos=false` y `erroresValidacion=[...]` visibles
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes

---

## ❌ Caso 3: Cliente No Encontrado

### Descripción
El UUID enviado tiene formato válido pero no corresponde a ningún cliente en la base de datos. El worker `obtener-datos-cliente` devuelve `clienteObtenido=false`; el gateway exclusivo del subproceso enruta al end event de error.

### Datos de Entrada

```json
{
  "clienteId": "00000000-0000-0000-0000-000000000000",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2027-06-01",
  "fechaFin": "2027-06-08",
  "numeroPasajeros": 2,
  "emailContacto": "contacto@example.com",
  "telefonoContacto": "+34600000000"
}
```

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅   (UUID válido en formato)
2. Gestión de Cliente
   - Obtener datos cliente → clienteObtenido=false
   - Gateway: cliente no encontrado → ERROR_CLIENTE_NO_ENCONTRADO
3. Fin: Error en Gestión de Cliente ❌
```

### Ejecutar con cURL

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "00000000-0000-0000-0000-000000000000",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 2,
    "emailContacto": "contacto@example.com",
    "telefonoContacto": "+34600000000"
  }'
```

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685253,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Logs a consultar

**`./logs.sh clientes`**:
```
✅ Iniciando validación de datos de entrada - Job: ...
✅ Datos de entrada válidos - Cliente: 00000000-...
🚀 Iniciando worker obtener-datos-cliente - Job Key: ...
🔍 Obteniendo datos del cliente: 00000000-...
⚠️ Cliente no encontrado en el sistema: 00000000-...
📤 Respuesta preparada: clienteObtenido=false
```

> El proceso no avanza más allá de la gestión de cliente — no habrá trazas en `reservas` ni `pagos`.

### Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-error-gestion-cliente`; variable `clienteObtenido=false`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes

---

## ❌ Caso 4: Cliente Bloqueado

### Descripción
El cliente existe pero está en estado BLOQUEADO. La validación de tarjeta pasa correctamente (la tarjeta en sí es válida), pero al intentar actualizar el estado del cliente a `EN_PROCESO_RESERVA`, el worker lanza `ERROR_CLIENTE_BLOQUEADO`. El error se propaga al proceso padre, que lo captura en el boundary del subproceso de gestión de cliente.

**Nota**: Roberto Morales Gil está BLOQUEADO por "Actividad sospechosa detectada".

### Datos de Entrada

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

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅ → Roberto Morales Gil (BLOQUEADO) - puedeRealizarPagos=false
   - Validar tarjeta ✅       → VISA *5727 — tarjeta válida, pasa la pasarela
   - Actualizar estado ❌     → ERROR_CLIENTE_BLOQUEADO (ACTIVO→EN_PROCESO_RESERVA denegado)
   - Error propagado al proceso padre → boundary en call-activity-gestion-cliente
3. Fin: Error en Gestión de Cliente ❌
```

### Ejecutar con cURL

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

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685254,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Logs a consultar

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

### Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-error-gestion-cliente`; variable `estadoCliente=BLOQUEADO`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes

---

## ❌ Caso 5: Tarjeta Expirada

### Descripción
Cliente activo pero con tarjeta de crédito expirada. El worker `validar-tarjeta-credito` detecta la expiración y lanza `ERROR_TARJETA_INVALIDA`, que es capturado por el boundary event del task de validación dentro del subproceso de gestión de cliente.

**Nota**: Raquel Iglesias Márquez tiene tarjeta VISA expirada (08/2023).

### Datos de Entrada

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

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅ → Raquel Iglesias Márquez (ACTIVO)
   - Validar tarjeta ❌       → VISA *6474 EXPIRADA (08/2023) → ERROR_TARJETA_INVALIDA
   - Boundary Event en validar-tarjeta-credito → error gestión cliente
3. Fin: Error en Gestión de Cliente ❌
```

### Ejecutar con cURL

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

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685255,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Logs a consultar

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

### Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-error-gestion-cliente`; variable `tarjetaValida=false`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes

---

## 🔄 Caso 6: Error en Reserva con Compensación BPMN

### Descripción
Los datos de entrada y la gestión de cliente son válidos, pero al procesar la reserva de vuelo falla la validación de datos del vuelo (`pasajeros=[]`). El subprocess embebido de reserva activa su event subprocess `subproceso-manejo-errores`, que guarda `motivoFallo` y dispara los eventos de compensación BPMN (Saga pattern) para cancelar las reservas completadas. El subprocess termina normalmente; el gateway exclusivo posterior detecta `motivoFallo != null` y enruta hacia la notificación al cliente.

> Este caso difiere del Caso 7: aquí la compensación la activa el error event subprocess del subproceso de reserva, no un mensaje del subproceso de pago.

### Flujo del Proceso

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

### Paso 1: Iniciar el proceso

Hay tres formas equivalentes de iniciar el proceso:

---

**Opción A — API de `servicio-reservas` (curl)**

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

---

**Opción B — Camunda REST API (Swagger)**

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

---

**Opción C — Tasklist**

Accede a http://localhost:8081 → pestaña **Processes** → selecciona "Proceso Principal de Reserva de Viaje" → pulsa **Start process**. Se abre el formulario de inicio (`iniciar-reserva`); rellénalo con los datos del caso y envía. El proceso arranca directamente desde la interfaz sin necesidad de curl ni Swagger.

### Paso 2: Completar "Revisar Datos de Entrada"

En **Tasklist** (http://localhost:8082): aparecerá el User Task "Revisar Datos de Entrada". Complétalo con los datos que aparecen pre-rellenos.

### Paso 3: Obtener el `userTaskKey` del User Task de vuelo

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

### Paso 4: Forzar el error completando el task de vuelo con `pasajeros: []`

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

### Logs a consultar

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

### Verificar en Camunda

1. **Operate** (http://localhost:8081): instancia terminada en `fin-reserva-fallida`; ver el evento de error y los tres eventos de compensación ejecutados; variable `motivoFallo` con el mensaje de error
2. **Tasklist** (http://localhost:8082): no hay User Tasks pendientes

---

## 🔄 Caso 7: Error en Pago con Compensación por Mensaje

### Descripción
Las reservas se completan correctamente pero el pago falla porque el monto total supera el límite del sistema (€10.000). El subproceso de pago lanza `ERROR_PROCESAR_PAGO`, que envía el mensaje `compensar-reserva` al subproceso de reserva. El subproceso de reserva captura el mensaje y ejecuta la compensación manual de las tres reservas.

El error de pago se simula cuando la suma de los precios calculados por los workers supera el límite. Para forzarlo, introducir precios altos al completar los User Tasks de gestión de reserva.

### Datos de Entrada

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

### Flujo del Proceso

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

### Ejecutar con cURL

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

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685257,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Logs a consultar

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

### Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-reserva-no-completada`; ver el subproceso de compensación manual activado; variables `pagoRealizado=false`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes tras el error

---

## ⚠️ Caso 8: Advertencia en Actualización

### Descripción
El pago se procesa correctamente pero falla la actualización del estado del cliente a `RESERVA_CONFIRMADA`. El subproceso de pago ejecuta la compensación parcial (revertir estado y marcar con advertencia). El proceso termina con éxito pero señalizado para intervención manual.

> **Nota de implementación**: el boundary event del BPMN captura el error code `ERROR_ACTUALIZACION_CLIENTE`, pero el worker `actualizar-estado-cliente` actualmente lanza otros códigos (`ERROR_TRANSICION_INVALIDA`, `ERROR_CLIENTE_BLOQUEADO`, etc.). Para que este caso funcione tal como está diseñado en el BPMN, hay dos opciones: (a) modificar el BPMN para que el boundary event sea catch-all (sin error code), o (b) forzar el error directamente vía H2 console como se describe abajo.

### Datos de Entrada

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

### Flujo del Proceso

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

### Ejecutar con cURL

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

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685258,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Variables de Salida

```json
{
  "reservaConfirmada": true,
  "estadoReservaFinal": "CONFIRMADA_CON_ADVERTENCIA",
  "requiereIntervencionManual": true
}
```

### Logs a consultar

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

### Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-reserva-con-advertencia`; variables `reservaConfirmada=true`, `estadoReservaFinal=CONFIRMADA_CON_ADVERTENCIA`, `requiereIntervencionManual=true`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes

---

## 🔄 Caso 9: Actualización de Tarjeta en Paralelo

### Descripción
Durante el proceso de reserva, el cliente actualiza su información de tarjeta de crédito. El evento de mensaje `tarjeta-proporcionada` es **no interrumpible**: activa un subproceso en paralelo sin detener el flujo principal.

La clave de correlación del mensaje es el `clienteId` (no el `processInstanceKey`).

### Datos de Entrada

**Paso 1 — Iniciar la reserva normalmente:**

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

**Paso 2 — Publicar mensaje de actualización de tarjeta** (con el proceso en ejecución y esperando un User Task):

Desde **Zeebe Swagger** (http://localhost:8088/swagger-ui/index.html), endpoint `POST /v2/messages/publication`:

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

### Flujo del Proceso

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

### Ejecutar con cURL

**Paso 1: Iniciar el proceso**

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

**Paso 2: Publicar mensaje** (mientras el proceso espera el User Task "Revisar Datos de Entrada")

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

### Respuesta Esperada

**Paso 1** (iniciar proceso):
```json
{
  "processInstanceKey": 2251799813685259,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

**Paso 2** (publicación del mensaje): respuesta 200/201 de Zeebe confirmando que el mensaje fue publicado y correlacionado.

### Logs a consultar

**`./logs.sh clientes`** — cuando el subproceso paralelo se activa y actualiza la tarjeta:
```
💳 Iniciando actualización de información de tarjeta - Job: ...
🔍 Actualizando tarjeta 11111111-... del cliente 123e4567-...
✅ Fecha de expiración actualizada
✅ Información de tarjeta actualizada correctamente: 11111111-... - Tipo: VISA
```

> El proceso principal continúa sin interrupción — los logs de reservas y pagos aparecen de forma normal (ver Caso 1).

### Verificar en Camunda

1. **Operate** (http://localhost:8080): ver el subproceso `subproceso-actualizar-tarjeta` activo en paralelo al flujo principal; verificar que el proceso principal continúa sin interrumpirse
2. **Tasklist** (http://localhost:8081): completar los User Tasks del flujo principal normalmente

---

## Recursos

- [Quick Start](doc_quick_start.md) — Guía de arranque del sistema
- [Procesos BPMN](doc_procesos_bpmn.md) — Documentación de workflows
- [Checklist de Progreso](checklist_casos_uso.md) — Estado de cada caso probado
- Swagger microservicio reservas: http://localhost:9090/swagger-ui.html
- Swagger Zeebe REST API: http://localhost:8088/swagger-ui/index.html
- Camunda Operate: http://localhost:8081
- Camunda Tasklist: http://localhost:8082

---

**Última actualización**: Junio 2026
