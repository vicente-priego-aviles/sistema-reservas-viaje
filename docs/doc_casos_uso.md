# Casos de Uso

Ejemplos prácticos de uso del sistema con diferentes escenarios.

---

## Escenarios Disponibles

| # | Escenario | Resultado | Complejidad | Documento |
|---|-----------|-----------|-------------|-----------|
| 1 | Reserva Exitosa | ✅ Éxito | Básico | [caso_01](casos_uso/caso_01_reserva_exitosa.md) |
| 2 | Datos de Entrada Inválidos | ❌ Error | Básico | [caso_02](casos_uso/caso_02_datos_invalidos.md) |
| 3 | Cliente No Encontrado | ❌ Error | Básico | [caso_03](casos_uso/caso_03_cliente_no_encontrado.md) |
| 4 | Cliente Bloqueado | ❌ Error | Básico | [caso_04](casos_uso/caso_04_cliente_bloqueado.md) |
| 5 | Tarjeta Expirada | ❌ Error | Básico | [caso_05](casos_uso/caso_05_tarjeta_expirada.md) |
| 6 | Error en Reserva con Compensación BPMN | 🔄 Compensación | Avanzado | [caso_06](casos_uso/caso_06_error_reserva_compensacion_bpmn.md) |
| 7 | Error en Pago con Compensación por Mensaje | 🔄 Compensación | Avanzado | [caso_07](casos_uso/caso_07_error_pago_compensacion_mensaje.md) |
| 8 | Advertencia en Actualización | ⚠️ Advertencia | Avanzado | [caso_08](casos_uso/caso_08_advertencia_actualizacion.md) |
| 9 | Actualización de Tarjeta en Paralelo | 🔄 Evento No Interrumpible | Avanzado | [caso_09](casos_uso/caso_09_actualizacion_tarjeta_paralelo.md) |

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
