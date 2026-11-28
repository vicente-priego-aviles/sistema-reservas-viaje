---
name: panel
description: Panel del proyecto para retomarlo tras días o semanas (brain/Panel.md): dónde se quedó, siguiente paso, lo urgente, timeline y limpieza. «/panel» al volver; «/panel cerrar» al terminar una sesión para apuntar el siguiente paso; «/panel limpiar» para cerrar issues ya resueltas y borrar ramas fusionadas.
disable-model-invocation: true
---

# Panel del proyecto

`brain/Panel.md` lo genera `node scripts/panel.mjs` con datos de git, GitHub (`gh`), los worklogs de `brain/raw/sesiones/` y `brain/factory/` (specs, FTD y decisiones pendientes). No se edita a mano: lo único que se escribe a mano son sus propiedades `siguiente_paso`, `notas` y `prioridad`, y se cambian con las opciones del script, que las conserva al regenerar.

| Opción | Qué hace |
|---|---|
| `--siguiente "texto"` | apunta el siguiente paso (y la fecha en `apuntado`) |
| `--notas "texto"` | lo que quedó a medias o hay que recordar; `""` lo borra |
| `--prioridad 68,65` | issues que van primero en «Lo más urgente», en ese orden; `""` lo borra |
| `--sin-github` | sin llamar a `gh` (sin red) |
| `--limpieza` | imprime en JSON las issues abiertas cuya rama ya se fusionó y las ramas locales fusionadas; no escribe el Panel |
| `--inicio`, `--fin` | los usan los hooks de `.claude/settings.json`: al abrir la sesión pasan a Claude el siguiente paso apuntado; al cerrarla, regeneran el Panel |

Lo urgente sale así: primero la prioridad apuntada, después `security`, `bug` y las issues de una spec sin terminar. Una issue abierta cuya rama `N-…` ya se fusionó va a «Limpieza», no a urgentes.

## `/panel` (retomar)
1. Ejecuta `node scripts/panel.mjs` y lee `brain/Panel.md`.
2. Resume en pocas líneas: el siguiente paso apuntado y hace cuánto, la rama y si hay cambios sin commit, las 3 primeras urgentes, la salud (último `./init.sh`, tests) y lo de limpieza que estorbe (ramas sin fusionar, issues para cerrar). Si `./init.sh` falló o hace semanas que no se ejecuta, propón lanzarlo antes de tocar código.
3. Si el siguiente paso apuntado ya no vale (la issue está cerrada, la rama no existe), dilo.
4. Propón por dónde empezar y espera a que el usuario elija. No cierres issues ni borres ramas sin que lo pida.

## `/panel cerrar` (al terminar)
1. Mira qué ha pasado en la sesión: `git status`, los commits nuevos, las ramas, y lo hablado.
2. Propón **un** siguiente paso concreto, en una frase que se entienda dentro de semanas sin más contexto: qué issue, qué archivo o qué orden, y en qué punto está (p. ej. «#68: escribir el test de C2 en servicio-clientes; el de C1 ya pasa»). Propón también notas si algo quedó a medias, y si cambia la prioridad.
3. Pregunta al usuario si lo apuntas así o lo corrige.
4. Ejecuta `node scripts/panel.mjs --siguiente "…" [--notas "…"] [--prioridad …]` con lo aceptado.
5. Avisa si quedan cambios sin commit o una rama sin PR.

## `/panel limpiar`
1. Ejecuta `node scripts/panel.mjs --limpieza`. Si `github` es `false`, limpia solo las ramas y dilo.
2. **Issues** (`issues`): enséñalas en una lista (número, título, dónde se resolvió). Una issue cuya rama se fusionó puede haberse resuelto solo en parte: si el título del PR no cubre el de la issue, márcala para que el usuario la mire. Pregunta cuáles se cierran (todas, ninguna o una lista) y espera la respuesta.
3. Ciérralas con `gh issue close N --comment "Resuelta en #PR (fusionado el AAAA-MM-DD en develop)."`. Si se resolvió con una rama sin PR (`rama fusionada`), el comentario lo dice así.
4. **Ramas** (`ramas`): enséñalas y pregunta. Bórralas con `git branch -d`, nunca `-D`: si git se niega, la rama tiene commits que no están en `develop`, así que no se borra y se avisa. Solo las locales; las remotas, solo si el usuario lo pide.
5. Si `mainDetras` > 0, recuérdalo (falta una release) pero no la hagas: es otra tarea (skill `release`).
6. Termina con `node scripts/panel.mjs` y di cuánto queda por limpiar.
