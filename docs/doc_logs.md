# Guía de Logs

Referencia para leer, filtrar y correlacionar los logs del sistema con las instancias de procesos en Camunda Operate.

---

## Formato de cada línea de log

```
2026-06-19 17:45:12 [pool-2-thread-1] INFO  d.j.r.p.i.c.ZeebeWorkerContextAspect [PI:2251799814112338] - 🔗 Proceso: 2251799814112338 [subproceso-pago] | Job: 2251799814112420
│                    │                 │     │                                      │                      │
│                    │                 │     └─ Logger (nombre de clase abreviado)  └─ MDC: process inst.  └─ Mensaje
│                    │                 └─ Nivel (INFO / DEBUG / WARN / ERROR)
│                    └─ Hilo de ejecución
└─ Timestamp
```

### Sección `[PI:...]`

El prefijo `[PI:N]` contiene el **processInstanceKey** de la instancia del proceso Zeebe que originó la línea. Ese número es el identificador directo que puedes pegar en Camunda Operate para ver el estado de la instancia.

- `[PI:?]` → la línea no viene de un worker (p.ej. arranque del servicio, petición REST).
- `[PI:2251799814112338]` → la línea fue emitida mientras se procesaba la instancia con esa clave.

### Nombres de logger abreviados

`%logger{36}` trunca los paquetes a su inicial. Ejemplos:

| Abreviatura | Clase completa |
|-------------|----------------|
| `d.j.r.c.i.c.ZeebeWorkerContextAspect` | `dev.javacadabra.reservasviaje.cliente.infraestructura.configuracion.ZeebeWorkerContextAspect` |
| `d.j.r.p.i.c.ZeebeWorkerContextAspect` | `dev.javacadabra.reservasviaje.pago.infraestructura.configuracion.ZeebeWorkerContextAspect` |
| `d.j.r.r.i.c.ZeebeWorkerContextAspect` | `dev.javacadabra.reservasviaje.reserva.infraestructura.configuracion.ZeebeWorkerContextAspect` |
| `d.j.r.c.i.a.e.c.ValidarDatosEntradaWorker` | `dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda.ValidarDatosEntradaWorker` |
| `d.j.r.c.a.s.ClienteServicio` | `dev.javacadabra.reservasviaje.cliente.aplicacion.servicio.ClienteServicio` |

---

## Línea de contexto por worker

Antes de cada worker, el aspecto `ZeebeWorkerContextAspect` emite automáticamente una línea con el contexto del proceso:

```
🔗 Proceso: <processInstanceKey> [<bpmnProcessId>] | Job: <jobKey>
```

Ejemplo real:
```
2026-06-19 17:45:12 [pool-2-thread-1] INFO  d.j.r.p.i.c.ZeebeWorkerContextAspect [PI:2251799814112338] - 🔗 Proceso: 2251799814112338 [subproceso-pago] | Job: 2251799814112420
```

Esta línea te dice:
- **processInstanceKey** (`2251799814112338`): ID de la instancia. Úsalo en Operate.
- **bpmnProcessId** (`subproceso-pago`): qué subproceso está ejecutando el worker.
- **jobKey** (`2251799814112420`): ID del job concreto dentro de esa instancia.

---

## Ver logs por servicio

```bash
./logs.sh clientes    # validación, gestión de cliente y tarjeta
./logs.sh reservas    # reservas de vuelo, hotel y coche (+ compensaciones)
./logs.sh pagos       # procesamiento de pago y confirmación
```

Los dos servicios sin workers (`servicio-vuelos`, `servicio-hoteles`, `servicio-alquiler-coches`) solo reciben llamadas REST desde los workers de `servicio-reservas`; sus logs se ven con:
```bash
./logs.sh vuelos
./logs.sh hoteles
./logs.sh coches
```

---

## Filtrar logs por instancia de proceso

Para seguir una reserva concreta en todos los servicios, copia el `processInstanceKey` que aparece en la primera línea `🔗 Proceso:` y filtra:

```bash
# Ver todas las líneas de una instancia en clientes
docker logs servicio-clientes 2>&1 | grep "2251799814112338"

# Ver solo las líneas de contexto (una por worker)
docker logs servicio-clientes 2>&1 | grep "🔗 Proceso"

# Ver errores de una instancia concreta
docker logs servicio-pagos 2>&1 | grep "2251799814112338" | grep "ERROR\|❌"
```

---

## Correlacionar con Camunda Operate

1. Ejecuta el caso de uso con cURL (ver [doc_casos_uso.md](doc_casos_uso.md)).
2. Anota el `processInstanceKey` de la respuesta HTTP:
   ```json
   { "processInstanceKey": 2251799814112338, "estado": "INICIADA", ... }
   ```
3. Abre **Operate** en http://localhost:8080.
4. En el filtro de instancias escribe el `processInstanceKey` → verás la instancia directamente.
5. El mismo número aparece en `[PI:N]` en todos los logs de esa instancia, por lo que puedes navegar libremente entre Operate y el terminal.

---

## Flujo de logs por subproceso

En el happy path, las líneas `🔗 Proceso:` aparecen en este orden:

| Orden | Servicio | bpmnProcessId |
|-------|---------|---------------|
| 1 | `clientes` | `subproceso-gestion-cliente` |
| 2 | `reservas` | `subproceso-proceso-reserva` (× 3, en paralelo) |
| 3 | `pagos` | `subproceso-pago` |
| 4 | `clientes` | `subproceso-pago` (worker de actualización de estado final) |

Nota: el `bpmnProcessId` que muestra el aspecto es el del **subproceso** que contiene el service task, no el `proceso-principal`. Para ver la instancia raíz en Operate, usa el `processInstanceKey` del subproceso y navega al proceso padre desde la UI.

---

## Implementación

Los logs de contexto están implementados mediante:

- **`ZeebeWorkerContextAspect`** en el paquete `infraestructura/configuracion/` de cada microservicio con workers (`servicio-clientes`, `servicio-pagos`, `servicio-reservas`). El aspecto intercepta todos los métodos `@JobWorker`, establece el MDC y emite la línea `🔗 Proceso:` antes de delegar al worker.
- **Patrón de log** en `application.yml` de cada servicio:
  ```
  %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [PI:%X{processInstanceKey:-?}] - %msg%n
  ```
