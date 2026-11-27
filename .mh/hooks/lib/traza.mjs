// Modo traza (ADR-0015): qué carril, skills, subagentes, reglas y hooks actúan, con iconos fijos.
// Lo escriben hooks y scripts, nunca el modelo: no cuesta tokens. Cada línea va a .mh/tmp/traza.log y, como
// aviso (systemMessage), a la conversación; lo que no se puede mostrar en el momento espera en una cola.
import { appendFileSync, existsSync, readFileSync, unlinkSync } from "node:fs";
import { join } from "node:path";
import { tmp } from "./comun.mjs";

export const ICONOS = {
  carril: "🛣️", skill: "🧩", subagente: "🤖", regla: "📏", riesgo: "⚖️",
  bloqueo: "🛡️", maven: "🔧", aviso: "⚠️", ok: "✅", fallo: "❌", permiso: "🔐",
};

/** Activo si MH_TRAZA=1, o si .mh.json dice traza: true (MH_TRAZA=0 lo apaga para una sesión). */
export function trazaActiva(config = {}) {
  if (process.env.MH_TRAZA === "1") return true;
  if (process.env.MH_TRAZA === "0") return false;
  return config.traza === true;
}

const corto = (t, n = 140) => (t.length > n ? `${t.slice(0, n - 1)}…` : t).replace(/\s+/g, " ");

/** Anota una línea en el log; si `cola`, también en la cola de pendientes. Devuelve la línea o null si está apagado. */
export function trazar(raiz, config, icono, texto, { cola = true } = {}) {
  if (!trazaActiva(config)) return null;
  const linea = `${icono} ${corto(texto)}`;
  const dir = tmp(raiz);
  const hora = new Date().toTimeString().slice(0, 8);
  try {
    appendFileSync(join(dir, "traza.log"), `${hora} ${linea}\n`);
    if (cola) appendFileSync(join(dir, "traza-pendiente"), `${linea}\n`);
  } catch {}
  return linea;
}

/** Saca (y borra) las líneas pendientes de la cola. */
export function sacarPendientes(raiz) {
  const f = join(tmp(raiz), "traza-pendiente");
  if (!existsSync(f)) return [];
  try {
    const lineas = readFileSync(f, "utf8").split("\n").filter(Boolean);
    unlinkSync(f);
    return lineas;
  } catch {
    return [];
  }
}

/** Texto del aviso: pendientes + líneas nuevas; null si está apagado o no hay nada. */
export function mensajeTraza(raiz, config, lineas = []) {
  if (!trazaActiva(config)) return null;
  const todas = [...sacarPendientes(raiz), ...lineas.filter(Boolean)];
  return todas.length ? todas.join("\n") : null;
}

/** Para un hook que decide: anota la línea y deja el aviso listo para que salga con su respuesta JSON. */
export function anunciar(raiz, config, icono, texto) {
  const linea = trazar(raiz, config, icono, texto, { cola: false });
  return mensajeTraza(raiz, config, [linea]);
}
