SHELL := /bin/bash

.PHONY: build up down logs

build:
	@echo "Verifying Java services can package..."
	@for svc in api-gateway auth-ms business-ms client-ms expense-ms invoice-ms; do \
		if [ -f "$$svc/pom.xml" ]; then \
			echo "Packaging $$svc"; \
			(cd "$$svc" && mvn -B -DskipTests package) || exit 1; \
		else \
			echo "Skipping $$svc (no pom.xml)"; \
		fi; \
	done

	docker compose build

up:
	docker compose up -d --remove-orphans

down:
	docker compose down --volumes

logs:
	docker compose logs -f
