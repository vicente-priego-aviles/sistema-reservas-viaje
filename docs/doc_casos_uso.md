# 💡 Casos de Uso

Ejemplos prácticos de uso del sistema con diferentes escenarios.

---

## 📋 Escenarios Disponibles

| # | Escenario | Resultado | Complejidad |
|---|-----------|-----------|-------------|
| 1 | Reserva Exitosa | ✅ Éxito | Básico |
| 2 | Cliente No Encontrado | ❌ Error | Básico |
| 3 | Tarjeta Inválida | ❌ Error | Básico |
| 4 | Error en Pago con Compensación | 🔄 Compensación | Avanzado |
| 5 | Tarjeta Expirada | ❌ Error | Básico |
| 6 | Advertencia en Actualización | ⚠️ Advertencia | Avanzado |
| 7 | Actualización de Tarjeta en Paralelo | 🔄 Evento No Interrumpible | Avanzado |

---

## ✅ Caso 1: Reserva Exitosa (Happy Path)

### Descripción
Cliente con datos válidos realiza una reserva completa exitosamente.

### Datos de Entrada

```json
{
  "clienteId": "CLI-001",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-01",
  "fechaFin": "2025-12-05",
  "monto": 1500
}
```

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅
   - Cliente encontrado: Vicente Priego
   - Validar tarjeta ✅
   - Actualizar estado: EN_PROCESO_RESERVA ✅
3. Revisar Datos de Entrada (User Task) 👤
4. Proceso de Reserva (Paralelo)
   - Reservar Vuelo ✅ → Revisar Vuelo 👤
   - Reservar Hotel ✅ → Revisar Hotel 👤
   - Reservar Coche ✅ → Revisar Coche 👤
5. Proceso de Pago
   - Procesar Pago ✅
   - Confirmar Reserva ✅
   - Actualizar Estado: CONFIRMADO ✅
6. Fin: Viaje Reservado con Éxito 🎉
```

### Ejecutar con cURL

```bash
curl -X POST http://localhost:9090/api/Pagos/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI-001",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2025-12-01",
    "fechaFin": "2025-12-05",
    "monto": 1500
  }'
```

### Respuesta Esperada

```json
{
  "reservaId": "550e8400-e29b-41d4-a716-446655440000",
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Verificar en Camunda

1. Acceder a **Operate**: http://localhost:8080
2. Ver proceso en ejecución
3. Acceder a **Tasklist**: http://localhost:8081
4. Completar User Tasks

---

## ❌ Caso 2: Cliente No Encontrado

### Descripción
Se intenta reservar con un cliente que no existe en el sistema.

### Datos de Entrada

```json
{
  "clienteId": "CLI-999",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-01",
  "fechaFin": "2025-12-05",
  "monto": 1500
}
```

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ❌
   - Cliente no encontrado
   - End Event: Error Cliente No Encontrado
3. Boundary Event captura error
4. Fin: Error en Gestión de Cliente
```

### Logs Esperados

```
🔍 Iniciando gestión de cliente: CLI-999
🔍 Buscando cliente en base de datos...
❌ Cliente no encontrado: CLI-999
❌ Error en gestión de cliente: ERROR_CLIENTE_NO_ENCONTRADO
```

---

## ❌ Caso 3: Tarjeta Inválida

### Descripción
Cliente existe pero su tarjeta de crédito no pasa la validación.

### Datos de Entrada

```json
{
  "clienteId": "CLI-003",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-01",
  "fechaFin": "2025-12-05",
  "monto": 1500
}
```

**Nota**: CLI-003 (Juan Pérez) tiene tarjeta inválida en datos de prueba.

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅
   - Cliente encontrado: Juan Pérez
   - Validar tarjeta ❌
   - Boundary Event: Error Tarjeta Inválida
   - End Event: Tarjeta Inválida
3. Fin: Error en Gestión de Cliente
```

### Logs Esperados

```
🔍 Validando tarjeta para cliente: CLI-003
🔍 Ejecutando algoritmo de Luhn...
❌ Tarjeta no válida: 1234-5678-9012-3456
❌ Error: ERROR_TARJETA_INVALIDA
```

---

## 🔄 Caso 4: Error en Pago con Compensación

### Descripción
Las Pagos se completan correctamente pero el pago falla, disparando compensaciones de todas las Pagos.

### Datos de Entrada

```json
{
  "clienteId": "CLI-001",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-01",
  "fechaFin": "2025-12-05",
  "monto": 15000
}
```

**Nota**: Monto > 10000 simula error de pago.

### Flujo del Proceso

```
1. Validar Datos ✅
2. Gestión de Cliente ✅
3. Revisar Datos 👤
4. Proceso de Reserva
   - Reservar Vuelo ✅
   - Reservar Hotel ✅
   - Reservar Coche ✅
   - User Tasks completadas 👤
5. Proceso de Pago
   - Procesar Pago ❌ (monto > 10000)
   - Boundary Event: Error Procesar Pago
   - Throw Message Event: Compensar Reserva
   - Notificar Cliente
6. Proceso de Reserva recibe mensaje
   - Subproceso de Compensación Manual
   - Compensar Vuelo 🔄
   - Compensar Hotel 🔄
   - Compensar Coche 🔄
   - Actualizar Registro Cliente
7. Fin: Reserva No Completada
```

### Logs Esperados

```
✅ Vuelo reservado: RV-12345
✅ Hotel reservado: RH-67890
✅ Coche reservado: RC-24680
💳 Procesando pago de 15000€...
❌ Error: Monto excede límite permitido
🔄 Iniciando compensaciones...
🔄 Cancelando vuelo: RV-12345
🔄 Cancelando hotel: RH-67890
🔄 Cancelando coche: RC-24680
✅ Compensaciones completadas
📧 Notificando cliente sobre reserva fallida
```

### Verificar Compensaciones

En **Camunda Operate** verás:
- Subproceso "Manejar Compensación" activado
- Eventos de compensación ejecutados
- Variables actualizadas

---

## ❌ Caso 5: Tarjeta Expirada

### Descripción
Cliente activo pero con tarjeta de crédito expirada. El proceso falla en la validación de tarjeta antes de llegar a la pasarela de pago.

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

**Nota**: Raquel Iglesias (ACTIVO) tiene una tarjeta VISA con últimos 4 dígitos `6474` expirada en 08/2023.

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅
   - Cliente encontrado: ACTIVO - puede reservar: false (tarjeta expirada)
   - Validar tarjeta ❌
   - ERROR_TARJETA_INVALIDA: Todas las tarjetas están expiradas
   - Boundary Event: Error Tarjeta Inválida
   - End Event: Tarjeta Inválida
3. Fin: Error en Gestión de Cliente
```

### Logs a consultar (servicio-clientes)

```bash
docker logs servicio-clientes 2>&1 | grep "<processInstanceKey>"
```

```
🔗 Proceso: <PI> [proceso-principal] | Job: <jobKey>
✅ Iniciando validación de datos de entrada - Job: <jobKey>
🔍 Variables recibidas: [reservaId, numeroPasajeros, clienteId, ...]
✅ Datos de entrada válidos - Cliente: 0e3e4567-e89b-12d3-a456-426655440015
🔗 Proceso: <PI> [subproceso-gestion-cliente] | Job: <jobKey>
🚀 Iniciando worker obtener-datos-cliente - Job Key: <jobKey>
🔍 Obteniendo datos del cliente: 0e3e4567-e89b-12d3-a456-426655440015
✅ Cliente encontrado: 0e3e4567-e89b-12d3-a456-426655440015 - Estado: ACTIVO - Email: raquel.iglesias@example.com
📤 Datos del cliente preparados - Tarjetas: 1 - Puede reservar: false
🔗 Proceso: <PI> [subproceso-gestion-cliente] | Job: <jobKey>
🚀 Iniciando worker validar-tarjeta-credito - Job Key: <jobKey>
💳 Validando tarjeta de crédito para cliente: 0e3e4567-e89b-12d3-a456-426655440015
❌ El cliente 0e3e4567-e89b-12d3-a456-426655440015 no tiene tarjetas válidas
```

**Diferencia con Caso 3 (Tarjeta Inválida)**: el Caso 3 falla por rechazo en la pasarela de pago (algoritmo Luhn o últimos 4 dígitos especiales). El Caso 5 falla antes de llegar a la pasarela porque `esValida()` devuelve `false` al detectar `estaExpirada() = true`.

---

## ⚠️ Caso 6: Advertencia en Actualización

### Descripción
El pago se procesa correctamente pero falla la actualización del estado del cliente, generando una advertencia.

### Datos de Entrada

```json
{
  "clienteId": "CLI-002",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-01",
  "fechaFin": "2025-12-05",
  "monto": 7500
}
```

**Nota**: Monto entre 5000-10000 puede generar advertencia.

### Flujo del Proceso

```
1-4. [Flujo normal hasta Pago]
5. Proceso de Pago
   - Procesar Pago ✅
   - Confirmar Reserva ✅
   - Actualizar Estado: Confirmado ❌
   - Boundary Event: Error Actualización
   - Revertir Estado del Cliente
   - Marcar Reserva con Advertencia
6. Fin: Reserva Confirmada con Advertencia ⚠️
```

### Variables de Salida

```json
{
  "reservaConfirmada": true,
  "estadoReservaFinal": "CONFIRMADA_CON_ADVERTENCIA",
  "requiereIntervencionManual": true,
  "motivo": "Error al actualizar estado tras confirmación"
}
```

---

## 🔄 Caso 7: Actualización de Tarjeta en Paralelo

### Descripción
Durante el proceso de reserva, el cliente actualiza su información de tarjeta de crédito sin interrumpir el flujo principal.

### Paso 1: Iniciar Reserva

```bash
curl -X POST http://localhost:9090/api/Pagos/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI-001",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2025-12-01",
    "fechaFin": "2025-12-05",
    "monto": 1500
  }'

# Guardar reservaId de la respuesta
```

### Paso 2: Actualizar Tarjeta (En Paralelo)

```bash
# Publicar mensaje mientras el proceso está en ejecución
curl -X POST http://localhost:9090/api/mensajes/publicar \
  -H "Content-Type: application/json" \
  -d '{
    "messageName": "tarjeta-proporcionada",
    "correlationKey": "550e8400-e29b-41d4-a716-446655440000",
    "variables": {
      "nuevaTarjeta": "5555-4444-3333-2222",
      "fechaExpiracion": "12/2026"
    }
  }'
```

### Comportamiento

- ✅ El proceso principal **NO se interrumpe**
- 🔄 Se activa el subproceso "Actualizar Información Tarjeta Crédito"
- 💾 Se actualiza la información en paralelo
- ✅ El proceso principal continúa normalmente

### Logs Esperados

```
🔄 Proceso de reserva en curso...
📥 Mensaje recibido: tarjeta-proporcionada
🔄 Iniciando actualización de tarjeta (no interrumpible)...
💾 Actualizando información de tarjeta...
✅ Tarjeta actualizada correctamente
🔄 Proceso principal continúa...
```

---

## 📊 Tabla de Datos de Prueba

### Clientes Precargados

| ID | Nombre | Email | Tarjeta | Resultado Esperado |
|----|--------|-------|---------|-------------------|
| CLI-001 | Vicente Priego | vicente@example.com | ✅ Válida | Reserva exitosa |
| CLI-002 | Verónica Lesmes | veronica@example.com | ✅ Válida | Reserva exitosa |
| CLI-003 | Juan Pérez | juan@example.com | ❌ Inválida | Error tarjeta |
| CLI-999 | - | - | - | Cliente no encontrado |

### Escenarios por Monto

| Monto | Comportamiento |
|-------|----------------|
| < 5000 | ✅ Proceso normal |
| 5000 - 10000 | ⚠️ Genera advertencia si falla actualización |
| > 10000 | ❌ Simula error de pago → Compensación |

---

## 🧪 Testing de Escenarios

### Script de Prueba Automatizado

```bash
#!/bin/bash

echo "🧪 Ejecutando suite de casos de uso..."

# Caso 1: Éxito
echo "✅ Test 1: Reserva exitosa"
curl -X POST http://localhost:9090/api/Pagos/iniciar \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"CLI-001","origen":"Madrid","destino":"Barcelona","fechaInicio":"2025-12-01","fechaFin":"2025-12-05","monto":1500}' \
  -w "\nStatus: %{http_code}\n"

sleep 2

# Caso 2: Cliente no encontrado
echo "❌ Test 2: Cliente no encontrado"
curl -X POST http://localhost:9090/api/Pagos/iniciar \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"CLI-999","origen":"Madrid","destino":"Barcelona","fechaInicio":"2025-12-01","fechaFin":"2025-12-05","monto":1500}' \
  -w "\nStatus: %{http_code}\n"

# ... más casos
```

---

## 📚 Recursos Adicionales

- [Quick Start](01-quick-start.md) - Guía rápida
- [Procesos BPMN](05-procesos-bpmn.md) - Documentación de workflows
- [API Documentation](http://localhost:9090/swagger-ui.html) - OpenAPI

---

**Última actualización**: Diciembre 2024
