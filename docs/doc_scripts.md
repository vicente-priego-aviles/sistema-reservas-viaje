# 📜 Scripts del Sistema de Reservas de Viaje

Guía completa de los scripts disponibles para gestionar el ciclo de vida del sistema.

---

## 📋 Resumen Rápido

| Script | Propósito | Cuándo usarlo |
|--------|-----------|---------------|
| [`build-and-run.sh`](#-build-and-runsh) | Compilar + levantar todo el sistema | Primera vez o tras cambios en el código |
| [`start.sh`](#-startsh) | Levantar el sistema sin recompilar | Reinicio rápido sin cambios de código |
| [`stop.sh`](#-stopsh) | Parar solo los microservicios | Cuando quieres mantener Camunda activo |
| [`stop-all.sh`](#-stop-allsh) | Parar todo el sistema | Parada completa, incluyendo Camunda |
| [`build-all.sh`](#-build-allsh) | Compilar + levantar microservicios | Si Camunda ya está corriendo |
| [`redeploy-bpmn.sh`](#-redeploy-bpmnsh) | Recompilar y redesplegar solo los BPMN | Tras modificar archivos BPMN |
| [`limpieza.sh`](#-limpiezash) | Limpiar todo el entorno Docker | Resolver problemas o liberar recursos |
| [`reset-cliente.sh`](#-reset-clientesh) | Resetear estado de un cliente a ACTIVO | Cliente bloqueado en EN_PROCESO_RESERVA tras cancelar una prueba |
| [`limpiar-instancias.sh`](#-limpiar-instanciassh) | Eliminar todas las instancias de proceso de Operate | Limpiar el historial antes de una nueva ronda de pruebas |

---

## ⚠️ Requisito previo al primer uso

`docker-compose-camunda.yml` requiere el archivo `connector-secrets.txt` para arrancar el servicio de Connectors. Este archivo **no está en el repositorio** (está en `.gitignore` por contener posibles secretos). Créalo a partir de la plantilla incluida:

```bash
cp connector-secrets.txt.example connector-secrets.txt
```

Para desarrollo local puedes dejarlo con el contenido por defecto (vacío). Si usas Camunda Connectors con servicios externos (Slack, HTTP, etc.), añade las variables de entorno necesarias en ese archivo.

---

## 🚀 build-and-run.sh

**Script principal de arranque.** Ejecuta el flujo completo desde cero: compilación, construcción de imágenes y despliegue de toda la plataforma.

```bash
./scripts/build-and-run.sh
```

### Qué hace paso a paso

1. ✅ Verifica que Maven y Docker están instalados
2. 🌐 Crea la red Docker de Camunda (si no existe)
3. 🐳 Levanta **Camunda Platform 8** (Zeebe, Operate, Tasklist)
4. ⏳ Espera a que Camunda esté listo (health check automático)
5. 🔧 Compila todos los microservicios con `mvn clean package -DskipTests`
6. 🏗️ Construye las imágenes Docker de cada microservicio
7. 🚀 Levanta todos los microservicios

### Cuándo usarlo

- ✅ Primera vez que arrancas el sistema
- ✅ Tras modificar código en cualquier microservicio
- ✅ Tras un `./scripts/stop-all.sh` o `./scripts/limpieza.sh`

### Tiempo estimado

⏱️ 3–5 minutos

### Servicios disponibles tras ejecutarlo

| Servicio | URL |
|---------|-----|
| 📊 Camunda Operate | http://localhost:8081 (demo/demo) |
| 📋 Camunda Tasklist | http://localhost:8082 (demo/demo) |
| 👥 Clientes | http://localhost:9080 |
| ✈️ Vuelos | http://localhost:9081 |
| 🏨 Hoteles | http://localhost:9082 |
| 🚗 Coches | http://localhost:9083 |
| 💳 Pagos | http://localhost:9084 |
| 🎯 Reservas (API) | http://localhost:9090 |

---

## ▶️ start.sh

**Arranque rápido sin recompilar.** Levanta Camunda y los microservicios usando las imágenes Docker ya construidas.

```bash
./scripts/start.sh
```

### Qué hace paso a paso

1. 🌐 Crea la red Docker de Camunda (si no existe)
2. 🐳 Levanta **Camunda Platform 8**
3. ⏳ Espera a que Camunda esté listo (health check)
4. 🚀 Levanta todos los microservicios

### Cuándo usarlo

- ✅ Reiniciar el sistema tras un `./scripts/stop-all.sh`
- ✅ Cuando el código no ha cambiado y las imágenes Docker ya están construidas

### Requisito previo

Las imágenes Docker deben existir (generadas previamente con `./scripts/build-and-run.sh`).

---

## 🛑 stop.sh

**Para solo los microservicios**, dejando Camunda Platform activo.

```bash
./scripts/stop.sh
```

### Qué hace

1. 🛑 Para y elimina los contenedores de todos los microservicios
2. ✅ Camunda (Zeebe, Operate, Tasklist) **sigue corriendo**

### Cuándo usarlo

- ✅ Quieres reiniciar solo los microservicios (p.ej. tras un cambio de código)
- ✅ Liberar recursos de los microservicios manteniendo el estado de Camunda

### Flujo típico

```bash
./scripts/stop.sh           # Para microservicios
# ... haces cambios en el código ...
mvn clean package -DskipTests  # Recompilas
docker-compose up -d --build    # Reconstruyes y levantas
# o simplemente:
./scripts/build-all.sh      # Si Camunda ya está corriendo
```

---

## ⏹️ stop-all.sh

**Para todo el sistema**, incluyendo Camunda Platform.

```bash
./scripts/stop-all.sh
```

### Qué hace paso a paso

1. 🛑 Para y elimina los contenedores de todos los microservicios
2. 🛑 Para y elimina los contenedores de Camunda (Zeebe, Operate, Tasklist, Elasticsearch)

### Cuándo usarlo

- ✅ Parada completa al final del día
- ✅ Antes de un reinicio completo del sistema
- ✅ Cuando quieres liberar todos los recursos Docker

### Para volver a levantar

```bash
./scripts/build-and-run.sh  # Si necesitas recompilar
./scripts/start.sh          # Si las imágenes ya están construidas
```

---

## 🔨 build-all.sh

**Compilación y despliegue de microservicios**, asumiendo que Camunda ya está corriendo.

```bash
./scripts/build-all.sh
```

### Qué hace paso a paso

1. ✅ Verifica que Maven y Docker están instalados
2. 🔧 Compila todos los microservicios con `mvn clean package -DskipTests`
3. 🏗️ Construye las imágenes Docker de los microservicios
4. 🚀 Levanta los microservicios con `docker-compose up -d`

### Cuándo usarlo

- ✅ Camunda ya está corriendo y solo necesitas reconstruir los microservicios
- ✅ Alternativa más rápida a `build-and-run.sh` si Camunda está activo

### Requisito previo

Camunda Platform debe estar corriendo. Si no lo está, usa `./scripts/build-and-run.sh`.

---

## 🔄 redeploy-bpmn.sh

**Recompila y redespliega los archivos BPMN** sin tocar el resto de microservicios. Es el script a usar cada vez que modificas un archivo `.bpmn` y quieres que Zeebe reciba la nueva versión.

```bash
./scripts/redeploy-bpmn.sh
```

### Qué hace paso a paso

1. ✅ Verifica que Maven y Zeebe están disponibles
2. 🔧 Compila **todos** los módulos con `mvn clean package -DskipTests`
   > La compilación completa es necesaria para que MapStruct genere correctamente los mapeadores de `servicio-reservas`. Construir solo ese módulo con `-pl` puede dejar beans sin registrar.
3. 🐳 Reconstruye y reinicia **únicamente** el contenedor `servicio-reservas`
4. ⏳ Espera a que el servicio esté listo (`/actuator/health`)
5. ✅ Al arrancar, `BpmnDeploymentService` despliega automáticamente los BPMN actualizados en Zeebe

### Cuándo usarlo

- ✅ Tras modificar cualquier archivo en `servicio-reservas/src/main/resources/bpmn/`
- ✅ Camunda y el resto de microservicios siguen corriendo sin interrupciones

### Flujo típico de edición BPMN

```bash
# 1. Edita el BPMN en Camunda Modeler (carpeta bpmn/)
# 2. Redesplegar (sincroniza automáticamente bpmn/ → runtime y reconstruye):
./scripts/redeploy-bpmn.sh
# 3. Verificar nueva versión en Operate: http://localhost:8081
```

### Tiempo estimado

⏱️ ~30 segundos

---

## 🧹 limpieza.sh

**Limpieza total del entorno Docker.** ⚠️ Operación destructiva: elimina todos los contenedores y redes del sistema Docker.

```bash
./scripts/limpieza.sh
```

### Qué hace

1. 🗑️ Para y elimina **todos** los contenedores Docker del sistema
2. 🌐 Elimina todas las redes Docker personalizadas
3. 🧽 Ejecuta `docker system prune` para liberar espacio en disco

### ⚠️ Advertencia

Este script afecta a **todos los contenedores Docker** de tu máquina, no solo los de este proyecto. Úsalo con precaución si tienes otros proyectos Docker corriendo.

### Cuándo usarlo

- ✅ Resolver conflictos de red o contenedores corruptos
- ✅ Liberar espacio en disco
- ✅ Empezar desde un estado completamente limpio

### Después de limpiar

```bash
./scripts/build-and-run.sh  # Reconstruye y levanta todo desde cero
```

---

## 🔧 reset-cliente.sh

**Resetea el estado de un cliente a `ACTIVO`.** Necesario cuando un proceso BPMN se cancela o falla a mitad del flujo y el cliente queda bloqueado en `EN_PROCESO_RESERVA`, impidiendo lanzar nuevas pruebas.

```bash
# Resetea el cliente de prueba por defecto (Juan Pérez García)
./scripts/reset-cliente.sh

# Resetea un cliente concreto pasando su UUID
./scripts/reset-cliente.sh b23e4567-e89b-12d3-a456-426655440010
```

### Qué hace

1. Llama a `POST /dev/clientes/{id}/reset-estado` en `servicio-clientes`
2. El endpoint cambia el estado del cliente a `ACTIVO` directamente en la base de datos
3. Muestra el estado anterior y confirma el cambio

### Cuándo usarlo

- ✅ El worker `actualizar-estado-en-proceso` falla con "Transición de estado no permitida: EN_PROCESO_RESERVA → EN_PROCESO_RESERVA"
- ✅ Antes de relanzar una prueba tras cancelar el proceso anterior a mitad del flujo

### Requisito previo

`servicio-clientes` debe estar corriendo. El endpoint `/dev/clientes/...` lo expone `DevAdminController`, que solo existe en este servicio.

---

## 🗑️ limpiar-instancias.sh

**Elimina todas las instancias de proceso de Camunda Operate**, independientemente de su estado (activas, completadas, con error, canceladas). Útil para dejar Operate completamente limpio antes de una nueva ronda de pruebas.

```bash
./scripts/limpiar-instancias.sh
```

### Qué hace paso a paso

1. 🔑 Obtiene sesión autenticada en Operate (`demo/demo`)
2. 📋 Obtiene todas las instancias existentes (paginando de 50 en 50)
3. 🗑️ Elimina **todas** las instancias sin excepción
4. ✅ Muestra un resumen final con el número eliminado y las instancias restantes

### Cuándo usarlo

- ✅ Antes de lanzar una nueva ronda de pruebas desde cero
- ✅ Cuando Operate acumula instancias de sesiones de prueba anteriores

### Requisito previo

Camunda Operate debe estar corriendo en `http://localhost:8081`.

### Ejemplo de salida

```
Obteniendo instancias de Operate...
Total encontradas: 24
  Progreso: 20/24

Eliminadas: 24  Errores: 0
Instancias restantes en Operate: 0
```

---

## 🔄 Flujos de Trabajo Habituales

### Primer arranque

```bash
./scripts/build-and-run.sh
```

### Ciclo de desarrollo normal (código Java)

```bash
# Parar microservicios
./scripts/stop.sh

# Hacer cambios en el código...

# Recompilar y levantar (Camunda sigue activo)
./scripts/build-all.sh
```

### Cambio solo en archivos BPMN

```bash
# Editar BPMN en Camunda Modeler (carpeta bpmn/)
# El script sincroniza automáticamente al runtime y redespliega:
./scripts/redeploy-bpmn.sh
```

### Parada y reinicio completo

```bash
./scripts/stop-all.sh
# más tarde...
./scripts/start.sh
```

### Resolver problemas graves

```bash
./scripts/limpieza.sh
./scripts/build-and-run.sh
```
