.PHONY: accounts accounts-build accounts-db-up accounts-db-down accounts-api-run accounts-restart \
        cards cards-build cards-db-up cards-db-down cards-api-run cards-restart \
        loans loans-build loans-db-up loans-db-down loans-api loans-restart \
        message-build message-up message-restart message-down \
        eureka-server-build eureka-server-up eureka-server-down dbs-up dbs-down \
        watch watch-accounts watch-cards watch-loans watch-gateway watch-message \
        gateway-up gateway-down gateway-restart \
        accounts-image-build cards-image-build loans-image-build message-image-build \
        gateway-server-image-build config-server-image-build eureka-server-image-build \
        accounts-image-push cards-image-push loans-image-push message-image-push \
        gateway-server-image-push config-server-image-push eureka-server-image-push \
        images-build images-push images-build-push images-pull \
        accounts-image-up cards-image-up loans-image-up \
        services-image-up all-image-up all-compose-up all-compose-down \
        infra infra-tfvars infra-init infra-fmt infra-validate \
        infra-plan infra-apply infra-output infra-down

INFRA_DIR := infra
TOFU := tofu
DOCKERHUB_USER := baicham
IMAGE_TAG := latest
COMPOSE_IMAGE := docker compose -f compose.image.yml
COMPOSE_ALL := docker compose -f docker-compose.all.yml
COMPOSE_DBS := docker compose -f docker-compose.dbs.yml

# ==============================================================================
# Accounts Service
# ==============================================================================
accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up:
	$(COMPOSE_DBS) up accounts-db -d

accounts-db-down:
	$(COMPOSE_DBS) down accounts-db -v

accounts-api-run:
	docker compose up accounts-api -d --build --no-deps

accounts:
	docker compose up accounts-db accounts-api -d

accounts-restart:
	docker compose up accounts-api -d --build --force-recreate --no-deps

accounts-down:
	docker compose down accounts-db accounts-api -v

# ==============================================================================
# Cards Service
# ==============================================================================
cards-build:
	cd cards && ./gradlew clean build

cards-db-up:
	$(COMPOSE_DBS) up cards-db -d

cards-db-down:
	$(COMPOSE_DBS) down cards-db -v

cards-api-run:
	docker compose up cards-api -d --build --no-deps

cards:
	docker compose up cards-db cards-api -d

cards-restart:
	docker compose up cards-api -d --build --force-recreate --no-deps

cards-down:
	docker compose down cards-db cards-api -v


# ==============================================================================
# Loans Service
# ==============================================================================
loans-build:
	cd loans && ./gradlew clean build

loans-db-up:
	$(COMPOSE_DBS) up loans-db -d

loans-db-down:
	$(COMPOSE_DBS) down loans-db -v

loans-api:
	docker compose up loans-api -d --build --no-deps

loans:
	docker compose up loans-db loans-api -d

loans-restart:
	docker compose up loans-api -d --build --force-recreate --no-deps

loans-down:
	docker compose down loans-db loans-api -v

# ==============================================================================
# Message Service
# ==============================================================================
message-build:
	cd message && ./gradlew clean build

message-up:
	docker compose up message -d --build

message-restart:
	docker compose up message -d --build --force-recreate --no-deps

message-down:
	docker compose down message -v

# ==============================================================================
# Eureka Server
# ==============================================================================
eureka-server-build:
	cd eureka-server && ./gradlew clean build

eureka-server-up:
	docker compose up eureka-server -d

eureka-server-down:
	docker compose stop eureka-server

# ==============================================================================
# Config Server
# ==============================================================================
rabbit-mq-up:
	docker compose -f ../config-server/compose.yml up rabbit-mq -d

rabbit-mq-down:
	docker compose -f ../config-server/compose.yml down rabbit-mq -v

config-server-up:
	docker compose -f ../config-server/compose.yml up config-server -d

config-server-down:
	docker compose -f ../config-server/compose.yml down config-server -v

# ==============================================================================
# Global / Teardown
# ==============================================================================
dbs-up:
	$(COMPOSE_DBS) up -d

dbs-down:
	$(COMPOSE_DBS) down -v
	@echo "all dbs are down"

api-up: accounts-api-run cards-api-run loans-api
	@echo "restart apis"

config-eureka:
	docker compose up config-server eureka-server -d

config-eureka-down:
	docker compose down config-server eureka-server -v

services-up:
	docker compose up accounts-api cards-api loans-api -d --build

services-down:
	docker compose down accounts-api cards-api loans-api


# ==============================================================================
# All Services
# ==============================================================================
all-up:
	docker compose up -d

all-down:
	docker compose down -v

gateway-up:
	docker compose up gateway-server -d --build

gateway-restart:
	docker compose up gateway-server -d --build --force-recreate --no-deps

gateway-down:
	docker compose down gateway-server -v

# ==============================================================================
# Docker Hub images (run without a local Dockerfile build)
# ==============================================================================
accounts-image-build:
	docker build -t $(DOCKERHUB_USER)/accounts-api:$(IMAGE_TAG) ./accounts

cards-image-build:
	docker build -t $(DOCKERHUB_USER)/cards-api:$(IMAGE_TAG) ./cards

loans-image-build:
	docker build -t $(DOCKERHUB_USER)/loans-api:$(IMAGE_TAG) ./loans

message-image-build:
	docker build -t $(DOCKERHUB_USER)/message:$(IMAGE_TAG) ./message

gateway-server-image-build:
	docker build -t $(DOCKERHUB_USER)/gateway-server:$(IMAGE_TAG) ./gateway-server

config-server-image-build:
	docker build -t $(DOCKERHUB_USER)/config-server:$(IMAGE_TAG) ./config-server

eureka-server-image-build:
	docker build -t $(DOCKERHUB_USER)/eureka-server:$(IMAGE_TAG) ./eureka-server

images-build: accounts-image-build cards-image-build loans-image-build message-image-build \
	gateway-server-image-build config-server-image-build eureka-server-image-build

accounts-image-push:
	docker push $(DOCKERHUB_USER)/accounts-api:$(IMAGE_TAG)

cards-image-push:
	docker push $(DOCKERHUB_USER)/cards-api:$(IMAGE_TAG)

loans-image-push:
	docker push $(DOCKERHUB_USER)/loans-api:$(IMAGE_TAG)

message-image-push:
	docker push $(DOCKERHUB_USER)/message:$(IMAGE_TAG)

gateway-server-image-push:
	docker push $(DOCKERHUB_USER)/gateway-server:$(IMAGE_TAG)

config-server-image-push:
	docker push $(DOCKERHUB_USER)/config-server:$(IMAGE_TAG)

eureka-server-image-push:
	docker push $(DOCKERHUB_USER)/eureka-server:$(IMAGE_TAG)

images-push: accounts-image-push cards-image-push loans-image-push message-image-push \
	gateway-server-image-push config-server-image-push eureka-server-image-push

images-build-push: images-build images-push

images-pull:
	$(COMPOSE_IMAGE) pull accounts-api cards-api loans-api

accounts-image-up:
	$(COMPOSE_IMAGE) up accounts-db accounts-api -d --no-build

cards-image-up:
	$(COMPOSE_IMAGE) up cards-db cards-api -d --no-build

loans-image-up:
	$(COMPOSE_IMAGE) up loans-db loans-api -d --no-build

services-image-up:
	$(COMPOSE_IMAGE) up accounts-api cards-api loans-api -d --no-build

all-image-up:
	$(COMPOSE_IMAGE) up -d --no-build

all-compose-up:
	$(COMPOSE_ALL) up -d

all-compose-down:
	$(COMPOSE_ALL) down -v

# ==============================================================================
# Live Sync & Watch
# ==============================================================================
watch:
	docker compose watch

watch-all-up: all-up watch
	@echo "start all and watch"

watch-accounts:
	docker compose watch accounts-api

watch-cards:
	docker compose watch cards-api

watch-loans:
	docker compose watch loans-api

watch-gateway:
	docker compose watch gateway-server

watch-message:
	docker compose watch message

# ==============================================================================
# OpenTofu (Keycloak realm, clients, users)
# ==============================================================================
# Requires OpenTofu (tofu) on PATH and a reachable Keycloak at keycloak_url.
# Copy infra/terraform.tfvars.example to infra/terraform.tfvars on first use.

keycloak-up:
	docker compose -f docker-compose.keycloak.yml up -d

keycloak-down:
	docker compose -f docker-compose.keycloak.yml down -v

infra-tfvars:
	@if [ ! -f $(INFRA_DIR)/terraform.tfvars ]; then \
		cp $(INFRA_DIR)/terraform.tfvars.example $(INFRA_DIR)/terraform.tfvars; \
		echo "Created $(INFRA_DIR)/terraform.tfvars from example. Edit secrets before applying."; \
	fi

infra-init: infra-tfvars
	cd $(INFRA_DIR) && $(TOFU) init -upgrade

infra-fmt:
	cd $(INFRA_DIR) && $(TOFU) fmt -recursive

infra-validate: infra-init
	cd $(INFRA_DIR) && $(TOFU) validate

infra-plan: infra-init
	cd $(INFRA_DIR) && $(TOFU) plan

infra-apply: infra-init
	cd $(INFRA_DIR) && $(TOFU) apply -auto-approve

infra: infra-apply

infra-output:
	cd $(INFRA_DIR) && $(TOFU) output

infra-down:
	cd $(INFRA_DIR) && $(TOFU) destroy -auto-approve

rabbit-mq-up:
	docker compose rabbit up rabbitmq -d

rabbit-mq-down:
	docker compose rabbit down rabbitmq -v

apis-up:
	docker compose up accounts-api cards-api loans-api -d --build

apis-down:
	docker compose down accounts-api cards-api loans-api -v