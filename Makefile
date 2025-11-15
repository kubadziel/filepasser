# =======================================================
#  FILEPASSER MAKEFILE (Router + Uploader + Shared DB)
# =======================================================

# Variables
DOCKER_COMPOSE = docker compose

POSTGRES_CONTAINER = filepasser-postgres-1
MINIO_CONTAINER = filepasser-minio-1
KAFKA_CONTAINER = filepasser-kafka-1

MINIO_ALIAS = local
MINIO_BUCKET = router-inbound

# Default target
.DEFAULT_GOAL := help


# --------------------------------------------
# Help
# --------------------------------------------
help:
	@echo ""
	@echo " FilePasser - Developer Commands"
	@echo "--------------------------------------------"
	@echo " make up                 - Start all containers (build if needed)"
	@echo " make down               - Stop all containers"
	@echo " make rebuild            - Rebuild router + uploader and restart"
	@echo " make logs               - Tail logs from all services"
	@echo " make clean              - Remove containers + volumes"
	@echo ""
	@echo " Backend:"
	@echo " make router-build       - Build router Spring Boot JAR"
	@echo " make uploader-build     - Build uploader Spring Boot JAR"
	@echo ""
	@echo " Database:"
	@echo " make db-router-reset    - Reset Router database schema"
	@echo " make db-uploader-reset  - Reset Uploader database schema"
	@echo ""
	@echo " MinIO:"
	@echo " make minio-setup        - Configure MinIO client alias"
	@echo " make minio-clean        - Remove all objects from bucket"
	@echo " make minio-reset        - Recreate bucket"
	@echo ""


# --------------------------------------------
# Docker lifecycle
# --------------------------------------------
up:
	$(DOCKER_COMPOSE) up --build -d

down:
	$(DOCKER_COMPOSE) down

rebuild:
	$(DOCKER_COMPOSE) down
	mvn -f router/pom.xml clean install -DskipTests
	mvn -f uploader/pom.xml clean install -DskipTests
	$(DOCKER_COMPOSE) up --build -d

logs:
	$(DOCKER_COMPOSE) logs -f

clean:
	$(DOCKER_COMPOSE) down -v --remove-orphans
	docker system prune -f


# --------------------------------------------
# Build
# --------------------------------------------
router-build:
	mvn -f router/pom.xml clean package -DskipTests

uploader-build:
	mvn -f uploader/pom.xml clean package -DskipTests


# --------------------------------------------
# Database maintenance
# --------------------------------------------
db-router-reset:
	@echo "Resetting routerdb schema..."
	$(DOCKER_COMPOSE) exec -T postgres psql -U postgres -d routerdb -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	@echo "Router DB reset complete."

db-uploader-reset:
	@echo "Resetting uploaderdb schema..."
	$(DOCKER_COMPOSE) exec -T postgres psql -U postgres -d uploaderdb -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	@echo "Uploader DB reset complete."


# --------------------------------------------
# MinIO maintenance (mc CLI required)
# --------------------------------------------
minio-setup:
	@echo "Configuring MinIO alias..."
	$(DOCKER_COMPOSE) exec -T minio mc alias set $(MINIO_ALIAS) http://localhost:9000 minio minio123
	@echo "MinIO alias configured."

minio-clean:
	@echo "Clearing bucket $(MINIO_BUCKET)..."
	$(DOCKER_COMPOSE) exec -T minio mc rm --recursive --force $(MINIO_ALIAS)/$(MINIO_BUCKET) || true
	@echo "Bucket cleaned."

minio-reset:
	@echo "Recreating bucket $(MINIO_BUCKET)..."
	$(DOCKER_COMPOSE) exec -T minio mc rm --recursive --force $(MINIO_ALIAS)/$(MINIO_BUCKET) || true
	$(DOCKER_COMPOSE) exec -T minio mc rb --force $(MINIO_ALIAS)/$(MINIO_BUCKET) || true
	$(DOCKER_COMPOSE) exec -T minio mc mb $(MINIO_ALIAS)/$(MINIO_BUCKET)
	@echo "Bucket recreated."
