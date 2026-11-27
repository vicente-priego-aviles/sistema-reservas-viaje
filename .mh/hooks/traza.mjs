#!/usr/bin/env node
// Modo traza (ADR-0015): anuncia skills, subagentes, reglas cargadas y documentos de carril, y saca la cola de
// pendientes (líneas de otros hooks y scripts). Solo avisos para el usuario: el modelo no los ve. Apagado sale al instante.
//   PreToolUse  Skill|Agent             → 🧩 skill / 🤖 subagente lanzado
//   PostToolUse Agent|Write|Edit|…      → 🤖 subagente terminó / 🛣️ documento de carril (una vez por archivo y sesión)
//   InstructionsLoaded path_glob_match  → 📏 regla con paths (a la cola: este evento descarta los avisos)
//   PermissionRequest / PermissionDenied → 🔐 permiso pedido (en -p, sin respuesta, se deniega) / denegado en modo auto
//   Stop                                → saca la cola
import { basename, relative } from "node:path";
import { conAviso, enAbierto, leerConfig, leerEntrada, primeraVez, raizProyecto, salirJson } from "./lib/comun.mjs";
import { ICONOS, mensajeTraza, trazaActiva, trazar } from "./lib/traza.mjs";

const CARRILES = [
  [/\/factory\/ftd\//, "FTD"], [/\/factory\/specs\//, "ASDD · spec"], [/\/factory\/planes\//, "ASDD · plan"],
  [/\/factory\/progreso\//, "ASDD · progreso"], [/\/factory\/verificaciones\//, "ASDD · cierre"], [/\/factory\/revisiones\//, "revisión"],
];

/** Líneas de traza para una entrada de hook (exportado para los tests). */
export function lineas(entrada, raiz, config) {
  const ev = entrada.hook_event_name;
  const ti = entrada.tool_input || {};
  const t = (icono, texto, cola = false) => trazar(raiz, config, icono, texto, { cola });
  if (ev === "PreToolUse" && entrada.tool_name === "Skill") return [t(ICONOS.skill, `skill ${ti.skill || ti.command || "?"}`)];
  if (ev === "PreToolUse" && /^(Agent|Task)$/.test(entrada.tool_name || "")) {
    return [t(ICONOS.subagente, `subagente ${ti.subagent_type || "general"}${ti.description ? ` — ${ti.description}` : ""}`)];
  }
  if (ev === "PostToolUse" && /^(Agent|Task)$/.test(entrada.tool_name || "")) return [t(ICONOS.subagente, `${ti.subagent_type || "subagente"} terminó`)];
  if (ev === "PostToolUse" && ti.file_path) {
    const rel = relative(raiz, ti.file_path).split("\\").join("/");
    const carril = CARRILES.find(([re]) => re.test(`/${rel}`));
    if (carril && primeraVez(raiz, entrada.session_id, `traza-${rel.replace(/[^\w.-]/g, "_")}`)) return [t(ICONOS.carril, `${carril[1]}: ${rel}`)];
    return [];
  }
  if (ev === "PermissionRequest" || ev === "PermissionDenied") {
    const que = `${entrada.tool_name || "?"}${ti.command ? ` ${ti.command}` : ti.file_path ? ` ${relative(raiz, ti.file_path)}` : ""}`;
    return [t(ICONOS.permiso, ev === "PermissionRequest" ? `permiso pedido: ${que}` : `permiso denegado: ${que}${entrada.reason ? ` — ${entrada.reason}` : ""}`)];
  }
  if (ev === "InstructionsLoaded" && entrada.load_reason === "path_glob_match") {
    const por = entrada.trigger_file_path ? ` (al tocar ${basename(entrada.trigger_file_path)})` : "";
    t(ICONOS.regla, `regla ${basename(entrada.file_path || "?")}${por}`, true);
    return [];
  }
  return [];
}

if (process.argv[1]?.endsWith("traza.mjs")) {
  enAbierto(() => {
    const entrada = leerEntrada();
    const raiz = raizProyecto(entrada);
    const config = leerConfig(raiz);
    if (!trazaActiva(config)) return;
    const nuevas = lineas(entrada, raiz, config);
    if (entrada.hook_event_name === "InstructionsLoaded") return; // descarta los avisos: queda en la cola
    const texto = mensajeTraza(raiz, config, nuevas);
    if (!texto) return;
    conAviso(texto);
    salirJson({});
  });
}
