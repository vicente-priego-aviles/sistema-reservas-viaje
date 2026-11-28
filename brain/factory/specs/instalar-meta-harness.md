---
type: Spec
title: Instalar meta-harness-factory en sistema-reservas-viaje
description: Quien clone el repositorio tendrá el harness de Claude Code (brain, carriles FTD y ASDD, hooks, subagentes y skills de stack) funcionando y sin secretos, sin que cambie cómo se compila y arranca el sistema.
tags: [spec, factoria, harness]
estado: implementada
tarea: 67
rama: 67-chore-instalar-harness
riesgo: 2
sources:
  - { resource: ../../wiki/index.md }
  - { resource: ../constitucion.md }
  - { resource: ../../wiki/factoria/glosario.md }
  - { resource: ../../../.claude/rules/brain.md }
  - { resource: ../../../.mh.json }
  - { resource: ../../../.gitignore }
  - { resource: ../../../init.sh }
  - { resource: ../../../.mh/hooks/bash-previo.mjs }
  - { resource: ../../../.mh/hooks/editar-previo.mjs }
  - { resource: https://github.com/vicente-priego-aviles/sistema-reservas-viaje/issues/67 }
  - { resource: https://github.com/vicente-priego-aviles/sistema-reservas-viaje/issues/70 }
generated: { by: agent:claude-opus-5-5, at: 2026-11-27 }
---

# Instalar meta-harness-factory en sistema-reservas-viaje

> [!info] Origen
> Issue [#67](https://github.com/vicente-priego-aviles/sistema-reservas-viaje/issues/67). La instalación ya está hecha en la rama `67-chore-instalar-harness` (commit `fe15183` sobre `develop`: 151 archivos; 64 en `.claude/skills`, 43 en `.mh`, 23 en `brain`, 12 en `.claude/agents`, 5 en `.claude/rules`, más `.mh.json`, `init.sh`, `.gitignore` y `CLAUDE.md`). No toca código Java, `pom.xml` ni `.bpmn`. Esta spec fija qué falta para darla por terminada: los hallazgos de la revisión del commit.

> [!success] Implementada (2026-11-28)
> Fusionada en `develop` en tres PR: [#69](https://github.com/vicente-priego-aviles/sistema-reservas-viaje/pull/69) (instalación, `CLAUDE.md` por microservicio, constitución y esta spec), [#71](https://github.com/vicente-priego-aviles/sistema-reservas-viaje/pull/71) (hooks versionados, `.gitignore`, `.pyc`, `.bpmn` protegidos y G5) y [#72](https://github.com/vicente-priego-aviles/sistema-reservas-viaje/pull/72) (`init.sh`). Los pendientes de la revisión se siguieron en la issue [#70](https://github.com/vicente-priego-aviles/sistema-reservas-viaje/issues/70). Cumplidos sobre la copia de trabajo: CA1, CA2, CA3, CA4, CA5, CA7, CA8, CA9, CA10, CA11 y CA12. No ejecutado: CA6 (hooks activos en una sesión nueva sobre un clon limpio) y las pruebas de CA1, CA2, CA3 y CA5 sobre un clon limpio; la #70 sigue abierta por CA6. Las decisiones de cada punto abierto están en su tabla.

> [!warning] Riesgo 2
> `riesgo.mjs` sobre el commit da 2 por rutas sensibles (`.claude/agents/*`, `.claude/rules/*`). Según G1, PR con aprobación humana.

## Problema
El commit instala el harness, pero tal como está no es reproducible ni limpio:

1. **Los hooks no viajan con el repositorio.** SessionStart, PreToolUse (Bash, Edit, Write), PostToolUse, Stop, PreCompact y SessionEnd, y los permisos, se configuran en `.claude/settings.json`, que está en `.gitignore` (commit `6882f8c`). En un clon nuevo no hay hooks: ni puerta de riesgo, ni convención de commit, ni rama protegida.
2. **Hay un secreto en la configuración local.** El `.claude/settings.json` local contiene un token personal de GitHub en claro dentro de reglas `allow`. No está en el historial de git (verificado con `git log --all -S`), pero bloquea cualquier opción de versionar ese archivo tal cual y contradice el espíritu de S1. Además acumula 84 reglas `allow` de sesiones anteriores (por ejemplo `curl` a localhost con `demo:demo`, `./scripts/reset-cliente.sh`, `Read(//usr/local/bin/**)`) y ninguna `deny`.
3. **Se versionó un binario de Python.** `.claude/skills/brain/scripts/__pycache__/verificar_evidencias.cpython-314.pyc` está en git y figura en `gestionados` de `.mh.json`; `.gitignore` no cubre `__pycache__/` ni `*.pyc`.
4. **`.gitignore` contradice al Maven Wrapper.** Ignora `mvnw`, `mvnw.cmd` y `.mvn/`, pero `mvnw` y `.mvn/wrapper/maven-wrapper.properties` están versionados, y la constitución (§1) declara Maven Wrapper como build. `mvnw.cmd` existe en disco; su estado en git se comprueba en el plan.
5. **`CLAUDE.md` excede el límite y afirma algo falso.** Tiene 152 líneas (el harness pide 120 como máximo) y describe `ArchitectureTest` como existente; no existe (la constitución lo marca como pendiente y la issue #68 lo crea).
6. **Los `.bpmn` no tienen protección técnica.** P1 exige autorización explícita para tocar un `.bpmn`, pero `.mh.json` tiene `"protegidos": []` y no hay `deny` en la configuración. `editar-previo.mjs` ya admite globs en `protegidos` (responde «ask», pide confirmación).
7. **El hook de commits exige lo que la constitución deja opcional.** Con `git.convencion: "conventional-gitmoji"`, `bash-previo.mjs` valida con `RE_GITMOJI`, que **exige** el emoji al principio; con `"conventional"` valida `RE_CONVENTIONAL`, que **rechaza** el emoji. Ninguna opción admite «gitmoji opcional» como dice G5.
8. **`init.sh` es la plantilla genérica.** Solo compila; las líneas de Docker y el smoke test están comentados y el ejemplo apunta a `:8080` (Camunda), no a un microservicio.

## Coste de no hacerlo
Cada clon nuevo trabaja sin las barreras del harness (riesgo 2 sin spec, commits en `develop`, `.bpmn` sin confirmación). Un descuido al versionar `.claude/settings.json` publicaría el token. Las contradicciones (`.gitignore` frente a wrapper, hook frente a G5, `CLAUDE.md` frente a la realidad) hacen que el agente reciba instrucciones incompatibles.

## Alternativa más barata
Fusionar el commit tal cual y documentar en `CLAUDE.md` que cada uno configure sus hooks a mano. No basta: deja el harness dependiente de la máquina de Vicente, no resuelve el `.pyc` ni las contradicciones, y no demuestra el criterio de la issue en un clon limpio.

## Objetivos / No objetivos
**Objetivos**
- `mh comprobar` devuelve LISTO en la rama.
- Un clon limpio de la rama compila y arranca igual que `develop`, y `./init.sh` termina bien.
- Los hooks del harness quedan activos en un clon limpio, o queda explícito y documentado que no lo están (según P1).
- `git ls-files` sin `.pyc`, sin `__pycache__` y sin secretos; ningún archivo versionado está a la vez ignorado.
- Resolver las contradicciones 4, 6 y 7 según decida Vicente.
- Revocar o retirar de la configuración el token personal de GitHub.

**No objetivos**
- Crear los `ArchitectureTest` (issue #68).
- Repartir `CLAUDE.md` en `servicio-*/CLAUDE.md` para bajar de 120 líneas (siguiente tarea de #67), salvo lo que decida P5.
- Implementar la idempotencia de los workers (#65, spec [workers-clientes-idempotencia-errores](workers-clientes-idempotencia-errores.md)).
- Instalar el módulo vaadin (aplazado: no hay dependencia `com.vaadin`).
- Cualquier cambio en código Java, `pom.xml`, `.bpmn`, `application.yml` o `docker-compose*.yml`.

## Las 4 preguntas
### 1. ¿Qué podrá hacer el usuario?
Vicente (o cualquier persona que clone el repositorio y abra Claude Code en él) podrá trabajar con los carriles Responder, FTD y ASDD, consultar y escribir el brain (`brain/`, abierto como bóveda de Obsidian), lanzar los subagentes revisores y usar las skills de stack, con los hooks del harness aplicando sus barreras desde la primera sesión. Precondiciones: Node disponible (los hooks y `mvnq.mjs` son `.mjs`), JDK para el wrapper, y `mh` (`~/Proyectos/meta-harness`) para `mh comprobar`.

### 2. ¿Qué resultado observable obtiene?
- `mh comprobar` imprime LISTO.
- `./init.sh` termina con código 0 e imprime «Entorno listo».
- En una sesión nueva, SessionStart anuncia el harness; `git commit -m "x"` es bloqueado por `convencion-commit`; un commit en `develop` es bloqueado por `rama-protegida`; editar un `.bpmn` pide confirmación (si se acepta P6).
- `node .mh/scripts/mvnq.mjs verify` en verde, con el mismo resultado que en `develop`.

### 3. ¿Qué debe respetar?
- Constitución: S1 (ningún secreto en el repositorio), P1 (los `.bpmn` no se tocan; todo cambio en uno es riesgo 2), G1 (riesgo 2 → PR con aprobación humana), G2 (español), G3 (PR a `develop`), G4 (rama `67-chore-instalar-harness`), G5 (Conventional Commits; gitmoji opcional, salvo enmienda en P7), §1 (Maven Wrapper), §7 (definición de terminado; C1 y C2 se declaran «no ejecutado» mientras no exista ArchUnit).
- Comportamiento que no cambia: compilación, tests y arranque de los 6 microservicios y de Camunda (`scripts/*.sh`, `docker-compose*.yml`).
- Los archivos gestionados por el harness (`gestionados` de `.mh.json`) no se editan a mano: los cambios en ellos van por `mh actualizar` o por meta-harness-factory.
- Notas del brain en español, un párrafo por línea, frontmatter OKF (`.claude/rules/brain.md`).

### 4. ¿Cómo se demuestra?
Sobre un clon limpio de la rama (`git clone --branch 67-chore-instalar-harness … /tmp/<dir>`), con la salida de cada comando guardada como evidencia en el PR (y en `brain/factory/` si la skill `revision` lo pide). La comparación con `develop` se hace con el mismo comando sobre un clon de `develop`.

## Criterios de aceptación
- **CA1** Dado el clon limpio, cuando se ejecuta `mh comprobar`, entonces devuelve LISTO. *Prueba*: salida de `mh comprobar`.
- **CA2** Dado el clon limpio, cuando se ejecuta `./init.sh`, entonces termina con código 0. *Prueba*: `./init.sh; echo $?` → `0`.
- **CA3** Dado el clon limpio, cuando se ejecuta `node .mh/scripts/mvnq.mjs verify`, entonces termina en verde con el mismo número de tests que en `develop`. *Prueba*: resumen de `mvnq.mjs verify` en ambos clones.
- **CA4** Dada la rama, cuando se compara con `develop`, entonces no hay cambios en código ni en configuración de ejecución. *Prueba*: `git diff --stat develop...HEAD -- '*.java' '*pom.xml' '*.bpmn' '*.yml' '*.yaml' '*.properties' 'scripts/' 'docker-compose*'` vacío (excepto `.mvn/wrapper/maven-wrapper.properties` si P4 lo toca).
- **CA5** Dado el clon limpio, cuando se arranca el sistema, entonces los 6 microservicios responden igual que en `develop`. *Prueba*: según P8.
- **CA6** Dado el clon limpio, cuando se inicia una sesión de Claude Code, entonces los hooks están activos: SessionStart anuncia el harness, `git commit -m "x"` queda bloqueado y un commit en `develop` queda bloqueado. *Prueba*: transcripción de la sesión. Depende de P1: si se decide mantener `settings.json` local, este criterio se sustituye por «`CLAUDE.md` o `init.sh` explican cómo activarlos» y la activación se declara «no ejecutado».
- **CA7** Dada la rama, cuando se listan los archivos versionados, entonces no hay `.pyc` ni `__pycache__`. *Prueba*: `git ls-files | grep -E '__pycache__|\.pyc$'` vacío; `git check-ignore -q x/__pycache__/y.pyc` → código 0.
- **CA8** Dada la rama, cuando se busca un token de GitHub en lo versionado y en su historial, entonces no aparece. *Prueba*: `git grep -nE 'gh[pousr]_[A-Za-z0-9]{20,}|github_pat_' $(git rev-list develop..HEAD)` vacío; y, si P1 versiona `settings.json`, el mismo `git grep` sobre `HEAD:.claude/settings.json` vacío.
- **CA9** Dada la rama, cuando se buscan archivos versionados que `.gitignore` ignora, entonces no hay ninguno. *Prueba*: `git ls-files -ci --exclude-standard` vacío (cubre `mvnw`, `.mvn/` y `settings.json`).
- **CA10** Dada la decisión de P7, cuando se intenta `git commit -m "fix(x): y"` y `git commit -m "🐛 fix(x): y"`, entonces el hook acepta o rechaza cada uno según lo que diga G5 tras la decisión. *Prueba*: invocar `comprobarConvencion` de `bash-previo.mjs` con ambos mensajes (script de node de una línea) y guardar la salida.
- **CA11** Si se acepta P6, dado el clon limpio, cuando el agente intenta editar `bpmn/proceso-principal.bpmn`, entonces el hook pide confirmación. *Prueba*: transcripción de la sesión (se rechaza la edición; el archivo queda intacto: `git status` limpio).
- **CA12** Dada la rama, cuando se lee `CLAUDE.md`, entonces no afirma que exista `ArchitectureTest`. *Prueba*: `git grep -n ArchitectureTest CLAUDE.md` solo en frases que lo dan como pendiente (#68). Alcance según P5.

## Puntos abiertos
| # | Tipo | Punto | Decisión |
|---|---|---|---|
| P1 | pregunta abierta | ¿Se versiona un `.claude/settings.json` limpio con los hooks y los permisos del harness, y lo personal pasa a `.claude/settings.local.json` (que sigue ignorado)? Recomendada: **sí**; quitar `.claude/settings.json` de `.gitignore` y versionar solo hooks + permisos genéricos del harness. Sin esto CA6 no se puede cumplir en un clon limpio. | **Sí** (#71): `.claude/settings.json` versionado con los hooks de la plantilla; lo personal, en `settings.local.json`. |
| P2 | pregunta abierta | Token personal de GitHub en `settings.json` local. Recomendada: **revocarlo en GitHub ya**, borrar las reglas que lo contienen y usar `gh auth` (o variable de entorno fuera del repositorio). Hacerlo antes de P1. | **Hecho** (2026-11-28): token revocado por Vicente; ya no está en ningún `settings*.json`. |
| P3 | pregunta abierta | ¿Se podan las 84 reglas `allow` y se añaden `deny` básicas? Recomendada: **sí**; las de sesión (`curl` con `demo:demo`, `reset-cliente.sh`, lecturas fuera del proyecto) a `settings.local.json` o fuera; en el `settings.json` versionado, `deny` para `Read` de `.env`, `*.pem`, `*.key` y `connector-secrets.txt`. | **Sí** (#71): de unas 135 reglas `allow` entre los dos archivos quedan 16 en `settings.local.json`; `deny` para `*.pem`, `*.key` y `connector-secrets.txt`. No para `.env`: solo lleva los valores por defecto de Camunda y se versiona. |
| P4 | pregunta abierta | `.gitignore` frente al wrapper. Recomendada: **quitar** las líneas `.mvn/`, `mvnw` y `mvnw.cmd` (se mantiene `.mvn/wrapper/maven-wrapper.jar`) y versionar `mvnw.cmd` si no lo está. | **Sí** (#71): `.gitignore` deja de ignorar `mvnw`, `mvnw.cmd`, `.mvn/` y `.env`; `mvnw.cmd` ya estaba versionado. |
| P5 | pregunta abierta | ¿Qué se corrige ahora en `CLAUDE.md`? Recomendada: **solo** la frase de `ArchitectureTest` (darlo como pendiente, #68); la reducción a 120 líneas queda para la siguiente tarea de #67. ¿El bloque gestionado lo admite sin `mh actualizar`? Lo comprueba el plan. | **Resuelto en #69**: `CLAUDE.md` repartido por microservicio (116 líneas) y sin afirmar que exista `ArchitectureTest`. |
| P6 | pregunta abierta | ¿Proteger los `.bpmn`? Recomendada: **sí**, `"protegidos": ["**/*.bpmn"]` en `.mh.json` (pide confirmación, coherente con P1 de la constitución); no `deny`, que impediría los cambios autorizados. | **Sí** (#71): `"protegidos": ["**/*.bpmn"]`. |
| P7 | pregunta abierta | El hook exige gitmoji y G5 lo deja opcional. Opciones: (a) enmendar G5 a «gitmoji obligatorio» (como ya elegiste en el plan de `mh onboard`); (b) `"conventional"`, que rechazaría los commits con emoji; (c) pedir a meta-harness-factory un modo que acepte ambos. Recomendada: **(a)** ahora, con fila en el registro de enmiendas, y (c) como issue en meta-harness-factory. | **(a)** (#71): G5 enmendada a gitmoji obligatorio, con fila en el registro de enmiendas. (c) sin issue: meta-harness no tiene remoto. |
| P8 | pregunta abierta | ¿Cómo se demuestra «arranca igual que antes» (CA5)? Recomendada: CA4 (ningún cambio en código ni en configuración de ejecución) + CA3 bastan como prueba principal; además, `./scripts/start.sh` y `curl -fsS` a `/actuator/health` de los puertos 9080–9084 y 9090 (todos los `pom.xml` declaran actuator) como evidencia; si no hay Docker, se declara «no ejecutado». | **Sí** (#72): `./init.sh` levanta todo y comprueba `/actuator/health` de los 6 microservicios. |
| P9 | pregunta abierta | El `.pyc` está en `gestionados` de `.mh.json`: borrarlo puede hacer fallar `mh comprobar`. Recomendada: quitarlo de git, añadir `__pycache__/` y `*.pyc` a `.gitignore`, abrir issue en meta-harness-factory para que no lo copie, y verificar CA1 después. | **Sí** (#71): `.pyc` fuera de git y `__pycache__/`, `*.pyc` en `.gitignore`; `mh comprobar` sigue en LISTO. Sigue en `gestionados` de `.mh.json`; que meta-harness no lo copie queda para meta-harness. |
| P10 | decisión aplazada | `init.sh` es la plantilla genérica (solo compila). Recomendada: dejarlo así en esta tarea y completarlo (Docker + smoke test) en una tarea aparte. | **Completado en #72** en vez de aplazarse: Docker, espera a Camunda, smoke test y `--solo-compilar`. |
| P11 | supuesto tomado | `mh comprobar` se ejecuta desde la instalación local de meta-harness (`~/Proyectos/meta-harness`) y no requiere red. | **Confirmado**: `node ~/Proyectos/meta-harness/installer/mh.mjs comprobar` funciona sin red. |
