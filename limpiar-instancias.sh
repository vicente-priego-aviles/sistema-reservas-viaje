#!/usr/bin/env python3
"""
Elimina de Camunda Operate todas las instancias de proceso excepto
las indicadas como argumento y sus procesos hijos.

Uso:
  ./limpiar-instancias.sh <key1> [key2 ...]

Ejemplo:
  ./limpiar-instancias.sh 2251799814089895 2251799813880244
"""

import subprocess
import json
import sys

OPERATE_URL = "http://localhost:8081"
USER = "demo"
PASSWORD = "demo"


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


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    try:
        keep_roots = {int(k) for k in sys.argv[1:]}
    except ValueError:
        print("ERROR: los argumentos deben ser números enteros (keys de proceso)", file=sys.stderr)
        sys.exit(1)

    cookie = get_cookie()

    print("Obteniendo instancias de Operate...")
    instances = fetch_all_instances(cookie)
    print(f"Total encontradas: {len(instances)}")

    # Expandir conjunto a conservar: raíces + todos sus hijos directos
    keep_keys = set(keep_roots)
    for inst in instances:
        if inst.get("parentKey") in keep_roots:
            keep_keys.add(inst["key"])

    to_delete = [inst["key"] for inst in instances if inst["key"] not in keep_keys]

    if not to_delete:
        print("No hay instancias que eliminar.")
        return

    print(f"A conservar ({len(keep_keys)}): {sorted(keep_keys)}")
    print(f"A eliminar: {len(to_delete)}")

    deleted = 0
    errors = 0
    for key in to_delete:
        ok, raw = operate_delete(cookie, key)
        if ok:
            deleted += 1
        else:
            errors += 1
            print(f"  ERROR al eliminar {key}: {raw[:150]}", file=sys.stderr)
        if deleted % 20 == 0 and deleted > 0:
            print(f"  Progreso: {deleted}/{len(to_delete)}")

    print(f"\nEliminadas: {deleted}  Errores: {errors}")

    # Verificación final
    remaining = fetch_all_instances(cookie)
    print(f"Instancias restantes en Operate: {len(remaining)}")
    for inst in remaining:
        print(f"  key={inst['key']}  bpmn={inst['bpmnProcessId']}  "
              f"state={inst['state']}  parent={inst.get('parentKey', '-')}")


if __name__ == "__main__":
    main()
