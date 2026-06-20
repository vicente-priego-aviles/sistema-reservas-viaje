# Caso 2: Datos de Entrada Inválidos

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Los datos de entrada no superan la validación inicial del proceso. El proceso termina inmediatamente sin llegar a consultar el cliente.

El worker `validar-datos-entrada` rechaza: fechas en el pasado, `fechaFin` anterior a `fechaInicio`, UUID inválido, email con formato incorrecto, `numeroPasajeros` fuera del rango 1–10, o destino vacío.

## Datos de Entrada

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

## Flujo del Proceso

```
1. Validar Datos de Entrada ❌ → ERROR_DATOS_INVALIDOS
2. Fin: Datos Inválidos ❌
   (el proceso termina aquí — no se consulta el cliente ni se crea reserva)
```

## Ejecutar con cURL

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

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685252,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

> La API devuelve 202 porque el proceso inicia de forma asíncrona. El error ocurre dentro del motor BPMN inmediatamente después.

## Logs a consultar

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

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en el end event `fin-datos-invalidos`; variables `datosValidos=false` y `erroresValidacion=[...]` visibles
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes
