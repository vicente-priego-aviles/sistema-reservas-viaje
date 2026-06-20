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
./build-and-run.sh
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
- ✅ Tras un `./stop-all.sh` o `./limpieza.sh`

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
./start.sh
```

### Qué hace paso a paso

1. 🌐 Crea la red Docker de Camunda (si no existe)
2. 🐳 Levanta **Camunda Platform 8**
3. ⏳ Espera a que Camunda esté listo (health check)
4. 🚀 Levanta todos los microservicios

### Cuándo usarlo

- ✅ Reiniciar el sistema tras un `./stop-all.sh`
- ✅ Cuando el código no ha cambiado y las imágenes Docker ya están construidas

### Requisito previo

Las imágenes Docker deben existir (generadas previamente con `./build-and-run.sh`).

---

## 🛑 stop.sh

**Para solo los microservicios**, dejando Camunda Platform activo.

```bash
./stop.sh
```

### Qué hace

1. 🛑 Para y elimina los contenedores de todos los microservicios
2. ✅ Camunda (Zeebe, Operate, Tasklist) **sigue corriendo**

### Cuándo usarlo

- ✅ Quieres reiniciar solo los microservicios (p.ej. tras un cambio de código)
- ✅ Liberar recursos de los microservicios manteniendo el estado de Camunda

### Flujo típico

```bash
./stop.sh           # Para microservicios
# ... haces cambios en el código ...
mvn clean package -DskipTests  # Recompilas
docker-compose up -d --build    # Reconstruyes y levantas
# o simplemente:
./build-all.sh      # Si Camunda ya está corriendo
```

---

## ⏹️ stop-all.sh

**Para todo el sistema**, incluyendo Camunda Platform.

```bash
./stop-all.sh
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
./build-and-run.sh  # Si necesitas recompilar
./start.sh          # Si las imágenes ya están construidas
```

---

## 🔨 build-all.sh

**Compilación y despliegue de microservicios**, asumiendo que Camunda ya está corriendo.

```bash
./build-all.sh
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

Camunda Platform debe estar corriendo. Si no lo está, usa `./build-and-run.sh`.

---

## 🔄 redeploy-bpmn.sh

**Recompila y redespliega los archivos BPMN** sin tocar el resto de microservicios. Es el script a usar cada vez que modificas un archivo `.bpmn` y quieres que Zeebe reciba la nueva versión.

```bash
./redeploy-bpmn.sh
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
./redeploy-bpmn.sh
# 3. Verificar nueva versión en Operate: http://localhost:8081
```

### Tiempo estimado

⏱️ ~30 segundos

---

## 🧹 limpieza.sh

**Limpieza total del entorno Docker.** ⚠️ Operación destructiva: elimina todos los contenedores y redes del sistema Docker.

```bash
./limpieza.sh
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
./build-and-run.sh  # Reconstruye y levanta todo desde cero
```

---

## 🔄 Flujos de Trabajo Habituales

### Primer arranque

```bash
./build-and-run.sh
```

### Ciclo de desarrollo normal (código Java)

```bash
# Parar microservicios
./stop.sh

# Hacer cambios en el código...

# Recompilar y levantar (Camunda sigue activo)
./build-all.sh
```

### Cambio solo en archivos BPMN

```bash
# Editar BPMN en Camunda Modeler (carpeta bpmn/)
# El script sincroniza automáticamente al runtime y redespliega:
./redeploy-bpmn.sh
```

### Parada y reinicio completo

```bash
./stop-all.sh
# más tarde...
./start.sh
```

### Resolver problemas graves

```bash
./limpieza.sh
./build-and-run.sh
```
