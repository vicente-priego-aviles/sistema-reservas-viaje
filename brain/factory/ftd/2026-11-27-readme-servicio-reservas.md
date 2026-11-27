---
type: FTD
title: README propio de servicio-reservas
description: servicio-reservas/README.md describía el servicio de pagos; se reescribe con lo que hay hoy en el código del módulo.
tags: [ftd, documentacion, servicio-reservas]
estado: hecho
rama: 67-chore-instalar-harness
riesgo: 0
generated: { by: agent:claude-opus-5-5, at: 2026-11-27 }
---

# README propio de servicio-reservas

## Intención
Resolver la decisión pendiente D4: el README del módulo era una copia del de pagos (título, descripción, endpoints y versiones que no existen). Vicente eligió reescribirlo (2026-11-27).

## Alcance
- `servicio-reservas/README.md` reescrito en español: qué hace, API REST, procesos BPMN y saga, workers, dominio, configuración, ejecución, tests y particularidades.
- Corrección en `servicio-reservas/CLAUDE.md`: la reserva va incrustada en `proceso-principal` y `subproceso-reserva.bpmn` no lo invoca nadie (lo escrito en el FTD anterior decía que eran call activities).
- D4 marcada como resuelta y D2 ampliada con el hallazgo del subproceso sin invocar.

## Fuera de alcance
- Tocar los BPMN o el código.
- El `CLAUDE.md` raíz (riesgo 2).

## Checklist
- [x] Cada dato del README sale del código — prueba: `application.yml`, `IniciarReservaController`/`IniciarReservaDTO`, `BpmnDeploymentService`, `grep` de `@JobWorker`, `BpmnError`, `calledElement` y `compensateEventDefinition` en los `.bpmn`
- [x] Riesgo 0 — prueba: `node .mh/scripts/riesgo.mjs`
- [x] Controles: `node .mh/scripts/mvnq.mjs verify`

## Evidencia
> [!success] Datos sacados del código
> Puerto, H2, consola, OpenAPI y Actuator de `application.yml`; ruta y `202 Accepted` de `IniciarReservaController`; campos y validaciones de `IniciarReservaDTO`; orden de despliegue y lote `proceso-principal` + `iniciar-reserva.form` de `BpmnDeploymentService`; 6 `@JobWorker` y sus códigos `BpmnError`; en los `.bpmn`, `calledElement` solo a `subproceso-gestion-cliente` y `subproceso-pago`, ninguna referencia a `subproceso-proceso-reserva`, y 9 `compensateEventDefinition` en `proceso-principal`; variables Docker de `docker-compose.yml`; versiones del `pom.xml` raíz (Java 25, Spring Boot 4.1.0, Camunda 8.9.6).

> [!success] Riesgo 0
> `node .mh/scripts/riesgo.mjs` con los 4 archivos tocados → riesgo 0, documentación. (Sin argumentos mide toda la rama frente a `develop`, que incluye la instalación del harness.)

> [!success] Controles
> `node .mh/scripts/mvnq.mjs verify` → BUILD SUCCESS, código 0.

> [!info] No ejecutado
> Tests de ArchUnit: no ejecutado (no existen). Prueba manual de `POST /api/reservas/iniciar`: no ejecutada (no se ha levantado Camunda).

## Notas
Hallazgo fuera de alcance: el `CLAUDE.md` raíz citaba `CAMUNDA_CLIENT_ZEEBE_GATEWAYADDRESS`, pero `docker-compose.yml` usa `CAMUNDA_CLIENT_ZEEBE_GRPCADDRESS` en todos los servicios. Corregido después en un commit aparte (riesgo 2 por la ruta, autorizado por Vicente el 2026-11-27 como excepción en FTD).
