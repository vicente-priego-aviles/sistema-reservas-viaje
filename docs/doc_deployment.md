### Prerequisitos

- Cluster Kubernetes 1.27+
- kubectl configurado
- Helm 3.x
- Ingress Controller (nginx)

### Namespace

```bash
kubectl create namespace reservas-viaje
kubectl config set-context --current --namespace=reservas-viaje
```

### Deploy Camunda

```bash
helm repo add camunda https://helm.camunda.io
helm repo update

helm install camunda camunda/camunda-platform \
  --namespace reservas-viaje \
  --set global.ingress.enabled=true \
  --set global.ingress.host=camunda.ejemplo.com
```

### Deploy Microservicios

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: servicio-clientes
spec:
  replicas: 3
  selector:
    matchLabels:
      app: servicio-clientes
  template:
    metadata:
      labels:
        app: servicio-clientes
    spec:
      containers:
      - name: servicio-clientes
        image: reservas/servicio-clientes:1.0.0
        ports:
        - containerPort: 9080
        env:
        - name: ZEEBE_ADDRESS
          value: "camunda-zeebe-gateway:26500"
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 9080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 9080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: servicio-clientes
spec:
  selector:
    app: servicio-clientes
  ports# 🐳 Guía de Deployment

Estrategias y guías para despliegue del sistema en diferentes entornos.

---

## 📋 Opciones de Deployment

| Opción | Complejidad | Escalabilidad | Recomendado Para |
|--------|-------------|---------------|------------------|
| Docker Compose | Baja | Baja | Desarrollo, Demos |
| Docker Swarm | Media | Media | Producción pequeña |
| Kubernetes | Alta | Alta | Producción enterprise |
| Cloud Managed | Media | Alta | Cloud-native |

---

## 🐳 Docker Compose (Desarrollo/Demo)

### Levantar Sistema Completo

```bash
# Opción 1: Script automatizado
./start.sh

# Opción 2: Manual
docker-compose -f docker-compose-camunda.yml up -d
sleep 60
docker-compose up -d
```

### Comandos Útiles

```bash
# Ver estado
docker-compose ps

# Logs
docker-compose logs -f

# Logs de un servicio
docker-compose logs -f servicio-clientes

# Reiniciar servicio
docker-compose restart servicio-clientes

# Detener todo
docker-compose down

# Detener y limpiar volúmenes
docker-compose down -v
```

---

## ☸️ Kubernetes (Producción)

### Prerequis