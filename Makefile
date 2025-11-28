# =======================================================
#  FILEPASSER MAKEFILE (Router + Uploader + Frontend)
# =======================================================

DOCKER_COMPOSE = docker compose

POSTGRES_CONTAINER = filepasser-postgres-1
MINIO_CONTAINER = filepasser-minio-1
KAFKA_CONTAINER = filepasser-kafka-1

MINIO_ALIAS = local
MINIO_BUCKET = router-inbound

.DEFAULT_GOAL := help

# --------------------------------------------
# HELP
# --------------------------------------------
help:
	@echo ""
	@echo " FilePasser - Developer Commands"
	@echo "--------------------------------------------"
	@echo " make up                 - Start ALL services (backend + frontend + DB + Kafka)"
	@echo " make down               - Stop all services"
	@echo " make rebuild            - Rebuild shared events + router + uploader (Docker)"
	@echo " make logs               - Tail all logs"
	@echo " make clean              - Remove containers + volumes"
	@echo ""
	@echo " Backend:"
	@echo " make router-build       - Build router backend"
	@echo " make uploader-build     - Build uploader backend"
	@echo " make shared-events-build- Build shared events jar"
	@echo ""
	@echo " (set RUN_TESTS=1 before any build target above to include tests)"
	@echo ""
	@echo " Frontend:"
	@echo " make frontend-dev       - Start Vite dev server"
	@echo " make frontend-build     - Build Vite frontend"
	@echo ""
	@echo " Database:"
	@echo " make db-router-reset    - Reset Router DB"
	@echo " make db-uploader-reset  - Reset Uploader DB"
	@echo ""
	@echo " MinIO:"
	@echo " make minio-setup        - Configure mc alias"
	@echo " make minio-clean        - Clear bucket"
	@echo " make minio-reset        - Recreate bucket"
	@echo ""

# --------------------------------------------
# DOCKER LIFECYCLE
# --------------------------------------------
up:
	$(DOCKER_COMPOSE) up --build -d

down:
	$(DOCKER_COMPOSE) down

rebuild:
	$(DOCKER_COMPOSE) down
	@if [ "$${RUN_TESTS:-0}" = "1" ]; then \
		echo "Running shared-kafka-events build with tests"; \
		mvn -f common/shared-kafka-events/pom.xml clean install; \
	else \
		mvn -f common/shared-kafka-events/pom.xml clean install -DskipTests; \
	fi
	@if [ "$${RUN_TESTS:-0}" = "1" ]; then \
		echo "Running router build with tests"; \
		mvn -f router/pom.xml clean install; \
	else \
		mvn -f router/pom.xml clean install -DskipTests; \
	fi
	@if [ "$${RUN_TESTS:-0}" = "1" ]; then \
		echo "Running uploader build with tests"; \
		mvn -f uploader/pom.xml clean install; \
	else \
		mvn -f uploader/pom.xml clean install -DskipTests; \
	fi
	$(DOCKER_COMPOSE) up --build -d

logs:
	$(DOCKER_COMPOSE) logs -f

clean:
	$(DOCKER_COMPOSE) down -v --remove-orphans
	docker system prune -f

# --------------------------------------------
# BACKEND BUILD
# --------------------------------------------
router-build:
	@if [ "$${RUN_TESTS:-0}" = "1" ]; then \
		echo "Running router build with tests"; \
		mvn -f router/pom.xml clean package; \
	else \
		mvn -f router/pom.xml clean package -DskipTests; \
	fi

uploader-build:
	@if [ "$${RUN_TESTS:-0}" = "1" ]; then \
		echo "Running uploader build with tests"; \
		mvn -f uploader/pom.xml clean package; \
	else \
		mvn -f uploader/pom.xml clean package -DskipTests; \
	fi

shared-events-build:
	@if [ "$${RUN_TESTS:-0}" = "1" ]; then \
		echo "Running shared-kafka-events build with tests"; \
		mvn -f common/shared-kafka-events/pom.xml clean package; \
	else \
		mvn -f common/shared-kafka-events/pom.xml clean package -DskipTests; \
	fi

# --------------------------------------------
# FRONTEND (filepasser-frontend)
# --------------------------------------------
frontend-dev:
	cd filepasser-frontend && npm install && npm run dev

frontend-build:
	cd filepasser-frontend && npm install && npm run build

# --------------------------------------------
# DATABASE
# --------------------------------------------
db-router-reset:
	@echo "Resetting routerdb..."
	$(DOCKER_COMPOSE) exec -T postgres psql -U postgres -d routerdb -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

db-uploader-reset:
	@echo "Resetting uploaderdb..."
	$(DOCKER_COMPOSE) exec -T postgres psql -U postgres -d uploaderdb -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# --------------------------------------------
# MINIO
# --------------------------------------------
minio-setup:
	$(DOCKER_COMPOSE) exec -T minio mc alias set $(MINIO_ALIAS) http://localhost:9000 minio minio123

minio-clean:
	$(DOCKER_COMPOSE) exec -T minio mc rm --recursive --force $(MINIO_ALIAS)/$(MINIO_BUCKET) || true

minio-reset:
	$(DOCKER_COMPOSE) exec -T minio mc rm --recursive --force $(MINIO_ALIAS)/$(MINIO_BUCKET) || true
	$(DOCKER_COMPOSE) exec -T minio mc rb --force $(MINIO_ALIAS)/$(MINIO_BUCKET) || true
	$(DOCKER_COMPOSE) exec -T minio mc mb $(MINIO_ALIAS)/$(MINIO_BUCKET)
