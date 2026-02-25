.PHONY: help up down restart logs ps clean test test-unit test-coverage         build run compile db-connect db-reset health format

# HELP
help:
	@echo ""
	@echo "  E-Commerce Monolith — Command Reference"
	@echo ""
	@echo "  INFRASTRUCTURE"
	@echo "  ─────────────────────────────────────────"
	@echo "  make up           Start PostgreSQL + pgAdmin"
	@echo "  make down         Stop all containers"
	@echo "  make restart      Restart all containers"
	@echo "  make logs         Follow container logs"
	@echo "  make ps           Show running containers"
	@echo "  make clean        Remove containers + volumes (DESTROYS DATA)"
	@echo ""
	@echo "  APPLICATION"
	@echo "  ─────────────────────────────────────────"
	@echo "  make run          Start the application"
	@echo "  make build        Compile + run tests + package JAR"
	@echo "  make compile      Compile only (no tests)"
	@echo "  make health       Check application health"
	@echo ""
	@echo "  TESTS"
	@echo "  ─────────────────────────────────────────"
	@echo "  make test         Run all tests"
	@echo "  make test-unit    Run unit tests only (no Docker)"
	@echo "  make test-coverage Open coverage report in browser"
	@echo ""
	@echo "  DATABASE"
	@echo "  ─────────────────────────────────────────"
	@echo "  make db-connect   Open psql in the container"
	@echo "  make db-reset     Destroy and recreate DB (DESTROYS DATA)"
	@echo ""

# INFRASTRUCTURE
up:
	docker-compose up -d
	@echo "PostgreSQL on localhost:5433 | pgAdmin on http://localhost:5050"

down:
	docker-compose down

restart:
	docker-compose down && docker-compose up -d

logs:
	docker-compose logs -f postgres

ps:
	docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

clean:
	@echo "This will DESTROY all data. Are you sure? [y/N] " && read ans && [ $${ans:-N} = y ]
	docker-compose down -v
	@echo "Containers and volumes removed"

# APPLICATION
run:
	./mvnw spring-boot:run

build:
	./mvnw clean package

compile:
	./mvnw clean compile -DskipTests

health:
	@curl -s http://localhost:8080/actuator/health | python3 -m json.tool

# TESTS
test:
	./mvnw clean verify

test-unit:
	./mvnw test -Dtest="**/*Test" -DfailIfNoTests=false

test-coverage:
	./mvnw clean test
	@echo "Opening coverage report..."
	@xdg-open target/site/jacoco/index.html 2>/dev/null || open target/site/jacoco/index.html 2>/dev/null || echo "Report at: target/site/jacoco/index.html"

# DATABASE
db-connect:
	docker exec -it ecommerce-postgres psql -U ecommerce -d ecommerce_db

db-reset:
	@echo "This will DESTROY all data. Are you sure? [y/N] " && read ans && [ $${ans:-N} = y ]
	docker-compose down -v
	docker-compose up -d
	@echo "Database recreated — Flyway will migrate on next app start"
