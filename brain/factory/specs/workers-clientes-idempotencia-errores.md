---
type: Spec
title: Workers de servicio-clientes idempotentes y con errores clasificados
description: Zeebe podrá reentregar cualquier job de servicio-clientes sin duplicar efectos ni desviar el proceso, y solo los resultados de negocio modelados llegarán al BPMN como error.
tags: [spec, clientes, camunda, workers, idempotencia]
estado: borrador
rama:
riesgo: 2
sources:
  - { resource: ../../wiki/index.md }
  - { resource: ../constitucion.md }
  - { resource: ../revision.md }
  - { resource: ../../wiki/factoria/glosario.md }
  - { resource: ../../wiki/producto/glosario.md }
  - { resource: ../../../.claude/rules/camunda.md }
  - { resource: ../../../.claude/rules/java.md }
  - { resource: ../../../.claude/rules/dominio.md }
  - { resource: ../../../.claude/skills/camunda-8/SKILL.md }
  - { resource: https://github.com/vicente-priego-aviles/sistema-reservas-viaje/issues/65 }
generated: { by: agent:claude-opus-5-5, at: 2026-11-09 }
---

# Workers de servicio-clientes idempotentes y con errores clasificados

> [!info] Origen
> Redactada el 2026-11-09 en el piloto de meta-harness (`sistema-reservas-viaje-piloto`, sobre el commit `20e59d4` de `develop`, el mismo del que parte esta rama) y copiada aquí el 2026-11-27 al instalar el harness (#67). Es la issue [#65](https://github.com/vicente-priego-aviles/sistema-reservas-viaje/issues/65). Sigue en **borrador**: hay que revisarla con Vicente (paso 2 de ASDD) antes de implementar nada. Las referencias al FTD de ArchUnit del piloto se han quitado: ese test todavía no existe aquí.

> [!note] Riesgo
> `riesgo.mjs` **no ejecutado** (todavía no hay cambios que medir). Es una estimación hecha con su TABLA: si solo se tocan `*.java`, sale 1; si se añade `camunda-process-test` (`pom.xml`), una tabla o migración (`*.sql`) o se toca un `.bpmn`, sale 2. Además, cambiar qué errores llegan a los BPMN desplegados afecta al contrato del proceso (P1, `revision.md`). Se recalcula en el plan con los archivos reales.

## Problema
Los 15 workers de `servicio-clientes/.../infraestructura/adaptador/entrada/camunda/` tienen dos defectos:

1. **No son idempotentes.** Zeebe entrega cada job al menos una vez. Si un worker con escritura confirma la transacción en BD pero el `complete` se pierde (caída, timeout del job), el reintento choca con su propio efecto. Casos concretos:
   - `actualizar-estado-cliente` en `subproceso-pago` → `resetear-cliente-activo` (`RESERVA_CONFIRMADA → ACTIVO`). En el reintento, el cliente ya está `ACTIVO`, `validarTransicion("ACTIVO","ACTIVO")` lanza `IllegalStateException` y el worker devuelve `ERROR_TRANSICION_INVALIDA`. Ese error lo recoge el boundary catch-all `evento-error-proceso-pago` y se ejecuta `publicar-compensacion-reserva`. Resultado: **se compensa una reserva ya pagada**.
   - `actualizar-registro-cliente` añade la tarjeta otra vez; al llegar al límite, `ERROR_LIMITE_TARJETAS`.
   - `crear-cliente` → `ERROR_EMAIL_DUPLICADO`; `activar/iniciar/confirmar/finalizar` → `ERROR_ESTADO_INVALIDO`.
2. **Convierten cualquier excepción en `BpmnError`.** Lo hacen con `catch (Exception e)`. Un fallo técnico (BD caída, NPE) sigue entonces el camino de negocio en vez de reintentarse o crear un incidente en Operate. Ejemplos: en `validar-tarjeta-credito`, un fallo técnico sale como `ERROR_TARJETA_INVALIDA` («tarjeta inválida»); `notificar-tarjeta-invalida` se traga la excepción y completa el job. Los `retries="3"` del BPMN nunca se usan.

Afecta al cliente final (reservas compensadas o rechazadas por error) y a operación (no hay incidentes que mirar; hoy se resetea el estado a mano por la consola H2, ver comentario en `application.yml`).

## Coste de no hacerlo
Cada fallo transitorio en el camino de pago o de gestión del cliente termina en un estado de negocio erróneo (reserva compensada, «error en gestión de cliente», cliente atascado en `EN_PROCESO_RESERVA`) sin reintento ni incidente. El problema no se ve y se corrige a mano.

## Alternativa más barata
Quitar solo los `catch (Exception e)` (que lo técnico se propague). **No basta**: con los reintentos que entonces se activan, la falta de idempotencia empeora (cada reintento de una escritura ya confirmada acaba en `ERROR_TRANSICION_INVALIDA` y desvía el proceso). Las dos cosas tienen que ir juntas, worker a worker.

## Objetivos / No objetivos
**Objetivos**
- O1. Los 15 workers aguantan reentregas: ejecutar dos veces el mismo job deja el mismo estado y las mismas variables de negocio, y no lanza error.
- O2. `BpmnError` solo para resultados de negocio; lo transitorio se reintenta con espera (`jobError`) y los bugs se propagan (incidente).
- O3. Se conservan los job types, los códigos de error de negocio, los nombres de las variables de salida y los BPMN desplegados.
- O4. Pruebas de worker (unitarias) e idempotencia (integración con H2) para cada worker.
- O5. Empezar por `ActivarClienteWorker` como patrón.

**No objetivos**
- Modificar los `.bpmn` (ids, códigos, boundary events, retries). Los desajustes que se encuentren se anotan (P10) y no se arreglan aquí.
- Workers de otros servicios (`servicio-pagos`, `servicio-reservas`…).
- Cambiar el envío real de notificaciones (siguen siendo simuladas).
- Desacoplar `Cliente` de `AbstractAggregateRoot` (FTD ArchUnit pendiente).

## Inventario (estado actual)

Ubicación BPMN: PP = `proceso-principal`, GC = `subproceso-gestion-cliente`, PG = `subproceso-pago`, SR = `subproceso-reserva` (proceso `subproceso-proceso-reserva`: se despliega, pero **nadie lo inicia**; `IniciarReservaServicio` solo arranca `proceso-principal`, que incrusta su propio subproceso de reserva).

| # | Worker (job type) | Dónde (BPMN) | Efecto | ¿Idempotente hoy? | Excepción → código | Quién lo capta |
|---|---|---|---|---|---|---|
| 1 | ActivarCliente (`activar-cliente`) | ninguno | escritura `PENDIENTE_VALIDACION→ACTIVO` | **No**: el reintento da `ERROR_ESTADO_INVALIDO` | NoEncontrado→`ERROR_CLIENTE_NO_ENCONTRADO`; IllegalState→`ERROR_ESTADO_INVALIDO`; Exception→`ERROR_ACTIVACION_CLIENTE` | nadie (sin BPMN) |
| 2 | ActualizarInformacionTarjeta (`actualizar-informacion-tarjeta`) | PP (event subprocess sin interrupción `subproceso-actualizar-tarjeta`, mensaje `tarjeta-proporcionada`); SR | escritura (número/CVV/fecha); repositorio y `@Transactional` **en el worker** | Probablemente sí (asignación absoluta), a verificar | NoEncontrado, TarjetaNoEncontrada, Bloqueado, Inactivo → su código; IllegalArgument→`ERROR_CVV_REQUERIDO`/`ERROR_FECHA_PASADO`/`ERROR_SIN_CAMBIOS`/`ERROR_DATOS_INVALIDOS` (por texto del mensaje); Exception→`ERROR_ACTUALIZAR_TARJETA` | no tiene boundary. **Probablemente** lo capta el error start catch-all de `subproceso-manejo-errores` (lanza la compensación completa); a verificar con test de proceso |
| 3 | ActualizarRegistroCliente (`actualizar-registro-cliente`) | SR (compensación manual) | escritura: añade tarjeta | **No**: duplica la tarjeta | NoEncontrado→`ERROR_CLIENTE_NO_ENCONTRADO`; Limite→`ERROR_LIMITE_TARJETAS`; IllegalArgument→`ERROR_VALIDACION_TARJETA`; Exception→`ERROR_ACTUALIZAR_TARJETA` | nadie (SR no se invoca) |
| 4 | ActualizarEstadoCliente (`actualizar-estado-cliente`) | GC `actualizar-estado-en-proceso` (→EN_PROCESO_RESERVA); PG `actualizar-estado-confirmado` (→RESERVA_CONFIRMADA) y `resetear-cliente-activo` (→ACTIVO); PP compensación manual (→ACTIVO) | escritura; inyecta `ClienteServicio` concreto; lee y escribe en **transacciones separadas** | **No**: el reintento da `ERROR_TRANSICION_INVALIDA` | NoEncontrado, Bloqueado, Inactivo → su código; IllegalArgument→`ERROR_DATOS_ENTRADA`; IllegalState→`ERROR_TRANSICION_INVALIDA`; Exception→`ERROR_ACTUALIZAR_ESTADO` | GC: catch-all `evento-error-gestion-cliente` (PP). PG: el boundary `evento-error-actualizacion` espera `ERROR_ACTUALIZACION_CLIENTE`, que **el worker nunca lanza**; todo sube al catch-all `evento-error-proceso-pago` (PP) → compensación. PP manual: nadie → incidente |
| 5 | CargarDatosFormulario (`cargar-datos-formulario`) | PP | lectura | Sí (salida depende de la fecha) | ninguna (propaga) | — |
| 6 | NotificarReservaFallida (`notificar-reserva-fallida`) | PP (sendTask) | ninguno (genera texto) | Sí | IllegalArgument y Exception→`ERROR_NOTIFICACION_FALLIDA` | nadie → incidente sin reintentos |
| 7 | ObtenerDatosCliente (`obtener-datos-cliente`, `retries=3`) | GC | lectura | Sí | NoEncontrado→salida `clienteObtenido=false` (gateway); IllegalArgument→`ERROR_VALIDACION_CLIENTE`; Exception→`ERROR_OBTENER_CLIENTE` | catch-all `evento-error-gestion-cliente` (PP) |
| 8 | IniciarReserva (`iniciar-reserva-cliente`) | ninguno | escritura `ACTIVO→EN_PROCESO_RESERVA` | **No** | NoEncontrado, Bloqueado, Inactivo, IllegalState→`ERROR_ESTADO_INVALIDO`; Exception→`ERROR_INICIAR_RESERVA` | nadie |
| 9 | NotificarTarjetaInvalida (`notificar-tarjeta-invalida`) | PG (sendTask tras `evento-error-pago`) | ninguno | Sí | **se traga** toda excepción y completa con `notificacionEnviada=false` | — |
| 10 | ValidarDatosEntrada (`validar-datos-entrada`) | PP | ninguno (validación pura) | Sí | errores de validación→`ERROR_DATOS_INVALIDOS`; Exception→`ERROR_VALIDACION` | `ERROR_DATOS_INVALIDOS`: boundary `evento-error-datos-invalidos`. `ERROR_VALIDACION`: nadie → incidente |
| 11 | ValidarTarjetaCredito (`validar-tarjeta-credito`, `retries=3`) | GC | lectura + pasarela simulada (salida no determinista: `codigoAutorizacion`) | Sí en efectos | rechazo/expirada/sin tarjetas→`ERROR_TARJETA_INVALIDA`; NoEncontrado→`ERROR_TARJETA_INVALIDA`; **Exception→`ERROR_TARJETA_INVALIDA`** | boundary `evento-error-tarjeta` → fin con error `ERROR_TARJETA_INVALIDA` → catch-all de PP |
| 12 | ValidarCliente (`validar-cliente`) | ninguno | lectura | Sí | NoEncontrado→salida `clienteValido=false`; Exception→`ERROR_VALIDACION_CLIENTE` | nadie |
| 13 | ConfirmarReserva (`confirmar-reserva-cliente`) | ninguno | escritura `EN_PROCESO_RESERVA→RESERVA_CONFIRMADA` | **No** | NoEncontrado; IllegalState→`ERROR_ESTADO_INVALIDO`; Exception→`ERROR_CONFIRMAR_RESERVA` | nadie |
| 14 | CrearCliente (`crear-cliente`) | ninguno | escritura (alta) | **No**: el reintento da `ERROR_EMAIL_DUPLICADO` | EmailDuplicado, DniDuplicado → su código; IllegalArgument→`ERROR_VALIDACION`; Exception→`ERROR_CREACION_CLIENTE` | nadie |
| 15 | FinalizarReserva (`finalizar-reserva-cliente`) | ninguno | escritura `RESERVA_CONFIRMADA→ACTIVO` | **No** | NoEncontrado; IllegalState→`ERROR_ESTADO_INVALIDO`; Exception→`ERROR_FINALIZAR_RESERVA` | nadie |

Hechos de contexto que condicionan la solución:
- Ni `Cliente` ni `ClienteEntidad` guardan el `reservaId` en curso (solo va en el evento) ni tienen `@Version`. Con el estado no se distingue «reintento de la misma reserva» de «otra reserva», y dos ejecuciones concurrentes del mismo job pueden pisarse.
- El dominio señala las violaciones de invariante con `IllegalStateException`/`IllegalArgumentException` de la JDK, no con `ClienteDominioExcepcion`.
- Flyway está comentado en `servicio-clientes/pom.xml`; `application-prod.yml` usa `ddl-auto: validate`. Una tabla nueva exige activar Flyway y añadir una migración.
- No hay tests de workers ni dependencia `camunda-process-test` en el módulo, ni ningún otro test. El test de arquitectura `ArchitectureTest` (FTD ArchUnit) se añade antes que esta spec (#67) y nacerá con la regla `dominio_no_depende_de_spring` **en rojo** a propósito: `Cliente` extiende `AbstractAggregateRoot` de Spring (lo encontró el piloto).
- Seis workers (1, 8, 12, 13, 14, 15) no aparecen en ningún BPMN; el 3 solo aparece en un proceso que no se invoca.

## Las 4 preguntas
### 1. ¿Qué podrá hacer el usuario?
Actor: el motor Zeebe (y, por detrás, operación con Operate). Acción: activar y reentregar cualquier job de los 15 tipos, incluido el mismo job varias veces (reintento, timeout, reasignación). Precondición: instancia de `proceso-principal` o de sus subprocesos en curso, o job creado a mano para los workers sin BPMN. Sin permisos nuevos.

### 2. ¿Qué resultado observable obtiene?
- Reentrega del mismo job tras éxito: el job se completa, el estado del cliente en BD es el mismo que tras la primera ejecución, no hay filas ni tarjetas duplicadas y las variables de negocio de salida son iguales (se excluyen las marcas de tiempo, ver P7).
- Resultado de negocio (cliente inexistente, bloqueado, tarjeta rechazada, datos de formulario inválidos…): `BpmnError` con **el mismo código que hoy** y el mismo camino BPMN.
- Fallo transitorio: el job falla con reintentos decrecientes y espera. Si se agotan, hay un incidente en Operate y la instancia **no** sigue el camino de error de negocio.
- Bug (NPE, variable obligatoria ausente por error de modelado): la excepción se propaga y el SDK reintenta hasta crear un incidente.

### 3. ¿Qué debe respetar?
- Constitución: C1 (el dominio sin Spring), P1 (BPMN = contrato, riesgo 2), Q1 (no se debilita ni desactiva ningún test, incluido el `ArchitectureTest` en rojo), G1 (riesgo 2 → PR con aprobación humana).
- `.claude/rules/camunda.md` y la skill `camunda-8`: workers finos que llaman a un puerto de entrada; `bpmnError` solo para lo modelado; transitorio → `jobError`; bug → que falle. `.claude/rules/java.md`: nada de `catch (Exception e)` que oculte errores; `@Transactional` solo en servicios de aplicación. `.claude/rules/dominio.md`: las invariantes dentro del agregado.
- `revision.md`: romper un contrato (variable de proceso, código de error) bloquea la PR.
- No cambian: los 15 job types, los `.bpmn` de `servicio-reservas/src/main/resources/bpmn/` y `bpmn/`, los códigos de negocio de la tabla, los nombres de las variables de salida, el comportamiento de `obtener-datos-cliente` y `validar-cliente` cuando el cliente no existe (variable a `false`, no error) ni la ruta de `ERROR_DATOS_INVALIDOS`.
- Instancias en vuelo: los BPMN no cambian, así que siguen con su versión. El cambio de comportamiento de los workers sí les aplica desde el despliegue (ver P5).
- Lenguaje ubicuo: el glosario del producto está vacío (plantilla). Se usan los términos del código (`Cliente`, `EstadoCliente`, `TarjetaCredito`, reserva).

### 4. ¿Cómo se demuestra?
- Test unitario por worker (`*WorkerTest`, Mockito, `ActivatedJob` simulado): mapeo de variables y clasificación de cada excepción.
- Test de integración de idempotencia por worker con escritura (`*WorkerIT`, `@SpringBootTest` + H2, servicio y repositorio reales): mismo job dos veces.
- Si se aprueba P8: test de proceso con `camunda-process-test-spring` para los caminos de PG/GC afectados.
- Evidencia: salida de `node .mh/scripts/mvnq.mjs verify` y lista de tests en la verificación de cada PR. El rojo heredado de `ArchitectureTest` se declara como tal, no como «pasado» (ver P11).

## Criterios de aceptación
- **CA1** (idempotencia, transiciones de estado) Dado un cliente en el estado de origen, cuando el worker 1, 4 (cada destino: `EN_PROCESO_RESERVA`, `RESERVA_CONFIRMADA`, `ACTIVO`), 8, 13 o 15 se ejecuta dos veces con el mismo job, entonces las dos ejecuciones terminan sin excepción, el estado final es el destino y las variables de negocio de salida coinciden. *Prueba*: `ActivarClienteWorkerIT.mismoJobDosVeces_mismoEstadoYSalida` (y análogos por worker y destino).
- **CA2** (idempotencia, sin duplicar) Dado un cliente con N tarjetas, cuando `actualizar-registro-cliente` se ejecuta dos veces con el mismo job y la misma tarjeta, entonces el cliente tiene N+1 tarjetas y las dos ejecuciones devuelven el mismo `tarjetaId`. *Prueba*: `ActualizarRegistroClienteWorkerIT.mismoJobDosVeces_noDuplicaTarjeta`.
- **CA3** (idempotencia, alta) Dado que `crear-cliente` ya creó un cliente con un job, cuando se repite el mismo job, entonces no se crea otro cliente y se devuelve el mismo `clienteId`. *Prueba*: `CrearClienteWorkerIT.mismoJobDosVeces_mismoClienteId` (depende de P1/P4).
- **CA4** (idempotencia, tarjeta) Dado una tarjeta actualizada por `actualizar-informacion-tarjeta`, cuando se repite el mismo job, entonces el número enmascarado, el tipo y la fecha no cambian y no hay error. *Prueba*: `ActualizarInformacionTarjetaWorkerIT.mismoJobDosVeces_mismoResultado`.
- **CA5** (lecturas y puros) Dado cualquiera de los workers 5, 6, 7, 9, 10, 11 y 12, cuando se ejecutan dos veces con el mismo job, entonces no escriben en BD y devuelven las mismas variables, salvo marcas de tiempo y `codigoAutorizacion`. *Prueba*: un test unitario por worker con verificación `verifyNoMoreInteractions` sobre los puertos de escritura.
- **CA6** (técnico ≠ negocio) Dado que el caso de uso lanza una excepción técnica (`DataAccessResourceFailureException`, `CannotCreateTransactionException`), cuando cualquiera de los 15 workers procesa el job, entonces la excepción lanzada **no** es `BpmnError` y es el `jobError` con reintentos `job.getRetries()-1` y la espera acordada en P6. *Prueba*: `<Worker>Test.errorTransitorio_esJobErrorConReintento`.
- **CA7** (bug ≠ negocio) Dado que el caso de uso lanza `NullPointerException` o un `RuntimeException` no clasificado, cuando el worker procesa el job, entonces la excepción se propaga sin convertirse en `BpmnError`. *Prueba*: `<Worker>Test.errorInesperado_sePropaga`. Incluye a `notificar-tarjeta-invalida`, que deja de completar con `notificacionEnviada=false` (si se aprueba P2).
- **CA8** (negocio intacto) Dado cada caso de negocio de la tabla del inventario, cuando el worker lo procesa, entonces lanza `BpmnError` con el **mismo código** que hoy y, en `obtener-datos-cliente` y `validar-cliente`, se mantiene la salida `clienteObtenido=false` / `clienteValido=false`. *Prueba*: `<Worker>Test.<caso>_mismoCodigoQueAntes` (tests parametrizados con la tabla).
- **CA9** (transición realmente inválida) Dado un cliente en un estado que no es ni el origen ni el destino (p. ej. `BLOQUEADO` al pedir `RESERVA_CONFIRMADA`), cuando se ejecuta `actualizar-estado-cliente`, entonces sigue lanzando el `BpmnError` de negocio que corresponde hoy (`ERROR_CLIENTE_BLOQUEADO`, `ERROR_TRANSICION_INVALIDA`…). *Prueba*: `ActualizarEstadoClienteWorkerTest.transicionInvalida_mismoCodigo`.
- **CA10** (camino de pago) Dado una instancia de `subproceso-pago` en `resetear-cliente-activo` con el cliente ya en `ACTIVO` (reentrega), cuando se completa el job, entonces la instancia llega a `fin-reserva-exitosa` y no se activa `evento-error-proceso-pago`. *Prueba*: test de proceso `SubprocesoPagoProcessTest.reentregaResetearActivo_noCompensa` (requiere P8; si P8 = no, se cubre con CA1 y se declara «no ejecutado» a nivel de proceso).
- **CA11** (contratos intactos) Dado el cambio completo, cuando se compara con `main`, entonces no hay diferencias en `**/*.bpmn`, `**/*.form` ni en los valores de `@JobWorker(type=...)`. *Prueba*: `git diff --stat main -- '*.bpmn' '*.form'` vacío y `grep -rho '@JobWorker(type = "[^"]*"' servicio-clientes/src/main` idéntico antes y después (salida guardada en la verificación).
- **CA12** (arquitectura) Dado el cambio, cuando se ejecuta `ArchitectureTest`, entonces no aparece ninguna violación nueva (las 3 de `Cliente` heredadas siguen como únicas). *Prueba*: `node .mh/scripts/mvnq.mjs test -pl servicio-clientes -Dtest=ArchitectureTest`, comparando la salida con la evidencia del FTD.

## Puntos abiertos
| # | Tipo | Punto | Decisión |
|---|---|---|---|
| P1 | pregunta abierta | **Mecanismo de idempotencia.** A) Comprobar el estado de dominio: «si ya está en el destino, éxito sin efecto»; en tarjetas, «si ya existe una con ese número, devolverla»; en alta, «si existe con el mismo DNI y email, devolverla». B) Clave de idempotencia por `jobKey` en una tabla `job_procesado` (resultado guardado y devuelto en el reintento). C) A + guardar `reservaIdEnCurso` (y `@Version`) en el agregado. **Recomendación: A** para la primera entrega. No necesita esquema nuevo (B y C exigen activar Flyway, migración y `ddl-auto: validate` en prod → riesgo 2 y más trabajo) y cubre el caso real (reentrega tras éxito). Limitación aceptada: con A no se distingue «misma reserva» de «otra reserva» (P3). C se deja como mejora posterior. | |
| P2 | pregunta abierta | **Qué es negocio y qué es técnico.** Propuesta: negocio = `ClienteNoEncontrado` (donde hoy da error), `ClienteBloqueado`, `ClienteInactivo`, `LimiteMaximoTarjetas`, `TarjetaNoEncontrada`, `EmailDuplicado`/`DniDuplicado` (cuando no son reintento), tarjeta rechazada/expirada/sin tarjetas, errores de validación de `validar-datos-entrada` y transición inválida que no sea reintento. Transitorio = `TransientDataAccessException`, `DataAccessResourceFailureException`, `CannotCreateTransactionException`, `QueryTimeoutException`. Bug = el resto, incluidas las variables obligatorias ausentes (error de modelado; hoy `ERROR_DATOS_ENTRADA`, `ERROR_VALIDACION`, `ERROR_VALIDACION_TARJETA` cuando falta un campo) y las excepciones de `notificar-tarjeta-invalida`. Duda: el dominio usa `IllegalStateException`/`IllegalArgumentException` de la JDK, que también lanzan frameworks. **Recomendación**: introducir excepciones de dominio (`TransicionEstadoInvalidaExcepcion`, `DatosInvalidosExcepcion` que extiendan `ClienteDominioExcepcion`) y clasificar solo por ellas, manteniendo los mismos códigos BPMN. | |
| P3 | pregunta abierta | **Reintento en estados posteriores.** Si `activar-cliente` se reentrega cuando el cliente ya pasó a `EN_PROCESO_RESERVA`, ¿éxito o `ERROR_ESTADO_INVALIDO`? Igual con `ACTIVO` pedido cuando ya está `ACTIVO` por otra reserva. **Recomendación**: éxito si el estado actual es el destino o uno «alcanzable solo después» del destino en el ciclo normal; error si es `BLOQUEADO`/`INACTIVO`. Debe confirmarlo negocio. | |
| P4 | pregunta abierta | **Workers sin BPMN** (1, 8, 12, 13, 14, 15) y el 3 (solo en un proceso que no se invoca). ¿Se migran, se borran o se dejan? **Recomendación**: migrarlos (el alcance pedido es «todos»), con prioridad baja salvo `ActivarClienteWorker` (piloto del patrón). Borrarlos sería otra spec. | |
| P5 | pregunta abierta | **Compatibilidad con instancias en vuelo y códigos de error.** Ningún BPMN cambia y los códigos de negocio se conservan. Lo que cambia: los fallos técnicos dejan de llegar a los catch-all (`evento-error-gestion-cliente`, `evento-error-proceso-pago`, error start de `subproceso-manejo-errores`) y pasan a incidente con reintentos. Desaparecen como salida técnica: `ERROR_ACTIVACION_CLIENTE`, `ERROR_ACTUALIZAR_TARJETA`, `ERROR_ACTUALIZAR_ESTADO`, `ERROR_OBTENER_CLIENTE`, `ERROR_VALIDACION`, `ERROR_VALIDACION_CLIENTE`, `ERROR_INICIAR_RESERVA`, `ERROR_CONFIRMAR_RESERVA`, `ERROR_FINALIZAR_RESERVA`, `ERROR_CREACION_CLIENTE`, `ERROR_NOTIFICACION_FALLIDA` y el `ERROR_TARJETA_INVALIDA` técnico. **Recomendación**: aceptarlo. Ningún boundary específico los captura (solo catch-all o nadie) y las instancias en vuelo se benefician al reintentar. Antes de desplegar, revisar en Operate si hay instancias en incidente para anotarlas. Confirmar que nadie fuera del BPMN (dashboards, otros servicios) depende de esos códigos. | |
| P6 | pregunta abierta | **Reintentos y espera.** Los reintentos los fija el BPMN (3 explícitos en GC; el valor por defecto en el resto) y no se tocan. ¿Espera? A) `jobError` con espera exponencial calculada a partir de los reintentos restantes (p. ej. 5 s, 10 s, 20 s). B) `retryBackoff` fijo en `@JobWorker`. **Recomendación: A**, en un clasificador común en `infraestructura/adaptador/entrada/camunda`. Los bugs se propagan sin `jobError` (espera 0 del SDK) o con la misma espera, a decidir. | |
| P7 | supuesto tomado | En «misma salida» se excluyen las marcas de tiempo generadas con `now()` (`fechaActivacion`, `fechaConfirmacion`, `timestampNotificacion`…) y `codigoAutorizacion` (pasarela simulada). Si se quiere salida idéntica, habría que derivarlas del agregado (`fechaActualizacion`). | |
| P8 | pregunta abierta | **Tests de proceso.** Añadir `camunda-process-test-spring` (cambio en `pom.xml`, riesgo 2) para CA10 y para verificar quién capta los errores del worker 2. **Recomendación**: sí, en `servicio-reservas` (donde viven los BPMN), con los workers de clientes simulados. Si no, CA10 queda «no ejecutado» a nivel de proceso. | |
| P9 | pregunta abierta | **Orden de migración y PR.** **Recomendación: varias PR**, cada una con su `riesgo.mjs`. PR1: clasificador común + `ActivarClienteWorker` (patrón, sin BPMN, bajo riesgo). PR2: transiciones de estado (4, 8, 13, 15), la más crítica por el camino de pago; incluye mover la lógica de 4 al caso de uso (una sola transacción). PR3: escrituras de tarjeta y alta (2, 3, 14); incluye sacar repositorio y `@Transactional` del worker 2. PR4: lecturas y puros (5, 6, 7, 9, 10, 11, 12). Alternativa: una sola PR (más difícil de revisar, mismo riesgo 2). | |
| P10 | decisión aplazada | Desajustes BPMN ↔ worker encontrados y **fuera de alcance** (necesitan cambiar BPMN): el boundary `evento-error-actualizacion` espera `ERROR_ACTUALIZACION_CLIENTE`, que nadie lanza; los BPMN de GC y SR están en la versión de Modeler 8.7.0 y el resto en 8.9.0; SR se despliega pero no se usa. Proponer otra spec. | |
| P11 | pregunta abierta | **Definición de terminado bloqueada** (cuando exista el FTD ArchUnit, que va antes). `mvnq verify` no puede salir en verde mientras `ArchitectureTest.dominio_no_depende_de_spring` siga en rojo (FTD ArchUnit) y Q1 prohíbe debilitarlo. ¿Se resuelve antes el FTD o se acepta «verify en rojo solo por esa regla heredada», declarado en la evidencia? **Recomendación**: resolver el FTD antes de PR2, o aprobar explícitamente la excepción para PR1. | |
| P12 | supuesto tomado | `NumeroTarjeta` se puede comparar por valor para detectar «tarjeta ya añadida» (CA2). Hay que verificarlo en el plan; si se guarda cifrado o enmascarado, CA2 necesita P1-B. | |
