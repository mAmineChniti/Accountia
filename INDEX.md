# Accountia Project Index

This index summarizes the repository layout, service entrypoints, and deployment assets for quick navigation.

## Top-Level Layout

- `api-gateway/` Spring Cloud Gateway (Java 21)
- `auth-ms/` Authentication microservice (Spring Boot)
- `business-ms/` Business management microservice (Spring Boot)
- `client-ms/` Client management microservice (Spring Boot)
- `expense-ms/` Expense tracking microservice (Spring Boot)
- `invoice-ms/` Invoice management microservice (Spring Boot)
- `reporting-ms/` Reporting service (FastAPI / Python 3.11)
- `eureka-server/` Service discovery (Spring Cloud Netflix Eureka)
- `keycloak/` Keycloak realm configuration
- `k8s/` Kustomize manifests
- `docker-compose.yml` Local dev orchestration
- `Makefile` Build/run shortcuts
- `Accountia-Global-Architecture.png` Architecture diagram
- `README.md` Main docs

## Services And Entrypoints

### API Gateway
- Path: `api-gateway/`
- Language/Framework: Java 21, Spring Cloud Gateway
- Main class: `api-gateway/src/main/java/com/example/api_gateway/ApiGatewayApplication.java`
- Config: `api-gateway/src/main/resources/application.yml`, `api-gateway/src/main/resources/application-ci.yml`
- Container: `api-gateway/Dockerfile`
- Local port (compose): `8080`

### Auth Service
- Path: `auth-ms/`
- Language/Framework: Java 21, Spring Boot
- Main class: `auth-ms/src/main/java/com/accountia/auth_ms/AuthApplication.java`
- Config: `auth-ms/src/main/resources/application.yml`, `auth-ms/src/main/resources/application-ci.yml`
- Container: `auth-ms/Dockerfile`
- Local port (compose): `8081`

### Business Service
- Path: `business-ms/`
- Language/Framework: Java 21, Spring Boot
- Main class: `business-ms/src/main/java/com/accountia/business_ms/BusinessApplication.java`
- Config: `business-ms/src/main/resources/application.yml`, `business-ms/src/main/resources/application-ci.yml`
- Container: `business-ms/Dockerfile`
- Local port (compose): `8082`

### Invoice Service
- Path: `invoice-ms/`
- Language/Framework: Java 21, Spring Boot
- Main class: `invoice-ms/src/main/java/com/accountia/invoice/InvoiceApplication.java`
- Config: `invoice-ms/src/main/resources/application.yml`, `invoice-ms/src/main/resources/application-ci.yml`
- Container: `invoice-ms/Dockerfile`
- Local port (compose): `8083`

### Expense Service
- Path: `expense-ms/`
- Language/Framework: Java 21, Spring Boot
- Main class: `expense-ms/src/main/java/com/accountia/expense_ms/ExpenseApplication.java`
- Config: `expense-ms/src/main/resources/application.yml`, `expense-ms/src/main/resources/application-ci.yml`
- Container: `expense-ms/Dockerfile`
- Local port (compose): `8084`

### Client Service
- Path: `client-ms/`
- Language/Framework: Java 21, Spring Boot
- Main class: `client-ms/src/main/java/com/accountia/client_ms/ClientApplication.java`
- Config: `client-ms/src/main/resources/application.yml`, `client-ms/src/main/resources/application-ci.yml`
- Container: `client-ms/Dockerfile`
- Local port (compose): `8085`

### Reporting Service
- Path: `reporting-ms/`
- Language/Framework: Python 3.11, FastAPI
- Main module: `reporting-ms/main.py`
- Routers: `reporting-ms/routers/report.py`
- Data/config: `reporting-ms/database.py`, `reporting-ms/models.py`, `reporting-ms/settings.py`
- Container: `reporting-ms/Dockerfile`
- Local port (compose): `8086` mapped to container `8000`

### Eureka Server
- Path: `eureka-server/`
- Language/Framework: Java 21, Spring Boot (Eureka)
- Main class: `eureka-server/src/main/java/com/accountia/eurekaserver/EurekaServerApplication.java`
- Config: `eureka-server/src/main/resources/application.yml`
- Container: `eureka-server/Dockerfile`
- Local port (compose): `8761`

## API Surface (Gateway Routes)

Defined in `README.md`:
- `http://localhost:8080/api/auth/**`
- `http://localhost:8080/api/business/**`
- `http://localhost:8080/api/client/**`
- `http://localhost:8080/api/invoices/**`
- `http://localhost:8080/api/expense/**`
- `http://localhost:8080/api/reporting/**`

## Local Runtime (Docker Compose)

Orchestration: `docker-compose.yml`

Infra containers:
- MySQL 8.0
- PostgreSQL 15
- Redis 7
- RabbitMQ 3.12 (management)
- Keycloak 24.0

Service containers:
- `eureka-server`
- `api-gateway`
- `auth-ms`
- `business-ms`
- `invoice-ms`
- `expense-ms`
- `client-ms`
- `reporting-ms`

## Kubernetes (Kustomize)

Kustomize root: `k8s/kustomization.yaml`

Base:
- `k8s/base/namespace.yaml`
- `k8s/base/configmap.yaml`
- `k8s/base/secrets.yaml`

Infrastructure:
- `k8s/infrastructure/mysql.yaml`
- `k8s/infrastructure/postgres.yaml`
- `k8s/infrastructure/redis.yaml`
- `k8s/infrastructure/rabbitmq.yaml`
- `k8s/infrastructure/keycloak.yaml`
- `k8s/infrastructure/eureka-server.yaml`

Services:
- `k8s/services/api-gateway.yaml`
- `k8s/services/auth-ms.yaml`
- `k8s/services/microservices.yaml`

## Key Docs And Assets

- `README.md` Overall docs and dev commands
- `auth-ms/README.md` Auth service notes
- `Accountia-Global-Architecture.png` Architecture diagram
