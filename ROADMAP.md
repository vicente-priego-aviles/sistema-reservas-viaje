# 🗺️ Roadmap del Proyecto - Sistema de Reservas de Viaje

Este documento describe el plan de desarrollo del proyecto a través de diferentes versiones, organizadas en ramas de Git.

---

## 📊 Estado General del Proyecto

| Versión | Rama | Estado | Progreso | Fecha Estimada |
|---------|------|--------|----------|----------------|
| v1.0.0 | `main` | ✅ Completado | 100% | Q4 2024 |
| v2.0.0 | `feature/vaadin-ui` | 🔄 En Desarrollo | 30% | Q1 2025 |
| v3.0.0 | `feature/observability` | 📋 Planificado | 0% | Q2 2025 |
| v4.0.0 | `feature/security` | 📋 Planificado | 0% | Q3 2025 |
| v5.0.0 | `feature/production-db` | 📋 Planificado | 0% | Q4 2025 |

---

## 📦 v1.0.0 - MVP Base (main) ✅

**Rama**: `main`  
**Estado**: ✅ Completado  
**Fecha de Release**: Diciembre 2024

### 🎯 Objetivos

Establecer la arquitectura base del sistema con todos los componentes fundamentales funcionando.

### ✅ Características Implementadas

#### Arquitectura y Diseño
- ✅ Arquitectura de microservicios (6 servicios)
- ✅ Arquitectura Hexagonal en cada microservicio
- ✅ Domain-Driven Design con JMolecules
- ✅ Separación de capas: Dominio, Aplicación, Infraestructura

#### Microservicios
- ✅ **servicio-clientes** (Puerto 9080)
  - Gestión de clientes
  - Validación de tarjetas de crédito
  - Gestión de estados
- ✅ **servicio-vuelos** (Puerto 9081)
  - Reservas de vuelos
  - Cancelaciones con compensación
- ✅ **servicio-hoteles** (Puerto 9082)
  - Reservas de hoteles
  - Cancelaciones con compensación
- ✅ **servicio-alquiler-coches** (Puerto 9083)
  - Reservas de vehículos
  - Cancelaciones con compensación
- ✅ **servicio-pagos** (Puerto 9084)
  - Procesamiento de pagos
  - Confirmación de reservas
- ✅ **servicio-reservas** (Puerto 9090)
  - Coordinador BPMN
  - Orquestación de procesos

#### Integración con Camunda
- ✅ Camunda Platform 8.7 (Zeebe, Operate, Tasklist)
- ✅ Workers externos conectados vía gRPC
- ✅ Job Workers con `@JobWorker`
- ✅ Expresiones FEEL en BPMN

#### Procesos BPMN
- ✅ Proceso principal de reserva
- ✅ Subproceso: Gestión de Cliente
- ✅ Subproceso: Proceso de Reserva (paralelo)
- ✅ Subproceso: Proceso de Pago
- ✅ Patrón Saga con compensaciones
- ✅ User Tasks para revisión manual
- ✅ Boundary Events de error
- ✅ Subprocesos de evento (error y mensaje)

#### Persistencia
- ✅ Spring Data JPA
- ✅ H2 Database (en memoria)
- ✅ Repositorios JPA
- ✅ Entidades JPA separadas del dominio

#### APIs REST
- ✅ Controllers REST en cada microservicio
- ✅ DTOs de entrada/salida
- ✅ Validación con Bean Validation
- ✅ Mapeo DTO-Entity con MapStruct
- ✅ Documentación OpenAPI/Swagger

#### Utilidades y Herramientas
- ✅ Lombok para reducir boilerplate
- ✅ Apache Commons Lang (StringUtils)
- ✅ Logs estructurados con iconos
- ✅ Health checks con Actuator

#### DevOps
- ✅ Dockerfiles para cada microservicio
- ✅ Docker Compose para desarrollo
- ✅ Script `start.sh` automatizado
- ✅ Separación de infraestructura Camunda

#### Documentación
- ✅ README.md completo
- ✅ Documentación de arquitectura
- ✅ Diagramas BPMN exportados
- ✅ Casos de uso documentados

### 📝 Notas de la Versión

Esta versión establece las bases sólidas del proyecto con:
- Arquitectura limpia y mantenible
- Integración completa con Camunda 8
- Patrón Saga funcional
- Base de datos en memoria para desarrollo rápido

---

## 🎨 v2.0.0 - Frontend con Vaadin (feature/vaadin-ui) 🔄

**Rama**: `feature/vaadin-ui`  
**Estado**: 🔄 En Desarrollo (30% completado)  
**Fecha Estimada**: Q1 2025

### 🎯 Objetivos

Proporcionar una interfaz de usuario moderna y reactiva para interactuar con el sistema de reservas, reemplazando los formularios básicos de Camunda.

### 🔄 Características en Desarrollo

#### Nuevo Microservicio
- 🔄 **servicio-frontend** (Puerto 9000)
  - Aplicación Vaadin 24.5
  - Integración con Zeebe REST API
  - Comunicación con microservicios backend

#### Pantallas y Vistas
- 🔄 Dashboard principal
  - Vista general de reservas
  - Estadísticas en tiempo real
  - Gráficos de estado de procesos
- 🔄 Formulario de nueva reserva
  - Wizard multi-paso
  - Validación en tiempo real
  - Selección de vuelos, hoteles y coches
- 🔄 Gestión de clientes
  - CRUD de clientes
  - Validación de tarjetas
  - Historial de reservas
- 🔄 Monitoreo de procesos
  - Lista de instancias de proceso
  - Estado actual de cada reserva
  - Filtros y búsqueda

#### Características de UI
- 🔄 Tema personalizado
- 🔄 Responsive design
- 🔄 Notificaciones en tiempo real
- 🔄 Internacionalización (i18n)
  - Español
  - Inglés

#### Integración
- 🔄 Cliente REST para comunicación con microservicios
- 🔄 WebSocket para actualizaciones en tiempo real
- 🔄 Integración con Zeebe REST API
- 🔄 Manejo de sesiones de usuario

### 📋 Características Planificadas

- 📋 Visualización de diagramas BPMN en UI
- 📋 Completar User Tasks desde la interfaz
- 📋 Histórico de compensaciones
- 📋 Reportes y exportación de datos

### ⚠️ Consideraciones

- Substituye formularios de Camunda Forms
- Camunda Tasklist seguirá disponible como fallback
- No se usará localStorage/sessionStorage (limitaciones de Claude.ai)

---

## 📊 v3.0.0 - Observabilidad Completa (feature/observability) 📋

**Rama**: `feature/observability`  
**Estado**: 📋 Planificado  
**Fecha Estimada**: Q2 2025

### 🎯 Objetivos

Implementar observabilidad completa del sistema para producción con métricas, logs centralizados y trazabilidad distribuida.

### 📋 Características Planificadas

#### Métricas
- 📋 Micrometer + Prometheus
  - Métricas de negocio (reservas, pagos, compensaciones)
  - Métricas técnicas (latencia, throughput, errores)
  - Métricas de JVM
  - Métricas de Camunda/Zeebe
- 📋 Dashboards con Grafana
  - Dashboard de negocio
  - Dashboard técnico
  - Dashboard de infraestructura
- 📋 Alertas automatizadas
  - Slack/Email
  - PagerDuty

#### Logs Centralizados
- 📋 ELK Stack (Elasticsearch, Logstash, Kibana)
  - Logs estructurados en JSON
  - Correlación de logs por traceId
  - Búsqueda y filtrado avanzado
  - Dashboards de logs
- 📋 Logs de auditoría
  - Acciones de usuario
  - Cambios de estado
  - Accesos a APIs

#### Distributed Tracing
- 📋 Zipkin o Jaeger
  - Trazas end-to-end
  - Visualización de latencias
  - Detección de cuellos de botella
  - Correlación con logs

#### Health Checks Avanzados
- 📋 Health checks personalizados
- 📋 Readiness probes
- 📋 Liveness probes
- 📋 Startup probes

#### Monitoreo de Camunda
- 📋 Métricas específicas de Zeebe
- 📋 Monitoreo de incidents
- 📋 Alertas de procesos bloqueados
- 📋 SLAs y SLOs

### 🛠️ Stack Tecnológico Adicional

- Prometheus
- Grafana
- Elasticsearch
- Logstash
- Kibana
- Zipkin/Jaeger
- Spring Cloud Sleuth

---

## 🔐 v4.0.0 - Seguridad y Autenticación (feature/security) 📋

**Rama**: `feature/security`  
**Estado**: 📋 Planificado  
**Fecha Estimada**: Q3 2025

### 🎯 Objetivos

Implementar seguridad robusta con autenticación, autorización y encriptación para preparar el sistema para producción.

### 📋 Características Planificadas

#### Autenticación
- 📋 Spring Security
- 📋 OAuth2 / OpenID Connect
- 📋 JWT (JSON Web Tokens)
- 📋 Refresh tokens
- 📋 Single Sign-On (SSO)

#### Autorización
- 📋 Roles y permisos (RBAC)
  - Administrador
  - Agente de reservas
  - Cliente
- 📋 Control de acceso a APIs
- 📋 Control de acceso a User Tasks
- 📋 Auditoría de permisos

#### API Gateway
- 📋 Spring Cloud Gateway
  - Routing centralizado
  - Rate limiting
  - Circuit breaker
  - Authentication/Authorization

#### Seguridad en Comunicaciones
- 📋 HTTPS obligatorio
- 📋 mTLS entre microservicios
- 📋 Encriptación de datos sensibles
- 📋 Secrets management (Vault)

#### Protección
- 📋 CORS configurado
- 📋 CSRF protection
- 📋 Rate limiting por IP
- 📋 DDoS protection

### 🛠️ Stack Tecnológico Adicional

- Spring Security
- Spring Cloud Gateway
- Keycloak (Identity Provider)
- HashiCorp Vault

---

## 🗄️ v5.0.0 - Base de Datos en Producción (feature/production-db) 📋

**Rama**: `feature/production-db`  
**Estado**: 📋 Planificado  
**Fecha Estimada**: Q4 2025

### 🎯 Objetivos

Migrar de H2 a bases de datos adecuadas para producción con soporte de alta disponibilidad.

### 📋 Características Planificadas

#### Bases de Datos
- 📋 PostgreSQL para microservicios transaccionales
  - servicio-clientes
  - servicio-pagos
  - servicio-reservas
- 📋 MongoDB para microservicios de catálogo
  - servicio-vuelos
  - servicio-hoteles
  - servicio-alquiler-coches
- 📋 Redis para caché
  - Caché de clientes
  - Caché de sesiones
  - Rate limiting

#### Migraciones
- 📋 Flyway para PostgreSQL
- 📋 Scripts de migración versionados
- 📋 Rollback automático

#### Alta Disponibilidad
- 📋 Replicación maestro-esclavo
- 📋 Connection pooling (HikariCP)
- 📋 Failover automático
- 📋 Backups automatizados

#### Optimización
- 📋 Índices optimizados
- 📋 Query optimization
- 📋 Paginación eficiente
- 📋 Caché distribuido

### 🛠️ Stack Tecnológico Adicional

- PostgreSQL 16
- MongoDB 7
- Redis 7
- Flyway

---

## 🚀 v6.0.0 - Kubernetes y Cloud Native (feature/k8s) 📋

**Rama**: `feature/k8s`  
**Estado**: 📋 Planificado  
**Fecha Estimada**: Q1 2026

### 🎯 Objetivos

Preparar el sistema para despliegue en Kubernetes con prácticas cloud-native.

### 📋 Características Planificadas

#### Kubernetes
- 📋 Helm charts para cada microservicio
- 📋 Deployments, Services, Ingress
- 📋 ConfigMaps y Secrets
- 📋 HPA (Horizontal Pod Autoscaler)
- 📋 PersistentVolumeClaims

#### Service Mesh
- 📋 Istio o Linkerd
  - Traffic management
  - Service discovery
  - Load balancing
  - mTLS automático

#### CI/CD
- 📋 GitHub Actions
  - Build automático
  - Tests automáticos
  - Deploy automático
- 📋 ArgoCD para GitOps
- 📋 Canary deployments
- 📋 Blue-green deployments

#### Cloud Provider
- 📋 AWS / Azure / GCP
- 📋 Managed Kubernetes (EKS/AKS/GKE)
- 📋 Managed databases
- 📋 Object storage (S3/Blob/GCS)

---

## 📈 Métricas de Éxito

Para cada versión, se medirán las siguientes métricas:

### Técnicas
- ✅ Cobertura de tests > 80%
- ✅ Tiempo de respuesta API < 200ms (p95)
- ✅ Disponibilidad > 99.9%
- ✅ Zero downtime deployments

### Negocio
- ✅ Tasa de éxito de reservas > 95%
- ✅ Tiempo de proceso completo < 5 minutos
- ✅ Tasa de compensaciones < 5%

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas en cualquier fase del roadmap. Ver [CONTRIBUTING.md](CONTRIBUTING.md) para más detalles.

### Cómo Contribuir a una Versión Futura

1. Revisa el roadmap y elige una feature
2. Comenta en el issue correspondiente
3. Crea una rama desde la rama de feature correspondiente
4. Desarrolla y crea un PR hacia la rama de feature

---

## 📝 Notas

- Las fechas son estimadas y pueden cambiar
- Las características pueden moverse entre versiones
- Se priorizarán features según feedback de usuarios
- Cada versión tendrá su propio CHANGELOG

---

**Última actualización**: Diciembre 2024  
**Próxima revisión**: Enero 2025
