---
type: Plan
title: Panel del proyecto
description: Qué falta del panel de brain/Panel.md antes de darlo por terminado y llevarlo al catálogo de meta-harness.
tags: [plan, factoria, panel]
estado: en-curso
tarea: 76
rama: 76-feat-panel-proyecto
riesgo: 2
---

# Panel del proyecto

El panel (`scripts/panel.mjs`, skill `panel`, fragmento CSS `panel-proyecto`) se hizo el 2026-11-28 en la issue #76 (PR #77) como prueba en este proyecto, con la idea de llevarlo después a meta-harness.

## Hecho
- Dónde lo dejé (siguiente paso, rama, última sesión, último cambio, PR abiertos, ramas sin fusionar), lo urgente, decisiones y specs, timeline vertical, limpieza y resto de issues.
- Hooks de inicio (pasa el siguiente paso a Claude) y de cierre (regenera el panel); `/panel`, `/panel cerrar` y `/panel limpiar`.
- Actividad (mapa de commits, milestones, issues olvidadas) y salud (último `./init.sh`, tests, CI, versiones).
- Cómo arrancar (comandos de `scripts/`, URLs de Camunda y puertos de los microservicios) y «Desde que lo dejaste» (foto al cerrar).

## Pendiente
- [ ] Probar los hooks en una sesión real: que al abrir llegue el siguiente paso y que al cerrar se regenere el panel con el worklog de la sesión.
- [ ] Probar `./init.sh` completo con Docker y ver la tarjeta 🩺 con los 7 servicios.
- [ ] Probar «Desde que lo dejaste» con un cierre real (hasta ahora, solo con una foto simulada).
- [ ] Usar `/panel limpiar` con las 7 issues ya resueltas y las 14 ramas fusionadas.
- [ ] Versiones: cubrir también las imágenes de `.env` (connectors, Elasticsearch), no solo el starter de Camunda.
- [ ] CI: el proyecto no tiene `.github/workflows/`; decidir si se crea uno (issue aparte).
- [ ] Tests de `scripts/panel.mjs` (funciones puras: tipos de PR, versiones, foto de cierre, decisiones).
- [ ] Llevarlo a meta-harness como módulo opcional, con ADR y tests: rama base y brain desde `.mh.json`, chuleta genérica (compose y scripts), dependencias a vigilar configurables, el CSS y los hooks en las plantillas del instalador.
