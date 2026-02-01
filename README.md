# Accountia

Accountia is a monorepo SaaS platform for multi-tenant financial management.

## Project Structure

- **Frontend**: Next.js 14, Bun, TailwindCSS, Shadcn/ui ([frontend/](frontend/))
- **API Gateway**: Spring Cloud Gateway (Java 21, Maven) ([api-gateway/](api-gateway/))
- **Microservices**:
	- **auth-ms**: Authentication (Spring Boot, Java 21) ([auth-ms/](auth-ms/))
	- **business-ms**: Business logic (Spring Boot, Java 21) ([business-ms/](business-ms/))
	- **client-ms**: Client management (Spring Boot, Java 21) ([client-ms/](client-ms/))
	- **expense-ms**: Expense tracking (Spring Boot, Java 21) ([expense-ms/](expense-ms/))
	- **invoice-ms**: Invoicing (Spring Boot, Java 21) ([invoice-ms/](invoice-ms/))
- **Reporting**: FastAPI (Python 3.11, PostgreSQL) ([reporting-ms/](reporting-ms/))
- **Orchestration**: Docker Compose, Makefile

## Getting Started

See `docker-compose.yml` and `Makefile` for local development and orchestration instructions.
