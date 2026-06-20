# Caso 3: Cliente No Encontrado

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

El UUID enviado tiene formato válido pero no corresponde a ningún cliente en la base de datos. El worker `obtener-datos-cliente` devuelve `clienteObtenido=false`; el gateway exclusivo del subproceso enruta al end event de error.

## Datos de Entrada

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

## Flujo del Proceso

```
1. Validar Datos de Entrada ✅   (UUID válido en formato)
2. Gestión de Cliente
   - Obtener datos cliente → clienteObtenido=false
   - Gateway: cliente no encontrado → ERROR_CLIENTE_NO_ENCONTRADO
3. Fin: Error en Gestión de Cliente ❌
```

## Ejecutar con cURL

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

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685253,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

## Logs a consultar

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

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-error-gestion-cliente`; variable `clienteObtenido=false`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes
