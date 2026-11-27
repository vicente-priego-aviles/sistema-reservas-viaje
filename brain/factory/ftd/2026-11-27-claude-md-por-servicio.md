---
type: FTD
title: CLAUDE.md por microservicio
description: El CLAUDE.md raíz superaba las 120 líneas; el detalle de cada microservicio pasa a su propio servicio-*/CLAUDE.md, documentando lo que hay hoy en el código.
tags: [ftd, documentacion]
estado: hecho
rama: 67-chore-instalar-harness
riesgo: 2
generated: { by: agent:claude-opus-5-5, at: 2026-11-27 }
---

# CLAUDE.md por microservicio

## Intención
El `CLAUDE.md` raíz tenía 151 líneas y mezclaba convenciones comunes con detalle de cada módulo. Se reparte para que el raíz baje de 120 líneas y cada `servicio-*/CLAUDE.md` se cargue solo al trabajar en su módulo. Incidencia #67.

## Alcance
- `CLAUDE.md` raíz: solo lo común (comandos, estructura hexagonal, cómo se escribe un worker, configuración de Zeebe, Lombok + MapStruct, estado de los tests) y una tabla de módulos que enlaza a cada subarchivo. El bloque `mh:inicio`/`mh:fin` no se toca.
- Un `CLAUDE.md` nuevo en cada uno de los seis `servicio-*`: puerto, base de datos, paquete, agregados, workers, endpoints y particularidades, **tal como están hoy en el código** (decisión de Vicente, 2026-11-27).
- Las discrepancias entre la documentación/constitución y el código se anotan en `decisiones-pendientes.md`, sin corregirlas.

## Fuera de alcance
- Corregir el código, los BPMN, la constitución o el `README.md` de `servicio-reservas`.
- Traducir al español el texto heredado del `CLAUDE.md` raíz (se mantiene en inglés). Los `servicio-*/CLAUDE.md` nuevos se escriben en español, a petición de Vicente.

## Checklist
- [x] El raíz baja de 120 líneas — prueba: `wc -l CLAUDE.md`
- [x] Existe un `CLAUDE.md` por microservicio — prueba: `ls servicio-*/CLAUDE.md`
- [x] Los datos de cada subarchivo coinciden con el código — prueba: `grep` de `@JobWorker`, `server.port`, `jdbc:h2`, `BpmnError`, mappings REST
- [x] El bloque meta-harness queda intacto — prueba: `diff` del bloque antes/después
- [x] Controles: `node .mh/scripts/mvnq.mjs verify`

## Evidencia
> [!success] El raíz baja de 120 líneas
> `wc -l CLAUDE.md` → 116 (antes 151).

> [!success] Un CLAUDE.md por microservicio
> `ls servicio-*/CLAUDE.md` → alquiler-coches (11 líneas), clientes (26 líneas), hoteles (11 líneas), pagos (22 líneas), reservas (47 líneas), vuelos (11 líneas),

> [!success] Los datos coinciden con el código
> Contrastados con `grep`: puertos y bases H2 en cada `application.yml`; los 27 `@JobWorker` (15 en clientes, 6 en reservas, 5 en pagos, 0 en vuelos/hoteles/coches); los códigos `ERROR_*` de cada worker de reservas y pagos; los 53 `throw BpmnError.bpmnError(`; los mappings REST (`/api/reservas/iniciar`, `/dev/clientes/...`); 0 archivos en `src/test` de cualquier módulo.

> [!success] Bloque meta-harness intacto
> `diff` del bloque `mh:inicio`…`mh:fin` entre `HEAD` y el archivo nuevo → sin diferencias.

> [!success] Controles
> `node .mh/scripts/mvnq.mjs verify` → BUILD SUCCESS, código 0 (los 7 módulos del reactor).

> [!info] No ejecutado
> Tests de ArchUnit: no ejecutado (no existen; C1 y C2 siguen pendientes según la constitución). Revisión de contexto limpio: no ejecutada (riesgo 2 autorizado en FTD sin panel).

## Notas
> [!warning] Riesgo 2 autorizado como excepción
> `riesgo.mjs` da 2 porque `CLAUDE.md` está en la lista de rutas sensibles (los `servicio-*/CLAUDE.md` y las notas del brain dan 0). El cambio es solo documentación y su alcance lo confirmó Vicente; el 2026-11-27 decidió seguir en FTD en vez de subir a ASDD.

Correcciones respecto al `CLAUDE.md` anterior, comprobadas en el código: `servicio-reservas` sí tiene workers (reservar/cancelar vuelo, hotel y coche); `servicio-vuelos`, `servicio-hoteles` y `servicio-alquiler-coches` no tienen ninguno; no hay ningún test en el repo; los errores de negocio se lanzan con `BpmnError.bpmnError(...)`, no con `ZeebeBpmnError`.
