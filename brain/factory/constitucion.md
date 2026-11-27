---
type: Constitución
title: Constitución de sistema-reservas-viaje
description: Reglas fijas del proyecto y definición de terminado; cambiarlas requiere confirmación.
tags: [constitucion, factoria]
generated: { by: meta-harness-factory, at: 2026-11-27 }
status: draft
---

# Constitución

> [!warning] Borrador
> Generado por el instalador. Revísalo con el agente (skill `asdd`, paso 0) y márcalo `status: stable`.

## 1. Stack canónico
- Build: maven (Maven Wrapper)
- Spring Boot (versión en el pom.xml)
- Camunda 8 (versión en el pom.xml)
- jMolecules (DDD) + ArchUnit
- Flyway (migraciones)

## 2. Arquitectura
- C1. El dominio no depende de Spring ni de infraestructura (lo comprueba ArchUnit).

## 3. Procesos
- P1. Un proceso BPMN desplegado es un contrato: cambiarlo es riesgo 2.

## 4. Calidad
- Q1. TDD; ningún test se debilita ni desactiva para que pase.

## 5. Seguridad
- S1. Ningún secreto en el repositorio.

## 6. Git
- G1. Riesgo 2 → PR con aprobación humana.
- G2. Código, commits, issues y PR en español; notas del brain en español.

## 7. Definición de terminado
- [ ] `node .mh/scripts/mvnq.mjs verify` en verde
- [ ] Evidencia del comportamiento pedido (FTD o verificación)
- [ ] Controles no ejecutados declarados como «no ejecutado»

## 8. Registro de enmiendas
| Fecha | Cambio | Aprobado por |
|---|---|---|
| 2026-11-27 | Borrador inicial | — |
