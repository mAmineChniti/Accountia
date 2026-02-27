# Accountia Config Server

Spring Cloud Config Server for centralized configuration management of Accountia microservices.

## Features

- **Local File System Backend**: Uses native file system for configuration storage
- **Environment Variable Support**: Secure configuration via environment variables
- **Multi-Environment Support**: Separate configs for dev, test, and production
- **Service-Specific Configs**: Individual configurations for each microservice
- **Health Monitoring**: Actuator endpoints for monitoring and health checks

## Configuration Structure

```
src/main/resources/config/
├── accountia.yml              # Common configuration for all services
├── accountia-dev.yml          # Development environment overrides
├── accountia-prod.yml         # Production environment overrides
├── accountia-test.yml         # Test environment overrides
├── auth-ms.yml                # Auth service specific config
├── business-ms.yml            # Business service specific config
├── client-ms.yml              # Client service specific config
├── expense-ms.yml             # Expense service specific config
├── invoice-ms.yml             # Invoice service specific config
└── reporting-ms.yml           # Reporting service specific config
```

## Environment Variables

The config server supports the following secure environment variables:

### Database Configuration
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Security
- `SECURITY_JWT_SECRET`

### Message Queue
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`

### Cache
- `REDIS_HOST`
- `REDIS_PORT`

### Service Discovery
- `EUREKA_SERVICE_URL`

### Admin User Seeding (Auth Service)
- `ADMIN_SEED_EMAIL`
- `ADMIN_SEED_USERNAME`
- `ADMIN_SEED_TENANT_ID`
- `ADMIN_SEED_PASSWORD`

## API Endpoints

- **Config Server**: http://localhost:8888
- **Health Check**: http://localhost:8888/actuator/health
- **Environment Info**: http://localhost:8888/actuator/env
- **Service Config**: http://localhost:8888/{application}/{profile}

## Usage Examples

### Get configuration for auth-ms in dev environment
```bash
curl http://localhost:8888/auth-ms/dev
```

### Get configuration for business-ms in production
```bash
curl http://localhost:8888/business-ms/prod
```

## Security Considerations

- Sensitive data like passwords and secrets should be provided via environment variables
- The config server uses native file system backend for local development
- For production, consider using Vault or encrypted property sources
- Access to config server should be restricted within the network

## Development

To run the config server locally:

```bash
cd config-server
./mvnw spring-boot:run
```

The server will start on port 8888 and serve configurations from the `src/main/resources/config/` directory.
