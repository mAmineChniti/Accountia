SHELL := /bin/bash

.PHONY: build up down logs

build:
	docker compose build

up:
	docker compose up -d --remove-orphans

down:
	docker compose down --volumes

logs:
	docker compose logs -f
