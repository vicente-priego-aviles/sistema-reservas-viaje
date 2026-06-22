#!/usr/bin/env python3
"""
Elimina de Camunda Operate TODAS las instancias de proceso existentes,
independientemente de su estado.

Las instancias activas se cancelan primero vía Zeebe REST API y luego
se eliminan de Operate una vez alcanzan estado terminal.

Uso:
  ./limpiar-instancias.sh
"""

import subprocess
import json
import sys
import time

OPERATE_URL = "http://localhost:8081"
USER = "demo"
PASSWORD = "demo"

TERMINAL_STATES = {"COMPLETED", "CANCELED"}


def get_cookie():
    out = subprocess.check_output(
        ["curl", "-s", "-i", "-X", "POST",
         f"{OPERATE_URL}/api/login?username={USER}&password={PASSWORD}"],
        text=True,
    )
    for line in out.splitlines():
        if "OPERATE-SESSION" in line:
            return line.split("OPERATE-SESSION=")[1].split(";")[0]
    print("ERROR: no se pudo obtener sesión de Operate", file=sys.stderr)
    sys.exit(1)


def operate_get(cookie, path, body):
    cmd = [
        "curl", "-s",
        "-H", f"Cookie: OPERATE-SESSION={cookie}",
        "-H", "Content-Type: application/json",
        "-d", json.dumps(body),
        f"{OPERATE_URL}{path}",
    ]
    return json.loads(subprocess.check_output(cmd, text=True))


def operate_delete(cookie, key):
    cmd = [
        "curl", "-s", "-X", "DELETE",
        "-H", f"Cookie: OPERATE-SESSION={cookie}",
        "-H", "Content-Type: application/json",
        f"{OPERATE_URL}/v1/process-instances/{key}",
    ]
    resp = subprocess.check_output(cmd, text=True)
    data = json.loads(resp)
    return data.get("deleted", 0) == 1, resp


def operate_cancel(cookie, key):
    cmd = [
        "curl", "-s", "-o", "/dev/null", "-w", "%{http_code}",
        "-X", "POST",
        "-H", f"Cookie: OPERATE-SESSION={cookie}",
        "-H", "Content-Type: application/json",
        "-d", json.dumps({"operationType": "CANCEL_PROCESS_INSTANCE"}),
        f"{OPERATE_URL}/api/process-instances/{key}/operation",
    ]
    return subprocess.check_output(cmd, text=True).strip()


def fetch_all_instances(cookie):
    instances = []
    sort_values = None
    while True:
        body = {"pageSize": 50}
        if sort_values:
            body["searchAfter"] = sort_values
        result = operate_get(cookie, "/v1/process-instances/search", body)
        items = result.get("items", [])
        if not items:
            break
        instances.extend(items)
        sort_values = result.get("sortValues")
        if len(instances) >= result.get("total", 0):
            break
    return instances


def wait_for_terminal(cookie, keys, timeout=30):
    """Polls Operate until all given keys reach a terminal state or timeout."""
    deadline = time.time() + timeout
    pending = set(keys)
    while pending and time.time() < deadline:
        time.sleep(2)
        instances = fetch_all_instances(cookie)
        state_map = {inst["key"]: inst["state"] for inst in instances}
        pending = {k for k in pending if state_map.get(k) not in TERMINAL_STATES}
        if pending:
            print(f"  Esperando cancelación: {len(pending)} instancia(s)...")
    if pending:
        print(f"  AVISO: {len(pending)} instancia(s) no alcanzaron estado terminal tras {timeout}s",
              file=sys.stderr)


def main():
    cookie = get_cookie()

    print("Obteniendo instancias de Operate...")
    instances = fetch_all_instances(cookie)
    print(f"Total encontradas: {len(instances)}")

    if not instances:
        print("No hay instancias que eliminar.")
        return

    # Cancelar las instancias activas vía Zeebe antes de borrarlas
    active = [inst for inst in instances if inst["state"] not in TERMINAL_STATES]
    if active:
        print(f"Cancelando {len(active)} instancia(s) activa(s) vía Zeebe...")
        for inst in active:
            key = inst["key"]
            status = operate_cancel(cookie, key)
            if status not in ("200", "204"):
                print(f"  AVISO: cancelación de {key} devolvió HTTP {status}", file=sys.stderr)
        wait_for_terminal(cookie, [inst["key"] for inst in active])
        # Refrescar lista tras esperar
        instances = fetch_all_instances(cookie)

    deleted = 0
    errors = 0
    for inst in instances:
        key = inst["key"]
        ok, raw = operate_delete(cookie, key)
        if ok:
            deleted += 1
        else:
            errors += 1
            print(f"  ERROR al eliminar {key}: {raw[:150]}", file=sys.stderr)
        if deleted % 20 == 0 and deleted > 0:
            print(f"  Progreso: {deleted}/{len(instances)}")

    print(f"\nEliminadas: {deleted}  Errores: {errors}")

    remaining = fetch_all_instances(cookie)
    print(f"Instancias restantes en Operate: {len(remaining)}")
    for inst in remaining:
        print(f"  key={inst['key']}  bpmn={inst['bpmnProcessId']}  "
              f"state={inst['state']}  parent={inst.get('parentKey', '-')}")


if __name__ == "__main__":
    main()
