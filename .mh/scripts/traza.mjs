#!/usr/bin/env node
// Interruptor del modo traza (ADR-0015).
// Uso: node .mh/scripts/traza.mjs on | off | ver [n]   (ver: últimas n líneas de .mh/tmp/traza.log, 40 por defecto)
// Para una sola sesión, sin tocar .mh.json: MH_TRAZA=1 claude   (o MH_TRAZA=0 para apagarlo)
import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { ICONOS } from "../hooks/lib/traza.mjs";

const raiz = process.env.CLAUDE_PROJECT_DIR || process.cwd();
const [orden = "ver", n = "40"] = process.argv.slice(2);
const cfgRuta = join(raiz, ".mh.json");

if (orden === "on" || orden === "off") {
  if (!existsSync(cfgRuta)) { console.error("No hay .mh.json: meta-harness-factory no está instalado aquí."); process.exit(1); }
  const cfg = JSON.parse(readFileSync(cfgRuta, "utf8"));
  cfg.traza = orden === "on";
  writeFileSync(cfgRuta, `${JSON.stringify(cfg, null, 2)}\n`);
  console.log(`Modo traza ${cfg.traza ? "activado" : "desactivado"} (.mh.json → traza: ${cfg.traza}).`);
  if (cfg.traza) console.log(`Iconos: ${Object.entries(ICONOS).map(([k, v]) => `${v} ${k}`).join(" · ")}`);
} else if (orden === "ver") {
  const log = join(raiz, ".mh", "tmp", "traza.log");
  if (!existsSync(log)) { console.log("Sin traza todavía (.mh/tmp/traza.log no existe)."); process.exit(0); }
  console.log(readFileSync(log, "utf8").trimEnd().split("\n").slice(-Number(n)).join("\n"));
} else {
  console.error("Uso: node .mh/scripts/traza.mjs on | off | ver [n]");
  process.exit(1);
}
