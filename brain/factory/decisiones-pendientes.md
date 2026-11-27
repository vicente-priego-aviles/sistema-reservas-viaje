---
type: Cola
title: Decisiones pendientes
description: Lo que el agente no debe decidir solo; bloqueante detiene, revisar-después permite seguir.
tags: [factoria, decisiones]
---

# Decisiones pendientes

<!-- - [bloqueante] D1 — <spec>/<tarea>: <qué decidir>. Opciones: A) … B) …. Recomendación: … -->

- [revisar-después] D1 — ftd/2026-11-27-claude-md-por-servicio: la constitución (C4) y el antiguo `CLAUDE.md` dicen que `servicio-reservas` no tiene workers Zeebe, pero tiene seis (`reservar-*`, `cancelar-*` de vuelo, hotel y coche), y `servicio-vuelos`, `servicio-hoteles` y `servicio-alquiler-coches` no tienen ninguno. Opciones: A) mover los workers a sus servicios para cumplir C4; B) enmendar C4 para reflejar el código. Recomendación: A, a medio plazo (cada servicio dueño de sus reservas), con ASDD.
- [revisar-después] D2 — ftd/2026-11-27-claude-md-por-servicio: `bpmn/` no tiene `subproceso-reserva.bpmn` ni `forms/`, que sí están en `servicio-reservas/src/main/resources/bpmn/` (incumple P2). Además, `subproceso-reserva.bpmn` (id `subproceso-proceso-reserva`) se despliega pero ningún proceso lo invoca: `proceso-principal` lleva su propia copia como subproceso incrustado. Opciones: A) copiar los que faltan a `bpmn/`; B) enmendar P2; C) decidir antes si `subproceso-reserva.bpmn` sobra o si `proceso-principal` debe invocarlo. Recomendación: C y después A (riesgo 2 por tocar `.bpmn`; requiere autorización según P1).
- [revisar-después] D3 — ftd/2026-11-27-claude-md-por-servicio: la constitución (P3) habla de `ZeebeBpmnError`, pero el código usa `BpmnError.bpmnError(...)`. Opciones: A) enmendar P3 con el nombre real. Recomendación: A.
- [resuelta 2026-11-27] D4 — `servicio-reservas/README.md` describía el servicio de pagos. Vicente eligió A (reescribirlo): ver ftd/2026-11-27-readme-servicio-reservas.
