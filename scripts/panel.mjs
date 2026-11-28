#!/usr/bin/env node
// Genera brain/Panel.md: dónde se quedó el proyecto, lo urgente, un timeline y lo que hay que limpiar.
// Uso: node scripts/panel.mjs [--siguiente "texto"] [--notas "texto"] [--prioridad 68,65] [--sin-github]
// Hooks: --inicio (SessionStart: pasa a Claude el siguiente paso apuntado, sin regenerar) · --fin (SessionEnd: regenera en silencio)
// Lo apuntado a mano (siguiente paso, notas, prioridad) vive en las propiedades del Panel y se conserva al regenerarlo.
import { execFileSync } from "node:child_process";
import { existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const RAIZ = join(dirname(fileURLToPath(import.meta.url)), "..");
const BRAIN = join(RAIZ, "brain");
const PANEL = join(BRAIN, "Panel.md");
const BASE = "develop";
const DIAS_PAUSA = 21;
const MAX_TIMELINE = 14;
const C = "\x60"; // comilla invertida, para el Markdown

const ejecutar = (cmd, args) => { try { return execFileSync(cmd, args, { cwd: RAIZ, encoding: "utf8", stdio: ["ignore", "pipe", "ignore"] }).trim(); } catch { return null; } };
const git = (...a) => ejecutar("git", a) ?? "";
const gh = (...a) => { const s = ejecutar("gh", a); try { return s ? JSON.parse(s) : null; } catch { return null; } };
const leer = (r) => { try { return readFileSync(r, "utf8"); } catch { return ""; } };
const corto = (s, n = 60) => (s.length > n ? s.slice(0, n - 1).trimEnd() + "…" : s);
const sinPrefijo = (t) => t.replace(/^\S*\p{Extended_Pictographic}\S*\s+/u, "").replace(/^[a-z]+(\([^)]*\))?!?:\s*/i, "").replace(/^\d+ (?=[a-z])/, "");
const nDias = (n) => n + (n === 1 ? " día" : " días");
// recorta sin dejar una comilla invertida abierta
const cortoMd = (s, n) => { let r = corto(s, n); if ((r.split(C).length - 1) % 2) r = r.replace(/…$/, "") + C + "…"; return r; };
const codigo = (s) => C + s + C;
const MESES = ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"];
const MESES_LARGOS = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];
const hoy = new Date().toISOString().slice(0, 10);
const diasEntre = (a, b) => Math.round((new Date(b) - new Date(a)) / 86400000);

// ── Argumentos y propiedades guardadas ────────────────────────────────────
function argumentos(argv) {
  const o = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === "--sin-github") o.sinGithub = true;
    else if (a === "--inicio") o.inicio = true;
    else if (a === "--fin") o.fin = true;
    else if (a === "--limpieza") o.limpieza = true;
    else if (["--siguiente", "--notas", "--prioridad"].includes(a)) o[a.slice(2)] = argv[++i] ?? "";
    else { console.error("Opción desconocida: " + a); process.exit(1); }
  }
  return o;
}
function propiedades(texto) {
  const fm = texto.match(/^---\n([\s\S]*?)\n---/)?.[1] ?? "";
  const valor = (k) => { const m = fm.match(new RegExp("^" + k + ": (.*)$", "m")); if (!m) return undefined; try { return JSON.parse(m[1]); } catch { return m[1]; } };
  return { siguiente: valor("siguiente_paso") ?? "", notas: valor("notas") ?? "", apuntado: valor("apuntado") ?? "", prioridad: valor("prioridad") ?? [] };
}
const args = argumentos(process.argv.slice(2));
const guardado = propiedades(leer(PANEL));
if (args.inicio) {
  // SessionStart: solo lee lo apuntado; nada de git ni de red, para no retrasar el arranque.
  const partes = [];
  if (guardado.siguiente) {
    const n = guardado.apuntado ? diasEntre(guardado.apuntado, hoy) : null;
    partes.push("Siguiente paso apuntado" + (guardado.apuntado ? " el " + guardado.apuntado + (n ? " (hace " + n + (n === 1 ? " día" : " días") + ")" : " (hoy)") : "") + ": " + guardado.siguiente);
  }
  if (guardado.notas) partes.push("Notas: " + guardado.notas);
  if (guardado.prioridad.length) partes.push("Prioridad: " + guardado.prioridad.map((x) => "#" + x).join(", "));
  if (partes.length) {
    const texto = "Panel del proyecto (brain/Panel.md). " + partes.join(" · ") + ". Si el usuario retoma el trabajo, propón empezar por ahí y comprueba antes que sigue vigente.";
    process.stdout.write(JSON.stringify({ hookSpecificOutput: { hookEventName: "SessionStart", additionalContext: texto.slice(0, 1200) } }));
  }
  process.exit(0);
}
if (args.fin) {
  // SessionEnd: deja que el hook del harness escriba antes el worklog de esta sesión, y no hagas ruido.
  await new Promise((r) => setTimeout(r, 2000));
  console.log = () => {};
}
const apuntes = {
  siguiente: args.siguiente ?? guardado.siguiente,
  notas: args.notas ?? guardado.notas,
  prioridad: args.prioridad !== undefined ? args.prioridad.split(/[,\s]+/).filter(Boolean).map(Number) : guardado.prioridad,
  apuntado: args.siguiente !== undefined || args.notas !== undefined ? ((args.siguiente ?? guardado.siguiente) ? hoy : "") : guardado.apuntado,
};

// ── Datos ────────────────────────────────────────────────────────────────
const remoto = git("remote", "get-url", "origin").match(/github\.com[:/](.+?)(\.git)?$/)?.[1];
const urlIssue = (n) => (remoto ? "[#" + n + "](https://github.com/" + remoto + "/issues/" + n + ")" : "#" + n);
const urlPr = (n) => (remoto ? "[#" + n + "](https://github.com/" + remoto + "/pull/" + n + ")" : "#" + n);

const conGithub = !args.sinGithub && remoto;
const issues = conGithub ? gh("issue", "list", "--state", "open", "--limit", "200", "--json", "number,title,labels,updatedAt") : null;
const fusionados = conGithub ? gh("pr", "list", "--state", "merged", "--limit", "200", "--json", "number,title,headRefName,mergedAt") : null;
const abiertos = conGithub ? gh("pr", "list", "--state", "open", "--json", "number,title,headRefName,isDraft") : null;
const hayGithub = issues && fusionados && abiertos;

const rama = git("rev-parse", "--abbrev-ref", "HEAD");
const sinCommit = git("status", "--porcelain").split("\n").filter(Boolean);
const ramasLocales = git("for-each-ref", "--format=%(refname:short)|%(committerdate:short)", "refs/heads").split("\n").filter(Boolean).map((l) => { const [nombre, fecha] = l.split("|"); return { nombre, fecha }; });
const fusionadasEnBase = new Set(git("branch", "--merged", BASE, "--format=%(refname:short)").split("\n").filter(Boolean));
const mainDetras = Number(git("rev-list", "--count", "main.." + BASE)) || 0;

function ultimaSesion() {
  const dir = join(BRAIN, "raw", "sesiones");
  if (!existsSync(dir)) return null;
  const f = readdirSync(dir).filter((x) => x.endsWith(".md")).sort((a, b) => leer(join(dir, a)).match(/^fin: (.*)$/m)?.[1]?.localeCompare(leer(join(dir, b)).match(/^fin: (.*)$/m)?.[1] ?? "") ?? 0).pop();
  if (!f) return null;
  const t = leer(join(dir, f));
  const prop = (k) => t.match(new RegExp("^" + k + ": (.*)$", "m"))?.[1] ?? "";
  const seccion = (titulo) => (t.split("## " + titulo)[1] ?? "").split("\n## ")[0].split("\n").filter((l) => l.startsWith("- ") && l !== "- ninguno");
  return { fin: prop("fin").slice(0, 10), rama: prop("rama"), commits: seccion("Commits de la sesión"), sinCommit: seccion("Cambios sin commit") };
}

function documentosFactoria() {
  const fuera = /^(implementad|cerrad|terminad|hech|descartad|resuelt)/i;
  const lista = [];
  for (const carpeta of ["specs", "ftd", "planes", "progreso"]) {
    const dir = join(BRAIN, "factory", carpeta);
    if (!existsSync(dir)) continue;
    for (const f of readdirSync(dir).filter((x) => x.endsWith(".md"))) {
      const fm = leer(join(dir, f)).match(/^---\n([\s\S]*?)\n---/)?.[1] ?? "";
      const p = (k) => fm.match(new RegExp("^" + k + ":[ \\t]*(.*)$", "m"))?.[1]?.trim() ?? "";
      if (fuera.test(p("estado"))) continue;
      lista.push({ ruta: "factory/" + carpeta + "/" + f, titulo: p("title") || f.replace(/\.md$/, ""), tipo: p("type") || carpeta, estado: p("estado") || "?", riesgo: p("riesgo"), tarea: p("tarea") });
    }
  }
  return lista;
}

function decisiones() {
  return leer(join(BRAIN, "factory", "decisiones-pendientes.md")).split("\n")
    .map((l) => l.match(/^- \[(bloqueante|revisar-después)\] (D\d+) — ([^:]+): (.*)$/))
    .filter(Boolean)
    .map(([, tipo, id, origen, texto]) => ({ tipo, id, origen, texto: cortoMd(texto.split(/\. Opciones:/)[0], 160) }));
}

// ── Cálculos ─────────────────────────────────────────────────────────────
const sesion = ultimaSesion();
const docs = documentosFactoria();
const decs = decisiones();
const etiquetas = (i) => i.labels.map((l) => l.name);
const ramaDeIssue = (n) => {
  const pr = (fusionados ?? []).find((p) => p.headRefName.startsWith(n + "-"));
  if (pr) return "PR " + pr.number;
  return [...fusionadasEnBase].some((r) => r.startsWith(n + "-")) ? "rama fusionada" : null;
};
const yaResueltas = (issues ?? []).filter((i) => ramaDeIssue(i.number)).map((i) => ({ ...i, pr: ramaDeIssue(i.number) }));
const resueltasNum = new Set(yaResueltas.map((i) => i.number));
const vivas = (issues ?? []).filter((i) => !resueltasNum.has(i.number));
const tareasDeDocs = new Set(docs.map((d) => Number(d.tarea)).filter(Boolean));
const motivo = (i) => {
  const pos = apuntes.prioridad.indexOf(i.number);
  if (pos >= 0) return { orden: pos, por: "prioridad apuntada" };
  if (etiquetas(i).includes("security")) return { orden: 100, por: "seguridad" };
  if (etiquetas(i).includes("bug")) return { orden: 200, por: "bug" };
  if (tareasDeDocs.has(i.number)) return { orden: 300, por: "spec en marcha" };
  return null;
};
const urgentes = vivas.map((i) => ({ ...i, m: motivo(i) })).filter((i) => i.m).sort((a, b) => a.m.orden - b.m.orden || b.updatedAt.localeCompare(a.updatedAt));
const urgentesNum = new Set(urgentes.map((i) => i.number));
const resto = vivas.filter((i) => !urgentesNum.has(i.number));
const sinFusionar = ramasLocales.filter((r) => r.nombre !== BASE && r.nombre !== "main" && !fusionadasEnBase.has(r.nombre))
  .map((r) => ({ ...r, delante: Number(git("rev-list", "--count", BASE + ".." + r.nombre)) || 0 })).filter((r) => r.delante > 0);
const ramasFusionadas = ramasLocales.filter((r) => fusionadasEnBase.has(r.nombre) && r.nombre !== BASE && r.nombre !== "main" && r.nombre !== rama);

// --limpieza: lo que hay que limpiar, en JSON, para «/panel limpiar» (no escribe el Panel)
if (args.limpieza) {
  const fecha = (n) => (fusionados ?? []).find((p) => "PR " + p.number === n)?.mergedAt?.slice(0, 10) ?? null;
  process.stdout.write(JSON.stringify({
    github: Boolean(hayGithub), base: BASE, ramaActual: rama,
    issues: yaResueltas.map((i) => ({ numero: i.number, titulo: i.title, resueltaEn: i.pr, fusionado: fecha(i.pr) })),
    ramas: ramasFusionadas.map((r) => ({ nombre: r.nombre, ultimoCommit: r.fecha })),
    mainDetras,
  }, null, 2) + "\n");
  process.exit(0);
}

// ── Timeline (HTML con el fragmento CSS panel-proyecto) ──────────────────
const esc = (s) => String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
const TIPOS = {
  feat: ["✨", "verde"], fix: ["🐛", "rojo"], chore: ["🔧", "naranja"], docs: ["📝", "azul"], test: ["✅", "cian"],
  refactor: ["♻️", "morado"], perf: ["⚡", "amarillo"], ci: ["👷", "gris"], build: ["📦", "gris"],
};
const chip = (texto, color = "gris", estilo = "") => '<span class="pp-chip pp-c-' + color + '"' + estilo + ">" + esc(texto) + "</span>";
const chipEtiqueta = (l) => {
  const hex = /^[0-9a-f]{6}$/i.test(l.color ?? "") ? l.color : null;
  const rgb = hex ? [0, 2, 4].map((k) => parseInt(hex.slice(k, k + 2), 16)).join(",") : null;
  return chip(l.name, "etiqueta", rgb ? ' style="--c:' + rgb + '"' : "");
};
const enlace = (url, texto, cls = "pp-num") => (url ? '<a class="external-link ' + cls + '" href="' + url + '">' + esc(texto) + "</a>" : '<span class="' + cls + '">' + esc(texto) + "</span>");
const urlPrRaw = (n) => (remoto ? "https://github.com/" + remoto + "/pull/" + n : null);
const urlIssueRaw = (n) => (remoto ? "https://github.com/" + remoto + "/issues/" + n : null);
function tipoDe(p) {
  const t = p.title.replace(/^\S*\p{Extended_Pictographic}\S*\s+/u, "");
  const m = t.match(/^([a-z]+)(?:\(([^)]*)\))?!?:/i);
  if (m) return { tipo: m[1].toLowerCase(), ambito: m[2] ?? "" };
  const r = p.headRefName.match(/^\d+-([a-z]+)-/) ?? p.headRefName.match(/^([a-z]+)\//);
  return { tipo: r ? r[1].replace("feature", "feat") : "", ambito: "" };
}
function timeline() {
  const porRama = new Map();
  for (const p of (fusionados ?? []).filter((p) => p.headRefName !== BASE).sort((a, b) => a.mergedAt.localeCompare(b.mergedAt))) porRama.set(p.headRefName, p);
  const prs = [...porRama.values()].sort((a, b) => a.mergedAt.localeCompare(b.mergedAt)).slice(-MAX_TIMELINE);
  if (!prs.length) return "";
  const porDia = new Map();
  for (const p of prs) { const d = p.mergedAt.slice(0, 10); porDia.set(d, [...(porDia.get(d) ?? []), p]); }
  const h = ['<div class="pp-tl">'];
  const pausa = (n, hasta) => h.push('<div class="pp-pausa">⏸️ ' + nDias(n) + " sin cambios fusionados" + hasta + "</div>");
  let anterior = null, mes = "";
  for (const [d, lista] of porDia) {
    if (anterior && diasEntre(anterior, d) >= DIAS_PAUSA) pausa(diasEntre(anterior, d), "");
    if (d.slice(0, 7) !== mes) { mes = d.slice(0, 7); h.push('<div class="pp-mes">📅 ' + MESES_LARGOS[Number(d.slice(5, 7)) - 1] + " " + d.slice(0, 4) + "</div>"); }
    h.push('<div class="pp-dia"><div class="pp-fecha">' + Number(d.slice(8)) + " " + MESES[Number(d.slice(5, 7)) - 1] + " " + chip(lista.length + (lista.length === 1 ? " PR" : " PR"), "gris") + "</div>");
    for (const p of lista) {
      const { tipo, ambito } = tipoDe(p);
      const [icono, color] = TIPOS[tipo] ?? ["🔀", "gris"];
      const titulo = sinPrefijo(p.title).replace(new RegExp("^" + tipo + " ", "i"), "");
      h.push('<div class="pp-item"><span class="pp-icono">' + icono + "</span>" + enlace(urlPrRaw(p.number), "#" + p.number) + (tipo ? chip(tipo, color) : "") + (ambito ? chip(ambito, "gris") : "") + nuevo("pr", p.number) + '<span class="pp-texto">' + esc(corto(titulo, 80)) + "</span></div>");
    }
    h.push("</div>");
    anterior = d;
  }
  if (anterior && diasEntre(anterior, hoy) >= DIAS_PAUSA) pausa(diasEntre(anterior, hoy), ", hasta hoy");
  const siguientes = urgentes.slice(0, 3);
  if (apuntes.siguiente || siguientes.length) {
    h.push('<div class="pp-dia pp-siguiente"><div class="pp-fecha">▶️ Siguiente</div>');
    if (apuntes.siguiente) h.push('<div class="pp-item"><span class="pp-icono">🎯</span><span class="pp-texto"><b>' + esc(apuntes.siguiente) + "</b></span></div>");
    for (const i of siguientes) h.push('<div class="pp-item"><span class="pp-icono">' + iconoMotivo(i.m.por) + "</span>" + enlace(urlIssueRaw(i.number), "#" + i.number) + i.labels.map(chipEtiqueta).join("") + '<span class="pp-texto">' + esc(corto(sinPrefijo(i.title), 80)) + "</span></div>");
    h.push("</div>");
  }
  h.push("</div>");
  return h.join("\n");
}
function iconoMotivo(por) { return por === "prioridad apuntada" ? "‼️" : por === "bug" ? "🐞" : por === "seguridad" ? "🔒" : "📐"; }
const DIAS_OLVIDADA = 90;
const olvidada = (i) => { const n = diasEntre(i.updatedAt.slice(0, 10), hoy); return n >= DIAS_OLVIDADA ? chip("💤 olvidada · " + nDias(n), "amarillo") : ""; };
const colorMotivo = (por) => (por === "prioridad apuntada" ? "morado" : por === "bug" || por === "seguridad" ? "rojo" : "azul");

// ── Desde que lo dejaste: foto al cerrar (/panel cerrar) en logs/panel-cierre.json ──
const FOTO = join(RAIZ, "logs", "panel-cierre.json");
const headBase = git("rev-parse", BASE);
let foto = null; try { foto = JSON.parse(leer(FOTO)); } catch {}
const desde = (() => {
  if (!foto) return null;
  const abiertasAntes = new Set(foto.issues ?? []), prsAntes = new Set(foto.prs ?? []);
  const nuevas = hayGithub && foto.issues ? (issues ?? []).filter((i) => !abiertasAntes.has(i.number)) : [];
  const abiertasAhora = new Set((issues ?? []).map((i) => i.number));
  const cerradas = hayGithub && foto.issues ? [...abiertasAntes].filter((n) => !abiertasAhora.has(n)) : [];
  const prs = hayGithub && foto.prs ? (fusionados ?? []).filter((p) => !prsAntes.has(p.number)) : [];
  const commits = foto.head && headBase ? Number(git("rev-list", "--count", foto.head + ".." + BASE)) || 0 : 0;
  return { fecha: foto.fecha, nuevas, cerradas, prs, commits, nuevasNum: new Set(nuevas.map((i) => i.number)), prsNum: new Set(prs.map((p) => p.number)) };
})();
const nuevo = (tipo, n) => (desde && (tipo === "pr" ? desde.prsNum : desde.nuevasNum).has(n) ? chip("🆕 nuevo", "verde") : "");
function guardarFoto() {
  mkdirSync(join(RAIZ, "logs"), { recursive: true });
  writeFileSync(FOTO, JSON.stringify({ fecha: new Date().toISOString().slice(0, 16).replace("T", " "), head: headBase,
    issues: hayGithub ? (issues ?? []).map((i) => i.number) : null, prs: hayGithub ? (fusionados ?? []).map((p) => p.number) : null }) + "\n");
}
function seccionDesde() {
  if (!desde) return '<div class="pp-nota">🆕 Lo nuevo se cuenta desde el próximo <code>/panel cerrar</code>.</div>';
  const t = [];
  const caja = (color, icono, titulo, n, lista) => '<div class="pp-tarjeta pp-c-' + (n ? color : "gris") + '"><div class="pp-tarjeta-titulo">' + icono + " " + titulo + '</div><div class="pp-tarjeta-valor">' + n + "</div>" + (lista ? '<div class="pp-tarjeta-pie">' + lista + "</div>" : "") + "</div>";
  t.push(caja("verde", "📥", "Issues nuevas", desde.nuevas.length, desde.nuevas.slice(0, 5).map((i) => enlace(urlIssueRaw(i.number), "#" + i.number) + " " + esc(corto(sinPrefijo(i.title), 40))).join("<br>")));
  t.push(caja("morado", "✅", "Issues cerradas", desde.cerradas.length, desde.cerradas.slice(0, 8).map((n) => enlace(urlIssueRaw(n), "#" + n)).join(" ")));
  t.push(caja("azul", "🔀", "PR fusionados", desde.prs.length, desde.prs.slice(0, 5).map((p) => enlace(urlPrRaw(p.number), "#" + p.number) + " " + esc(corto(sinPrefijo(p.title), 40))).join("<br>")));
  t.push(caja("cian", "📌", "Commits en " + esc(BASE), desde.commits, ""));
  return '<div class="pp-nota">Desde el último <code>/panel cerrar</code>, el ' + esc(desde.fecha) + ".</div>" + '<div class="pp-tarjetas">' + t.join("") + "</div>";
}

// ── Versiones (Maven Central y Adoptium), como mucho una consulta al día: logs/panel-versiones.json ──
const pomRaiz = leer(join(RAIZ, "pom.xml"));
const propPom = (k) => pomRaiz.match(new RegExp("<" + k.replace(/\./g, "\\.") + ">([^<]+)</"))?.[1] ?? null;
const DEPENDENCIAS = [
  { nombre: "Spring Boot", icono: "🍃", actual: propPom("spring-boot.version"), maven: "org/springframework/boot/spring-boot" },
  { nombre: "Camunda", icono: "⚙️", actual: propPom("camunda-spring-boot.version"), maven: "io/camunda/camunda-spring-boot-starter" },
  { nombre: "Java", icono: "☕", actual: propPom("java.version"), java: true },
];
const conRed = !args.sinGithub;
async function traer(url, json) {
  try { const r = await fetch(url, { signal: AbortSignal.timeout(6000) }); return r.ok ? (json ? r.json() : r.text()) : null; } catch { return null; }
}
const num = (v) => v.split(".").map(Number);
const mayorQue = (a, b) => { const x = num(a), y = num(b); for (let k = 0; k < 3; k++) if ((x[k] ?? 0) !== (y[k] ?? 0)) return (x[k] ?? 0) > (y[k] ?? 0); return false; };
async function versiones() {
  const ruta = join(RAIZ, "logs", "panel-versiones.json");
  let cache = null; try { cache = JSON.parse(leer(ruta)); } catch {}
  if (cache && (cache.fecha === hoy || !conRed)) return cache;
  if (!conRed) return null;
  const datos = { fecha: hoy, dependencias: {} };
  for (const d of DEPENDENCIAS) {
    if (d.java) {
      const j = await traer("https://api.adoptium.net/v3/info/available_releases", true);
      if (j) datos.dependencias[d.nombre] = { lts: String(j.most_recent_lts), ultima: String(j.most_recent_feature_release) };
    } else {
      const xml = await traer("https://repo1.maven.org/maven2/" + d.maven + "/maven-metadata.xml");
      // la versión estable más alta (no la etiqueta <latest>, que puede apuntar a una publicación antigua)
      const estables = xml ? [...xml.matchAll(/<version>(\d+\.\d+\.\d+)<\/version>/g)].map((m) => m[1]) : [];
      if (estables.length) datos.dependencias[d.nombre] = { estables };
    }
  }
  if (!Object.keys(datos.dependencias).length) return cache;
  mkdirSync(join(RAIZ, "logs"), { recursive: true });
  writeFileSync(ruta, JSON.stringify(datos) + "\n");
  return datos;
}
const datosVersiones = await versiones();
function tarjetaVersiones() {
  if (!datosVersiones) return "";
  const filas = DEPENDENCIAS.filter((d) => d.actual && datosVersiones.dependencias[d.nombre]).map((d) => {
    const info = datosVersiones.dependencias[d.nombre];
    if (d.java) {
      const estado = d.actual === info.lts ? chip("última LTS", "verde") : Number(d.actual) > Number(info.lts) ? chip("posterior a la LTS", "azul") : chip("LTS " + info.lts + " disponible", "amarillo");
      return '<div class="pp-item"><span class="pp-icono">' + d.icono + "</span><b>" + d.nombre + "</b> <code>" + esc(d.actual) + "</code>" + estado + (info.ultima !== d.actual ? chip("última " + info.ultima, "gris") : "") + "</div>";
    }
    const [ma, mi] = num(d.actual);
    const max = info.estables.reduce((a, b) => (mayorQue(b, a) ? b : a));
    const parche = info.estables.filter((v) => num(v)[0] === ma && num(v)[1] === mi).reduce((a, b) => (mayorQue(b, a) ? b : a), d.actual);
    const estado = !mayorQue(max, d.actual) ? chip("al día", "verde")
      : (mayorQue(parche, d.actual) ? chip("parche " + parche, "amarillo") : "") + (mayorQue(max, parche) ? chip("nueva " + max, num(max)[0] > ma ? "naranja" : "azul") : "");
    return '<div class="pp-item"><span class="pp-icono">' + d.icono + "</span><b>" + d.nombre + "</b> <code>" + esc(d.actual) + "</code>" + estado + "</div>";
  });
  if (!filas.length) return "";
  return '<div class="pp-tarjeta pp-c-azul pp-ancha"><div class="pp-tarjeta-titulo">📦 Versiones</div><div class="pp-tarjeta-pie">' + filas.join("") + chip("consultado el " + datosVersiones.fecha, "gris") + "</div></div>";
}

// ── CI (GitHub Actions) ──
function tarjetaCI() {
  const dir = join(RAIZ, ".github", "workflows");
  if (!existsSync(dir) || !readdirSync(dir).some((f) => /\.ya?ml$/.test(f))) return '<div class="pp-tarjeta pp-c-gris"><div class="pp-tarjeta-titulo">🤖 CI</div><div class="pp-tarjeta-valor">sin workflows</div><div class="pp-tarjeta-pie">No hay <code>.github/workflows/</code>: nada comprueba los PR.</div></div>';
  const r = conGithub ? gh("run", "list", "--branch", BASE, "--limit", "1", "--json", "conclusion,status,workflowName,createdAt,url") : null;
  const u = r?.[0];
  if (!u) return '<div class="pp-tarjeta pp-c-gris"><div class="pp-tarjeta-titulo">🤖 CI</div><div class="pp-tarjeta-valor">sin ejecuciones</div></div>';
  const ok = u.conclusion === "success", color = u.status !== "completed" ? "amarillo" : ok ? "verde" : "rojo";
  return '<div class="pp-tarjeta pp-c-' + color + '"><div class="pp-tarjeta-titulo">🤖 CI en ' + esc(BASE) + '</div><div class="pp-tarjeta-valor"><a class="external-link" href="' + u.url + '">' + (u.status !== "completed" ? "⏳ en curso" : ok ? "✔ bien" : "✘ " + esc(u.conclusion)) + "</a></div>" +
    '<div class="pp-tarjeta-pie">' + chip(u.workflowName, "gris") + chip(u.createdAt.slice(0, 10), "gris") + "</div></div>";
}

// ── Cómo arrancar: comandos y URLs (de los docker-compose y de scripts/) ──
function chuleta() {
  const servicios = [];
  for (const f of ["docker-compose-camunda.yml", "docker-compose.yml"]) {
    let nombre = null;
    for (const l of leer(join(RAIZ, f)).split("\n")) {
      const c = l.match(/^\s{4}container_name:\s*(\S+)/); if (c) nombre = c[1];
      const p = l.match(/^\s{6}- "?(\d+):\d+"?\s*(?:#\s*(.*))?$/); if (p && nombre) servicios.push({ nombre, puerto: p[1], nota: p[2] ?? "" });
    }
  }
  const primero = new Map(); for (const s of servicios) if (!primero.has(s.nombre)) primero.set(s.nombre, s);
  const micro = [...primero.values()].filter((s) => s.nombre.startsWith("servicio-"));
  const urls = [];
  if (primero.has("camunda")) urls.push(["🛰️ Operate", "http://localhost:8080/operate"], ["📋 Tasklist", "http://localhost:8080/tasklist"], ["💓 Salud de Camunda", "http://localhost:9600/actuator/health/readiness"]);
  if (primero.has("elasticsearch")) urls.push(["🔎 Elasticsearch", "http://localhost:9200"]);
  const comandos = [["./init.sh", "compila, levanta Camunda y los servicios, y hace el smoke test"], ["./init.sh --solo-compilar", "solo compila"]];
  const dirS = join(RAIZ, "scripts");
  if (existsSync(dirS)) for (const f of readdirSync(dirS).filter((x) => x.endsWith(".sh")).sort()) {
    const lineas = leer(join(dirS, f)).split("\n"), k = lineas.findIndex((l) => /^#[ \t]*Descripción:/.test(l));
    if (k < 0) continue;
    const partes = [lineas[k].replace(/^#[ \t]*Descripción:/, "")];
    for (let j = k + 1; j < lineas.length && /^#/.test(lineas[j]) && !/^#[\s=-]*$/.test(lineas[j]) && !/^#[ \t]*\w+:/.test(lineas[j]); j++) partes.push(lineas[j].slice(1));
    const d = partes.map((p) => p.replace(/^[\s\-•*]*(\p{Extended_Pictographic}\p{M}*\s*)?/u, "").trim()).filter(Boolean).map((p, n, a) => (n < a.length - 1 && /^\p{Lu}/u.test(a[n + 1]) && !/[.,;:]$/.test(p) ? p + " ·" : p)).join(" ");
    if (d) comandos.push(["scripts/" + f, corto(d, 90)]);
  }
  if (existsSync(join(dirS, "logs.sh"))) comandos.push(["scripts/logs.sh <servicio>", "logs de un microservicio (clientes, vuelos, hoteles, coches, pagos, reservas)"]);
  const h = ['<details class="pp-chuleta" open><summary>🚀 Cómo arrancar</summary><div class="pp-tarjetas">'];
  h.push('<div class="pp-tarjeta pp-c-verde pp-ancha"><div class="pp-tarjeta-titulo">⌨️ Comandos</div><div class="pp-tarjeta-pie">' + comandos.map(([c, d]) => '<div class="pp-item"><code>' + esc(c) + '</code><span class="pp-texto">' + esc(d) + "</span></div>").join("") + "</div></div>");
  if (urls.length) h.push('<div class="pp-tarjeta pp-c-morado"><div class="pp-tarjeta-titulo">🔗 Camunda</div><div class="pp-tarjeta-pie">' + urls.map(([n, u]) => '<div class="pp-item">' + n + ' <a class="external-link" href="' + u + '">' + esc(u.replace("http://", "")) + "</a></div>").join("") + "</div></div>");
  if (micro.length) h.push('<div class="pp-tarjeta pp-c-azul"><div class="pp-tarjeta-titulo">🧩 Microservicios</div><div class="pp-tarjeta-pie">' + micro.map((s) => '<div class="pp-item">' + chip(":" + s.puerto, "azul") + esc(s.nombre.replace("servicio-", "")) + ' <a class="external-link" href="http://localhost:' + s.puerto + '/swagger-ui.html">swagger</a> · <a class="external-link" href="http://localhost:' + s.puerto + '/actuator/health">salud</a></div>').join("") + "</div></div>");
  h.push("</div></details>");
  return h.join("");
}

// ── Actividad, milestones y salud ────────────────────────────────────────
const SEMANAS = 26;
function mapaActividad() {
  const desde = new Date(); desde.setUTCHours(0, 0, 0, 0);
  desde.setUTCDate(desde.getUTCDate() - ((desde.getUTCDay() + 6) % 7) - (SEMANAS - 1) * 7); // lunes de hace 25 semanas
  const cuenta = new Map();
  for (const d of git("log", "--all", "--no-merges", "--format=%ad", "--date=short", "--since=" + desde.toISOString().slice(0, 10)).split("\n").filter(Boolean)) cuenta.set(d, (cuenta.get(d) ?? 0) + 1);
  const nivel = (n) => (n === 0 ? 0 : n <= 2 ? 1 : n <= 5 ? 2 : n <= 10 ? 3 : 4);
  const celdas = [], meses = [];
  let total = 0, activos = 0, mesAnterior = "";
  for (let k = 0; k < SEMANAS * 7; k++) {
    const d = new Date(desde); d.setUTCDate(d.getUTCDate() + k);
    const iso = d.toISOString().slice(0, 10);
    if (iso > hoy) { celdas.push('<span class="pp-dia-mapa pp-futuro"></span>'); continue; }
    const n = cuenta.get(iso) ?? 0; total += n; if (n) activos++;
    if (k % 7 === 0 && iso.slice(0, 7) !== mesAnterior) { mesAnterior = iso.slice(0, 7); meses.push('<span style="grid-column: ' + (k / 7 + 1) + ' / span 4">' + MESES[Number(iso.slice(5, 7)) - 1] + "</span>"); }
    celdas.push('<span class="pp-dia-mapa pp-n' + nivel(n) + '" title="' + iso + ": " + n + (n === 1 ? " commit" : " commits") + '"></span>');
  }
  const ultimoDia = [...cuenta.keys()].sort().pop();
  return '<div class="pp-actividad"><div class="pp-actividad-cabecera">' +
    chip("📊 " + total + " commits en " + SEMANAS + " semanas", "azul") + chip("📆 " + activos + " días con commits", "verde") +
    (ultimoDia ? chip("🕘 último el " + ultimoDia, diasEntre(ultimoDia, hoy) > 14 ? "rojo" : "gris") : "") + "</div>" +
    '<div class="pp-mapa-meses" style="grid-template-columns: repeat(' + SEMANAS + ', 12px)">' + meses.join("") + "</div>" +
    '<div class="pp-mapa">' + celdas.join("") + "</div>" +
    '<div class="pp-mapa-leyenda">menos <span class="pp-dia-mapa pp-n0"></span><span class="pp-dia-mapa pp-n1"></span><span class="pp-dia-mapa pp-n2"></span><span class="pp-dia-mapa pp-n3"></span><span class="pp-dia-mapa pp-n4"></span> más</div></div>';
}
function milestones() {
  const lista = conGithub ? gh("api", "repos/" + remoto + "/milestones?state=open") : null;
  if (!lista?.length) return "";
  return '<div class="pp-tarjetas">' + lista.map((m) => {
    const total = m.open_issues + m.closed_issues, pct = total ? Math.round((m.closed_issues * 100) / total) : 0;
    const vence = m.due_on?.slice(0, 10), n = vence ? diasEntre(vence, hoy) : null;
    const plazo = vence ? (n > 0 ? chip("venció hace " + nDias(n), "rojo") : chip("vence en " + nDias(-n), -n < 14 ? "amarillo" : "verde")) : chip("sin fecha", "gris");
    const color = pct === 100 ? "verde" : n > 0 ? "rojo" : "azul";
    return '<div class="pp-tarjeta pp-c-' + color + '"><div class="pp-tarjeta-titulo">🏁 Milestone</div><div class="pp-tarjeta-valor"><a class="external-link" href="' + m.html_url + '">' + esc(m.title) + "</a></div>" +
      '<div class="pp-barra"><span style="width: ' + pct + '%"></span></div><div class="pp-tarjeta-pie">' + chip(m.closed_issues + "/" + total + " cerradas · " + pct + "%", color) + plazo + "</div></div>";
  }).join("") + "</div>";
}
function salud() {
  const t = [];
  const tarjeta = (color, icono, titulo, valor, pie = "") => '<div class="pp-tarjeta pp-c-' + color + '"><div class="pp-tarjeta-titulo">' + icono + " " + titulo + '</div><div class="pp-tarjeta-valor">' + valor + "</div>" + (pie ? '<div class="pp-tarjeta-pie">' + pie + "</div>" : "") + "</div>";
  let init = null; try { init = JSON.parse(leer(join(RAIZ, "logs", "init-ultimo.json"))); } catch {}
  if (init) {
    const f = init.fecha.slice(0, 10), n = diasEntre(f, hoy), ok = init.resultado === "ok";
    t.push(tarjeta(ok ? (n > 14 ? "amarillo" : "verde") : "rojo", "🩺", "Último ./init.sh", (ok ? "✔ bien" : "✘ falló en «" + esc(init.fase) + "»"),
      chip(f + (n ? " · hace " + nDias(n) : " · hoy"), n > 14 ? "amarillo" : "gris") + chip(init.modo, "gris") + (init.modo === "completo" ? chip((init.sanos?.length ?? 0) + "/7 en marcha", ok ? "verde" : "naranja") : "")));
  } else t.push(tarjeta("gris", "🩺", "Último ./init.sh", "sin registro", "Se apunta en " + "<code>logs/init-ultimo.json</code>" + " la próxima vez que lo ejecutes."));
  // tests: archivos en src/test y el último informe de surefire
  let archivos = 0, ejecutados = 0, fallos = 0, saltados = 0, ultimo = 0;
  const recorrer = (dir, fn) => { if (!existsSync(dir)) return; for (const e of readdirSync(dir, { withFileTypes: true })) { const r = join(dir, e.name); e.isDirectory() ? recorrer(r, fn) : fn(r); } };
  for (const m of readdirSync(RAIZ, { withFileTypes: true }).filter((e) => e.isDirectory() && existsSync(join(RAIZ, e.name, "pom.xml")))) {
    recorrer(join(RAIZ, m.name, "src", "test"), (r) => { if (/(Test|Tests|IT)\.java$/.test(r)) archivos++; });
    const inf = join(RAIZ, m.name, "target", "surefire-reports");
    if (existsSync(inf)) for (const f of readdirSync(inf).filter((x) => /^TEST-.*\.xml$/.test(x))) {
      const x = leer(join(inf, f)).match(/<testsuite[^>]*>/)?.[0] ?? "", at = (k) => Number(x.match(new RegExp(" " + k + '="(\\d+)"'))?.[1] ?? 0);
      ejecutados += at("tests"); fallos += at("failures") + at("errors"); saltados += at("skipped");
      ultimo = Math.max(ultimo, statSync(join(inf, f)).mtimeMs);
    }
  }
  const pieTests = ultimo ? chip(ejecutados + " ejecutados", "azul") + (fallos ? chip(fallos + " fallan", "rojo") : chip("0 fallos", "verde")) + (saltados ? chip(saltados + " saltados", "amarillo") : "") + chip("última vez " + new Date(ultimo).toISOString().slice(0, 10), "gris") : "Sin informes de surefire en <code>target/</code>.";
  t.push(tarjeta(archivos === 0 ? "rojo" : fallos ? "rojo" : "verde", "🧪", "Tests", archivos + (archivos === 1 ? " clase de test" : " clases de test"), pieTests));
  t.push(tarjetaCI());
  const v = tarjetaVersiones(); if (v) t.push(v);
  return '<div class="pp-tarjetas">' + t.join("") + "</div>";
}

// ── Panel ────────────────────────────────────────────────────────────────
const o = [];
o.push("---", "type: Panel", "title: Panel del proyecto", "cssclasses:", "  - panel-proyecto",
  "description: Dónde se quedó el proyecto, lo urgente, timeline y limpieza. Lo genera scripts/panel.mjs; no se edita a mano.",
  "generado: " + new Date().toISOString().slice(0, 16).replace("T", " "),
  "apuntado: " + JSON.stringify(apuntes.apuntado),
  "siguiente_paso: " + JSON.stringify(apuntes.siguiente),
  "notas: " + JSON.stringify(apuntes.notas),
  "prioridad: " + JSON.stringify(apuntes.prioridad), "---", "");
o.push("# 🧭 Panel del proyecto", "");
o.push("Generado el " + hoy + " con " + codigo("node scripts/panel.mjs") + " (o " + codigo("/panel") + "). Al terminar una sesión, " + codigo("/panel cerrar") + " apunta el siguiente paso." + (hayGithub ? "" : " **Sin datos de GitHub** (" + codigo("gh") + " no responde o se usó " + codigo("--sin-github") + "): faltan issues y PR."), "");

o.push('<div class="pp-resumen">' + [
  chip("🚦 " + urgentes.length + " urgentes", urgentes.length ? "rojo" : "verde"),
  chip("🤔 " + decs.length + " decisiones", decs.length ? "amarillo" : "verde"),
  chip("📐 " + docs.length + " specs abiertas", docs.length ? "azul" : "verde"),
  chip("🧹 " + (yaResueltas.length + ramasFusionadas.length) + " cosas por limpiar", yaResueltas.length + ramasFusionadas.length ? "naranja" : "verde"),
  chip("🌿 " + rama, "gris"),
].join("") + "</div>", "");
o.push("## 📍 Dónde lo dejé", "");
{
  const cod = (s) => "<code>" + esc(s) + "</code>";
  const hace = (f) => { const n = diasEntre(f, hoy); return n ? chip("hace " + nDias(n), n > 14 ? "rojo" : n > 3 ? "amarillo" : "verde") : chip("hoy", "verde"); };
  const h = [];
  // Tarjeta grande: siguiente paso
  if (apuntes.siguiente) {
    h.push('<div class="pp-heroe pp-heroe-ok"><div class="pp-heroe-titulo">🎯 Siguiente paso ' + (apuntes.apuntado ? chip("apuntado el " + apuntes.apuntado, "morado") + hace(apuntes.apuntado) : "") + '</div><div class="pp-heroe-texto">' + esc(apuntes.siguiente) + "</div>" + (apuntes.notas ? '<div class="pp-heroe-notas">📝 ' + esc(apuntes.notas) + "</div>" : "") + "</div>");
  } else {
    h.push('<div class="pp-heroe pp-heroe-vacio"><div class="pp-heroe-titulo">🎯 Siguiente paso ' + chip("sin apuntar", "amarillo") + '</div><div class="pp-heroe-notas">Al terminar la próxima sesión, usa <code>/panel cerrar</code> para dejarlo escrito.</div></div>');
  }
  // Tarjetas de estado
  const tarjeta = (color, icono, titulo, valor, pie = "") => '<div class="pp-tarjeta pp-c-' + color + '"><div class="pp-tarjeta-titulo">' + icono + " " + titulo + '</div><div class="pp-tarjeta-valor">' + valor + "</div>" + (pie ? '<div class="pp-tarjeta-pie">' + pie + "</div>" : "") + "</div>";
  const t = [];
  t.push(tarjeta(sinCommit.length ? "naranja" : "verde", "🌿", "Rama actual", cod(rama), sinCommit.length ? chip(sinCommit.length + " sin commit", "naranja") : chip("limpia", "verde")));
  if (sesion) t.push(tarjeta("azul", "🕘", "Última sesión", esc(sesion.fin) + " " + hace(sesion.fin), cod(sesion.rama) + " " + chip(sesion.commits.length + " commits", "azul") + (sesion.sinCommit.length ? chip(sesion.sinCommit.length + " sin commit al cerrar", "naranja") : "")));
  const [fUlt, ...resto0] = git("log", "-1", "--format=%ad|%s", "--date=short", BASE).split("|");
  if (fUlt) {
    const msg = resto0.join("|"), nPr = msg.match(/^Merge pull request #(\d+)/)?.[1];
    const pr = nPr && (fusionados ?? []).find((p) => String(p.number) === nPr);
    t.push(tarjeta("morado", "📌", "Último en " + esc(BASE), esc(fUlt) + " " + hace(fUlt), pr ? enlace(urlPrRaw(pr.number), "#" + pr.number) + " " + esc(corto(sinPrefijo(pr.title), 60)) : esc(corto(msg, 70))));
  }
  t.push(tarjeta(abiertos?.length ? "cian" : "gris", "🔀", "PR abiertos", String(abiertos?.length ?? "?"),
    (abiertos ?? []).map((p) => enlace(urlPrRaw(p.number), "#" + p.number) + (p.isDraft ? chip("borrador", "gris") : "") + " " + esc(corto(sinPrefijo(p.title), 40))).join("<br>") || (hayGithub ? "ninguno" : "sin datos de GitHub")));
  if (sinFusionar.length) t.push(tarjeta("rojo", "🌱", "Ramas sin fusionar", String(sinFusionar.length), sinFusionar.map((r) => cod(r.nombre) + " " + chip(r.delante + " commits", "rojo")).join("<br>")));
  h.push('<div class="pp-tarjetas">' + t.join("") + "</div>");
  o.push(h.join("\n"), "");
}

o.push("## 🆕 Desde que lo dejaste", "", seccionDesde(), "");

o.push("## 🚦 Lo más urgente", "");
if (urgentes.length) {
  o.push("| Issue | Por qué | Título | Etiquetas |", "|---|---|---|---|");
  for (const i of urgentes) o.push("| " + urlIssue(i.number) + " | " + chip(iconoMotivo(i.m.por) + " " + i.m.por, colorMotivo(i.m.por)) + " | " + corto(sinPrefijo(i.title), 90).replace(/\|/g, "\\|") + " " + nuevo("issue", i.number) + olvidada(i) + " | " + i.labels.map(chipEtiqueta).join(" ") + " |");
} else if (hayGithub) o.push("Ninguna issue con prioridad apuntada, bug ni spec en marcha.");
o.push("");
if (decs.length) {
  o.push("**Decisiones pendientes** ([cola](factory/decisiones-pendientes.md)):", "");
  for (const d of decs) o.push("- " + (d.tipo === "bloqueante" ? "🔴 **" + d.id + "** (bloqueante)" : "🟡 **" + d.id + "**") + ": " + d.texto);
  o.push("");
}
if (docs.length) {
  o.push("**Specs y trabajo de la factoría sin terminar:**", "");
  for (const d of docs) o.push("- [" + d.titulo + "](" + d.ruta + ") · " + d.tipo + " · " + d.estado + (d.riesgo ? " · riesgo " + d.riesgo : "") + (d.tarea ? " · " + urlIssue(d.tarea) : ""));
  o.push("");
}

o.push(chuleta(), "");

o.push("## 🕒 Timeline", "");
o.push(timeline() || "Sin PR fusionados que mostrar.", "");

o.push("## 📈 Actividad y salud", "");
o.push([mapaActividad(), milestones(), salud()].filter(Boolean).join("\n"), "");

o.push("## 🧹 Limpieza", "");
const limpieza = [];
if (yaResueltas.length) limpieza.push("**Issues abiertas cuya rama ya se fusionó** (¿cerrarlas?): " + yaResueltas.map((i) => urlIssue(i.number) + " (" + i.pr + ")").join(", "));
if (ramasFusionadas.length) limpieza.push("**" + ramasFusionadas.length + " ramas locales ya fusionadas en " + BASE + "**: " + ramasFusionadas.map((r) => codigo(r.nombre)).join(", "));
if (mainDetras) limpieza.push("**" + codigo("main") + " va " + mainDetras + " commits por detrás de " + codigo(BASE) + "**: falta una release.");
const noVersionados = sinCommit.filter((l) => l.startsWith("??")).map((l) => codigo(l.slice(3)));
if (noVersionados.length) limpieza.push("**Archivos sin versionar**: " + noVersionados.join(", "));
o.push(limpieza.length ? limpieza.map((x) => "- " + x).join("\n") : "Nada que limpiar.", "");

if (resto.length) {
  const grupos = new Map();
  for (const i of resto) { const g = etiquetas(i)[0] ?? "sin etiqueta"; grupos.set(g, [...(grupos.get(g) ?? []), i]); }
  o.push("> [!note]- 📋 Resto de issues abiertas (" + resto.length + ")");
  for (const [g, lista] of grupos) {
    o.push("> **" + g + "**", ">");
    for (const i of lista) o.push("> - " + urlIssue(i.number) + " " + corto(sinPrefijo(i.title), 90) + " " + nuevo("issue", i.number) + (olvidada(i) || chip("actualizada el " + i.updatedAt.slice(0, 10), "gris")));
    o.push(">");
  }
  o.pop();
  o.push("");
}

writeFileSync(PANEL, o.join("\n"));
if (args.siguiente !== undefined) guardarFoto();
console.log("Panel escrito en brain/Panel.md" + (hayGithub ? "" : " (sin datos de GitHub)") + ": " + urgentes.length + " urgentes, " + decs.length + " decisiones, " + yaResueltas.length + " issues para cerrar, " + ramasFusionadas.length + " ramas para borrar.");
