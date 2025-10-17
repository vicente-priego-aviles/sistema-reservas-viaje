# 🤝 Guía de Contribución

¡Gracias por tu interés en contribuir al Sistema de Reservas de Viaje! Este documento proporciona pautas para contribuir al proyecto.

---

## 📋 Tabla de Contenidos

- [Código de Conducta](#-código-de-conducta)
- [¿Cómo Puedo Contribuir?](#-cómo-puedo-contribuir)
- [Proceso de Desarrollo](#-proceso-de-desarrollo)
- [Estándares de Código](#-estándares-de-código)
- [Mensajes de Commit](#-mensajes-de-commit)
- [Pull Requests](#-pull-requests)
- [Reportar Bugs](#-reportar-bugs)
- [Sugerir Mejoras](#-sugerir-mejoras)

---

## 📜 Código de Conducta

Este proyecto adhiere a un Código de Conducta. Al participar, se espera que mantengas este código.

### Nuestro Compromiso

- 🤝 Ser respetuoso y acogedor
- 🎯 Enfocarse en lo mejor para la comunidad
- 💬 Aceptar críticas constructivas
- 🌟 Mostrar empatía hacia otros miembros

---

## 🎯 ¿Cómo Puedo Contribuir?

### 1. 🐛 Reportar Bugs

Antes de crear un reporte de bug:
- ✅ Verifica que no exista un issue similar
- ✅ Determina qué versión estás usando
- ✅ Reproduce el problema en un ambiente limpio

Incluye en tu reporte:
- Descripción clara del problema
- Pasos para reproducir
- Comportamiento esperado vs actual
- Screenshots si aplica
- Información del ambiente (OS, Java, etc.)

### 2. ✨ Sugerir Mejoras

Para sugerir una mejora:
- Crea un issue con la etiqueta `enhancement`
- Describe claramente la propuesta
- Explica por qué sería útil
- Proporciona ejemplos si es posible

### 3. 💻 Contribuir Código

#### Áreas Donde Puedes Contribuir

- **Nuevas Features**: Ver [ROADMAP.md](ROADMAP.md)
- **Bug Fixes**: Ver issues con etiqueta `bug`
- **Documentación**: Mejorar docs, README, ejemplos
- **Tests**: Aumentar cobertura de tests
- **Refactoring**: Mejorar código existente

---

## 🔄 Proceso de Desarrollo

### 1. Fork y Clone

```bash
# Fork el repositorio en GitHub

# Clona tu fork
git clone https://github.com/tu-usuario/sistema-reservas-viaje.git
cd sistema-reservas-viaje

# Añade el repositorio original como remote
git remote add upstream https://github.com/original-usuario/sistema-reservas-viaje.git
```

### 2. Crea una Rama

Usa nombres descriptivos para tus ramas:

```bash
# Para nuevas features
git checkout -b feature/nombre-descriptivo

# Para bug fixes
git checkout -b fix/descripcion-bug

# Para documentación
git checkout -b docs/tema-documentacion

# Para refactoring
git checkout -b refactor/area-refactorizada
```

**Convención de nombres:**
- `feature/` - Nuevas características
- `fix/` - Corrección de bugs
- `docs/` - Cambios en documentación
- `refactor/` - Refactorización de código
- `test/` - Añadir o modificar tests
- `chore/` - Tareas de mantenimiento

### 3. Realiza tus Cambios

#### Estructura del Proyecto

Sigue la arquitectura hexagonal existente:

```
servicio-<nombre>/
└── src/main/java/dev/javacadabra/reservasviaje/<dominio>/
    ├── aplicacion/          # Casos de uso
    ├── dominio/             # Lógica de negocio
    └── infraestructura/     # Adaptadores
```

#### Mantén la Consistencia

- Usa las mismas convenciones que el código existente
- Sigue los principios SOLID
- Aplica Domain-Driven Design
- Respeta la arquitectura hexagonal

### 4. Escribe Tests

```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify -P integration-tests

# Tests de arquitectura
mvn test -Dtest=ArchitectureTests
```

**Requerimientos de Testing:**
- ✅ Cobertura mínima: 80%
- ✅ Tests unitarios para lógica de dominio
- ✅ Tests de integración para adaptadores
- ✅ Tests de arquitectura para validar capas

### 5. Ejecuta Verificaciones

```bash
# Compila el proyecto
mvn clean install

# Verifica el estilo de código
mvn checkstyle:check

# Ejecuta todos los tests
mvn verify

# Levanta el sistema completo
./start.sh
```

---

## 📏 Estándares de Código

### Convenciones Java

#### Nombres
```java
// Clases: PascalCase
public class ClienteServicio { }

// Métodos y variables: camelCase
public void procesarReserva() { }
private String nombreCliente;

// Constantes: UPPER_SNAKE_CASE
private static final int MAX_INTENTOS = 3;

// Paquetes: lowercase
package dev.javacadabra.reservasviaje.cliente;
```

#### Idioma
- **Todo en español**: clases, métodos, variables, comentarios
- **Excepción**: Términos técnicos reconocidos (controller, service, repository)

```java
// ✅ Correcto
public class ClienteServicio {
    private final ClienteRepositorio clienteRepositorio;
    
    /**
     * Valida la tarjeta de crédito del cliente.
     * @param tarjetaNumero número de la tarjeta
     * @return true si la tarjeta es válida
     */
    public boolean validarTarjeta(String tarjetaNumero) {
        // Lógica de validación
    }
}

// ❌ Incorrecto
public class CustomerService {
    public boolean validateCard(String cardNumber) { }
}
```

#### Anotaciones

Usa anotaciones de JMolecules para DDD:

```java
// Agregados
@org.jmolecules.ddd.annotation.AggregateRoot
public class Cliente { }

// Entidades
@org.jmolecules.ddd.annotation.Entity
public class Reserva { }

// Value Objects
@org.jmolecules.ddd.annotation.ValueObject
public record TarjetaCredito(String numero) { }

// Repositorios
@org.jmolecules.ddd.annotation.Repository
public interface ClienteRepositorio { }
```

#### Logs

Usa logs con iconos para claridad:

```java
@Slf4j
public class ReservaServicio {
    
    public void crearReserva(ReservaDTO reservaDTO) {
        log.info("🔍 Iniciando creación de reserva: {}", reservaDTO.getId());
        
        try {
            // Lógica
            log.info("✅ Reserva creada exitosamente: {}", reservaId);
        } catch (Exception e) {
            log.error("❌ Error al crear reserva: {}", e.getMessage());
            throw e;
        }
    }
}
```

**Iconos recomendados:**
- ✅ Éxito
- ❌ Error
- 🔍 Debug/Búsqueda
- ⚠️ Advertencia
- 🚀 Inicio de proceso
- 🔄 Procesando
- 💾 Guardando
- 📤 Enviando
- 📥 Recibiendo

#### Utilidades

Prioriza Apache Commons Lang:

```java
import org.apache.commons.lang3.StringUtils;

// ✅ Correcto
if (StringUtils.isBlank(nombre)) {
    throw new IllegalArgumentException("El nombre no puede estar vacío");
}

String nombreCapitalizado = StringUtils.capitalize(nombre);

// ❌ Incorrecto - No reimplemente funciones existentes
if (nombre == null || nombre.trim().isEmpty()) { }
```

#### Manejo de Excepciones

```java
// Excepciones de dominio
public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(String clienteId) {
        super("Cliente no encontrado: " + clienteId);
    }
}

// Uso
if (cliente == null) {
    log.error("❌ Cliente no encontrado: {}", clienteId);
    throw new ClienteNoEncontradoException(clienteId);
}
```

### Convenciones BPMN

#### IDs de Elementos
```xml
<!-- Usa kebab-case en español -->
<bpmn:serviceTask id="validar-tarjeta-credito" name="Validar Tarjeta de Crédito">
  <bpmn:incoming>flujo-a-validar-tarjeta</bpmn:incoming>
  <bpmn:outgoing>flujo-tarjeta-validada</bpmn:outgoing>
</bpmn:serviceTask>

<!-- Prefija flujos por contexto para evitar duplicados -->
<bpmn:sequenceFlow id="flujo-pago-a-confirmar" />
<bpmn:sequenceFlow id="flujo-cliente-a-validar" />
```

#### Nombres Visuales
- Usa español y emojis para claridad
- User Tasks: Incluye emoji descriptivo
  - `✈️ Revisar Reserva de Vuelo`
  - `🏨 Revisar Reserva de Hotel`
  - `🚗 Revisar Reserva de Coche`

#### Expresiones FEEL
```xml
<!-- ✅ Correcto: Usa FEEL -->
<bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
  =clienteObtenido = true
</bpmn:conditionExpression>

<!-- ❌ Incorrecto: No uses JavaScript -->
<bpmn:conditionExpression xsi:type="bpmn:tFormalExpression" language="javascript">
  clienteObtenido == true
</bpmn:conditionExpression>
```

---

## 📝 Mensajes de Commit

Seguimos [Conventional Commits](https://www.conventionalcommits.org/es/).

### Formato

```
<tipo>[alcance opcional]: <descripción>

[cuerpo opcional]

[nota de pie opcional]
```

### Tipos

- `feat`: Nueva característica
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `style`: Formato, no afecta código
- `refactor`: Refactorización de código
- `test`: Añadir o modificar tests
- `chore`: Tareas de mantenimiento
- `perf`: Mejora de rendimiento
- `ci`: Cambios en CI/CD

### Ejemplos

```bash
# Feature
git commit -m "feat(clientes): añadir validación de email"

# Bug fix
git commit -m "fix(reservas): corregir compensación de vuelos"

# Documentación
git commit -m "docs(readme): actualizar guía de instalación"

# Refactorización
git commit -m "refactor(pagos): extraer lógica de validación"

# Breaking change
git commit -m "feat(api)!: cambiar estructura de respuesta de reservas

BREAKING CHANGE: El campo 'id' ahora es 'reservaId'"
```

### Buenas Prácticas

- ✅ Usa imperativo: "añadir" no "añadido" ni "añade"
- ✅ Primera línea ≤ 72 caracteres
- ✅ Describe el **qué** y el **por qué**, no el **cómo**
- ✅ Referencia issues si aplica: `fix(pagos): corregir timeout #123`

---

## 🔀 Pull Requests

### Antes de Crear el PR

- ✅ Actualiza tu rama con `main` o la rama feature correspondiente
- ✅ Todos los tests pasan
- ✅ El código compila sin warnings
- ✅ Has ejecutado `mvn clean install`
- ✅ Has probado localmente con `./start.sh`

### Crear el PR

1. **Título descriptivo**
   ```
   feat(clientes): añadir endpoint para actualizar email
   ```

2. **Descripción completa**
   ```markdown
   ## 📋 Descripción
   Añade un nuevo endpoint PUT para actualizar el email del cliente.
   
   ## 🎯 Motivación
   Los clientes necesitan poder actualizar su email sin crear un nuevo registro.
   
   ## 🔧 Cambios
   - Añadido endpoint PUT /api/clientes/{id}/email
   - Validación de formato de email
   - Tests unitarios y de integración
   - Documentación OpenAPI actualizada
   
   ## ✅ Checklist
   - [x] Tests añadidos/actualizados
   - [x] Documentación actualizada
   - [x] CHANGELOG.md actualizado
   - [x] Sin breaking changes
   
   ## 📸 Screenshots
   (si aplica)
   
   ## 🔗 Issues relacionados
   Closes #42
   ```

3. **Etiquetas apropiadas**
   - `enhancement`
   - `bug`
   - `documentation`
   - etc.

### Proceso de Revisión

1. **Revisión automática**: CI/CD ejecuta tests
2. **Revisión de código**: Al menos 1 aprobación requerida
3. **Merge**: Squash and merge (por defecto)

### Checklist del Revisor

- ✅ El código sigue los estándares
- ✅ Arquitectura hexagonal respetada
- ✅ Tests adecuados incluidos
- ✅ Documentación actualizada
- ✅ Sin código duplicado
- ✅ Logs apropiados
- ✅ Manejo de errores correcto

---

## 🐛 Reportar Bugs

Usa la plantilla de issue de bug:

```markdown
**Descripción del Bug**
Descripción clara y concisa del problema.

**Pasos para Reproducir**
1. Ir a '...'
2. Ejecutar '...'
3. Ver error

**Comportamiento Esperado**
Qué debería suceder.

**Comportamiento Actual**
Qué sucede actualmente.

**Screenshots**
Si aplica, añade screenshots.

**Ambiente**
- OS: [e.g. Windows 11]
- Java: [e.g. 21.0.1]
- Spring Boot: [e.g. 3.5.6]
- Camunda: [e.g. 8.7.0]
- Versión del proyecto: [e.g. 1.0.0]

**Logs**
```
Pega aquí los logs relevantes
```

**Contexto Adicional**
Cualquier otra información relevante.
```

---

## 💡 Sugerir Mejoras

Usa la plantilla de issue de feature:

```markdown
**¿Tu feature está relacionada con un problema?**
Descripción clara del problema. Ej: "Siempre es frustrante cuando..."

**Describe la Solución que te Gustaría**
Descripción clara de lo que quieres que suceda.

**Describe Alternativas que has Considerado**
Otras soluciones o features alternativas.

**Información Adicional**
Contexto adicional, screenshots, mockups, etc.

**Impacto**
- [ ] Alto: Bloquea funcionalidad crítica
- [ ] Medio: Mejora significativa
- [ ] Bajo: Nice to have

**Esfuerzo Estimado**
- [ ] Pequeño: < 1 día
- [ ] Medio: 1-3 días
- [ ] Grande: > 3 días
```

---

## 🎯 Versiones y Ramas

### Estrategia de Branching

```
main (v1.0.0)
├── feature/vaadin-ui (v2.0.0)
│   └── feature/vaadin-dashboard
│   └── fix/vaadin-bug
├── feature/observability (v3.0.0)
└── feature/security (v4.0.0)
```

### Para Contribuir a una Versión Específica

```bash
# Para v1.0.0 (main)
git checkout main
git checkout -b fix/mi-bug-fix

# Para v2.0.0 (vaadin-ui)
git checkout feature/vaadin-ui
git checkout -b feat/mi-feature-vaadin

# Para v3.0.0 (observability)
git checkout feature/observability
git checkout -b feat/mi-feature-metrics
```

---

## 📚 Recursos Adicionales

### Documentación
- [README.md](README.md) - Visión general
- [ROADMAP.md](ROADMAP.md) - Plan de desarrollo
- [docs/](docs/) - Documentación detallada

### Arquitectura
- [Arquitectura Hexagonal](docs/02-arquitectura.md)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Patrón Saga](https://microservices.io/patterns/data/saga.html)

### Camunda
- [Camunda 8 Docs](https://docs.camunda.io/)
- [BPMN 2.0](https://www.omg.org/spec/BPMN/2.0/)
- [FEEL](https://camunda.github.io/feel-scala/)

### Spring Boot
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

---

## 🙏 Agradecimientos

¡Gracias por contribuir al Sistema de Reservas de Viaje! Tu tiempo y esfuerzo son muy apreciados.

---

## ❓ Preguntas

Si tienes preguntas, puedes:
- 💬 Abrir un issue de discusión
- 📧 Enviar un email a: tu-email@example.com
- 💼 Contactar en LinkedIn: [tu-perfil](https://linkedin.com/in/tu-perfil)

---

**Última actualización**: Diciembre 2024