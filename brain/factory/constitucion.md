---
type: Constitución
title: Constitución de sistema-reservas-viaje
description: Reglas fijas del proyecto y definición de terminado; cambiarlas requiere confirmación.
tags: [constitucion, factoria]
generated: { by: meta-harness-factory, at: 2026-11-27 }
status: stable
---

# Constitución

> [!success] Estable
> Revisada y aprobada por Vicente el 2026-11-27. Cambiarla requiere su confirmación y una fila en el registro de enmiendas.

## 1. Stack canónico
- Build: Maven multimódulo con Maven Wrapper (`mvnw`); en el agente, `node .mh/scripts/mvnq.mjs <objetivos>`.
- Java + Spring Boot (versión en el `pom.xml` raíz; hoy 4.1.0).
- Camunda Platform 8 / Zeebe con `camunda-spring-boot-starter` (versión en el `pom.xml` raíz; hoy 8.9.6) e imagen unificada `camunda/camunda` en Docker.
- jMolecules (DDD) + ArchUnit.
- Lombok + MapStruct, con `lombok-mapstruct-binding` para que Lombok procese antes que MapStruct.
- springdoc-openapi para la documentación de las API REST.
- Persistencia: H2 en memoria en desarrollo, una base de datos por servicio; PostgreSQL en el perfil `prod`.
- Flyway: obligatorio solo en el perfil `prod`; en desarrollo con H2 no se usa.
- Tests: JUnit 5, Mockito, Testcontainers (PostgreSQL) y `@ZeebeSpringTest`.

## 2. Arquitectura
- C1. El dominio no depende de Spring ni de infraestructura (lo comprueba ArchUnit).
- C2. Arquitectura hexagonal en cada microservicio: `infraestructura` → `aplicacion` → `dominio`, nunca al revés (lo comprueba ArchUnit).
- C3. Cada microservicio tiene su propia base de datos; ningún servicio lee ni escribe la de otro.
- C4. `servicio-reservas` orquesta y despliega los procesos BPMN; no tiene workers Zeebe.

> [!todo] C1 y C2 aún no tienen test
> Existe la dependencia de ArchUnit, pero ningún `ArchitectureTest`. Hay que crearlos en cada microservicio; hasta entonces C1 y C2 se revisan a mano (subagente `revisor-ddd`) y se declaran «no ejecutado» en la evidencia.

## 3. Procesos
- P1. Un proceso BPMN desplegado es un contrato: ningún `.bpmn` se modifica sin autorización explícita previa de Vicente, y todo cambio en uno es riesgo 2.
- P2. Las copias de `bpmn/` (para Camunda Modeler) y de `servicio-reservas/src/main/resources/bpmn/` (runtime) se mantienen iguales.
- P3. Los errores de negocio de un worker se lanzan como `ZeebeBpmnError` con un código que coincide literalmente con el del evento de borde del BPMN.
- P4. Los workers son idempotentes: Zeebe puede entregar el mismo job más de una vez.
- P5. Saga: cada tarea de reserva tiene su tarea de compensación; no se añade una reserva sin su cancelación.

## 4. Calidad
- Q1. TDD; ningún test se debilita ni desactiva para que pase.

## 5. Seguridad
- S1. Ningún secreto en el repositorio.

## 6. Git
- G1. Riesgo 2 → PR con aprobación humana.
- G2. Código, commits, issues y PR en español; notas del brain en español.
- G3. `develop` es la rama base: las ramas salen de `develop` y los PR van a `develop`; `main` solo recibe merges desde `develop`.
- G4. Ramas con el formato `<issue>-<tipo>-<descripcion>` (por ejemplo, `28-chore-migrar-camunda-87-a-89-imagen-unificada`).
- G5. Commits con Conventional Commits y gitmoji obligatorio al principio (lo exige el hook `convencion-commit`).

## 7. Definición de terminado
- [ ] `node .mh/scripts/mvnq.mjs verify` en verde
- [ ] Tests de ArchUnit en verde (mientras no existan, C1 y C2 declarados «no ejecutado»)
- [ ] Evidencia del comportamiento pedido (FTD o verificación)
- [ ] Controles no ejecutados declarados como «no ejecutado»

## 8. Registro de enmiendas
| Fecha | Cambio | Aprobado por |
|---|---|---|
| 2026-11-27 | Borrador inicial | — |
| 2026-11-27 | Stack completado; Flyway solo en `prod`; C2–C4; P1 endurecida y P2–P5; G3–G5; ArchUnit pendiente de crear | Vicente |
| 2026-11-28 | G5: gitmoji obligatorio, alineado con `git.convencion: conventional-gitmoji` (#70) | Vicente |
