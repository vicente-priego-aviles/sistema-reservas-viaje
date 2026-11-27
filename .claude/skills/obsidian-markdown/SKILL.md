---
name: obsidian-markdown
description: Escribe notas válidas para Obsidian con frontmatter OKF, enlaces markdown relativos, callouts, embeds y Mermaid. Úsala al crear o editar cualquier .md de la bóveda o de la documentación del proyecto, o cuando se hable de callouts, propiedades, embeds o enlaces de Obsidian.
tags: [obsidian, markdown, okf, documentacion]
origen: "Adaptada de una skill con licencia MIT; ver AVISOS-DE-TERCEROS.md"
---

# Markdown para Obsidian (OKF)

Solo cubre lo específico de Obsidian y estas convenciones; el Markdown estándar se da por sabido. Lo que el `CLAUDE.md` del proyecto diga en contra (wikilinks, otros callouts) manda sobre esta skill.

## Crear una nota
1. **Frontmatter OKF** (obligatorio `type`; recomendados `title`, `description`, `tags`; trazabilidad con `sources`, `generated`, `verified`, `status`, `stale_after`). Plantilla en la skill `brain`.
2. **Contenido**: corto, destilado, en español.
3. **Enlaces** según la regla de abajo.
4. **Callouts** para avisos y estados; **Mermaid** para diagramas simples; skill `excalidraw` para los complejos.
5. **Comprobar**: enlaces que resuelven y frontmatter YAML válido.

## Enlaces
| Caso | Sintaxis |
|---|---|
| Nota o archivo de la bóveda | `[Texto](ruta/relativa/nota.md)` — **markdown relativo al archivo** (OKF) |
| Sección de una nota | `[Texto](nota.md#encabezado)` |
| URL externa | `[Texto](https://…)` |
| Dentro de dibujos Excalidraw | `[[nota]]` (excepción) |
| Embeber un `.base` o un `.excalidraw.md` | `![[archivo.base#Vista]]`, `![[diagrama.excalidraw]]` (excepción: Obsidian lo resuelve mejor) |

Obsidian está configurado con `useMarkdownLinks: true` y `newLinkFormat: relative`, así que al renombrar actualiza los enlaces. Si un proyecto decide usar wikilinks, lo dirá su `CLAUDE.md`; en ese caso `[[Nota]]`, `[[Nota#Encabezado]]`, `[[Nota|texto]]`.

## Embeds
```markdown
![Texto alternativo](../diagramas/imagen.png)     imagen
![](otra-nota.md)                                  nota entera
![](otra-nota.md#Encabezado)                       sección
![[tablero.base#Pendientes]]                       vista de una Base
```
Detalles (PDF, audio, búsquedas) en `references/sintaxis.md`.

## Callouts
```markdown
> [!note] Título opcional
> Texto.

> [!warning]- Plegado por defecto
> Texto.
```
Uso fijo (salvo que el proyecto diga otra cosa):
| Callout | Para |
|---|---|
| `[!abstract]` | resumen al inicio de una nota larga |
| `[!tip]` | recomendación |
| `[!warning] Desactualizado (fecha)` | afirmación superada (brain) |
| `[!question] En disputa` | fuentes que se contradicen (brain) |
| `[!danger]` | riesgo, acción irreversible |
| `[!success]` / `[!failure]` | evidencia de una verificación |
| `[!todo]` | pendiente |

## Otros
- **No partas párrafos con saltos de línea manuales**: Obsidian los muestra tal cual. Un párrafo, una línea.
- Etiquetas en el frontmatter (`tags: [a, b/c]`); evita `#etiqueta` en línea salvo en notas rápidas.
- Comentarios ocultos: `%%texto%%`. Resaltado: `==texto==`. Notas al pie: `[^1]`.
- Mermaid: bloque ```` ```mermaid ````; para enlazar un nodo a una nota: `class Nodo internal-link;`.
- Bloques citables: `^id-bloque` al final del párrafo.
