.PHONY: accounts accounts-build accounts-db-up accounts-db-down accounts-api-run accounts-restart \
        cards cards-build cards-db-up cards-db-down cards-api-run cards-restart \
        loans loans-build loans-db-up loans-db-down loans-api loans-restart \
        message-build message-up message-restart message-down \
        eureka-server-build eureka-server-up eureka-server-down dbs-up dbs-down \
        kafka-up kafka-down \
        watch watch-accounts watch-cards watch-loans watch-gateway watch-message \
        gateway-up gateway-down gateway-restart \
        accounts-image-build cards-image-build loans-image-build message-image-build \
        gateway-server-image-build config-server-image-build eureka-server-image-build \
        accounts-image-push cards-image-push loans-image-push message-image-push \
        gateway-server-image-push config-server-image-push eureka-server-image-push \
        images-build images-push images-build-push images-pull \
        require-tag \
        accounts-image-build-tag cards-image-build-tag loans-image-build-tag message-image-build-tag \
        config-server-image-build-tag eureka-server-image-build-tag gateway-server-image-build-tag \
        accounts-image-push-tag cards-image-push-tag loans-image-push-tag message-image-push-tag \
        config-server-image-push-tag eureka-server-image-push-tag gateway-server-image-push-tag \
        images-build-tag images-push-tag images-build-push-tag \
        accounts-image-up cards-image-up loans-image-up message-image-up \
        services-image-up all-image-up all-compose-up all-compose-down \
        infra infra-tfvars infra-init infra-fmt infra-validate \
        infra-plan infra-apply infra-output infra-down \
        k8s-keycloak k8s-configmap k8s-calico k8s-kafka \
        k8s-accounts k8s-cards k8s-loans k8s-message \
        k8s-config-server k8s-eureka-server k8s-gateway-server \
        k8s-services k8s-platform k8s-up \
        helm-deps helm-lint helm-template helm-up helm-down

INFRA_DIR := infra
TOFU := tofu
DOCKERHUB_USER := baicham
IMAGE_TAG := latest
COMPOSE := docker compose -f docker/compose.yml --project-directory .
COMPOSE_IMAGE := docker compose -f docker/compose.image.yml --project-directory .
COMPOSE_ALL := docker compose -f docker/compose.all.yml --project-directory .
COMPOSE_KEYCLOAK := docker compose -f docker/compose.keycloak.yml --project-directory .

# ==============================================================================
# Accounts Service
# ==============================================================================
accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up:
	$(COMPOSE) up accounts-db -d

accounts-db-down:
	$(COMPOSE) stop accounts-db

accounts-api-run:
	$(COMPOSE) up accounts-api -d --build --no-deps

accounts:
	$(COMPOSE) up accounts-db accounts-api -d

accounts-restart:
	$(COMPOSE) up accounts-api -d --build --force-recreate --no-deps

accounts-down:
	$(COMPOSE) down accounts-db accounts-api -v

# ==============================================================================
# Cards Service
# ==============================================================================
cards-build:
	cd cards && ./gradlew clean build

cards-db-up:
	$(COMPOSE) up cards-db -d

cards-db-down:
	$(COMPOSE) stop cards-db

cards-api-run:
	$(COMPOSE) up cards-api -d --build --no-deps

cards:
	$(COMPOSE) up cards-db cards-api -d

cards-restart:
	$(COMPOSE) up cards-api -d --build --force-recreate --no-deps

cards-down:
	$(COMPOSE) down cards-db cards-api -v


# ==============================================================================
# Loans Service
# ==============================================================================
loans-build:
	cd loans && ./gradlew clean build

loans-db-up:
	$(COMPOSE) up loans-db -d

loans-db-down:
	$(COMPOSE) stop loans-db

loans-api:
	$(COMPOSE) up loans-api -d --build --no-deps

loans:
	$(COMPOSE) up loans-db loans-api -d

loans-restart:
	$(COMPOSE) up loans-api -d --build --force-recreate --no-deps

loans-down:
	$(COMPOSE) down loans-db loans-api -v

# ==============================================================================
# Message Service
# ==============================================================================
message-build:
	cd message && ./gradlew clean build

message-up:
	$(COMPOSE) up message -d --build

message-restart:
	$(COMPOSE) up message -d --build --force-recreate --no-deps

message-down:
	$(COMPOSE) down message -v

# ==============================================================================
# Eureka Server
# ==============================================================================
eureka-server-build:
	cd eureka-server && ./gradlew clean build

eureka-server-up:
	$(COMPOSE) up eureka-server -d

eureka-server-down:
	$(COMPOSE) stop eureka-server

# ==============================================================================
# Config Server
# ==============================================================================
config-server-up:
	$(COMPOSE) up config-server -d

config-server-down:
	$(COMPOSE) stop config-server

# ==============================================================================
# Kafka (event bus)
# ==============================================================================
kafka-up:
	$(COMPOSE) up kafka -d

kafka-down:
	$(COMPOSE) stop kafka

# ==============================================================================
# Global / Teardown
# ==============================================================================
dbs-up:
	$(COMPOSE) up accounts-db cards-db loans-db redis -d

dbs-down:
	$(COMPOSE) stop accounts-db cards-db loans-db redis
	@echo "all dbs are down"

api-up: accounts-api-run cards-api-run loans-api
	@echo "restart apis"

config-eureka:
	$(COMPOSE) up config-server eureka-server -d

config-eureka-down:
	$(COMPOSE) down config-server eureka-server -v

services-up:
	$(COMPOSE) up accounts-api cards-api loans-api -d --build

services-down:
	$(COMPOSE) down accounts-api cards-api loans-api


# ==============================================================================
# All Services
# ==============================================================================
all-up:
	$(COMPOSE) up -d

all-down:
	$(COMPOSE) down -v

gateway-up:
	$(COMPOSE) up gateway-server -d --build

gateway-restart:
	$(COMPOSE) up gateway-server -d --build --force-recreate --no-deps

gateway-down:
	$(COMPOSE) down gateway-server -v

# ==============================================================================
# Docker Hub images (run without a local Dockerfile build)
# ==============================================================================
# Mutable default tag (overrideable): make images-build-push IMAGE_TAG=v1.0.0
# Immutable explicit tag (required):  make images-build-push-tag TAG=v1.0.0
accounts-image-build:
	docker build -t $(DOCKERHUB_USER)/securedbank-accounts-api:$(IMAGE_TAG) ./accounts

cards-image-build:
	docker build -t $(DOCKERHUB_USER)/securedbank-cards-api:$(IMAGE_TAG) ./cards

loans-image-build:
	docker build -t $(DOCKERHUB_USER)/securedbank-loans-api:$(IMAGE_TAG) ./loans

message-image-build:
	docker build -t $(DOCKERHUB_USER)/securedbank-message:$(IMAGE_TAG) ./message

config-server-image-build:
	docker build -t $(DOCKERHUB_USER)/securedbank-config-server:$(IMAGE_TAG) ./config-server

eureka-server-image-build:
	docker build -t $(DOCKERHUB_USER)/securedbank-eureka-server:$(IMAGE_TAG) ./eureka-server

gateway-server-image-build:
	docker build -t $(DOCKERHUB_USER)/securedbank-gateway-server:$(IMAGE_TAG) ./gateway-server

images-build: accounts-image-build cards-image-build loans-image-build message-image-build \
	config-server-image-build eureka-server-image-build gateway-server-image-build

accounts-image-push:
	docker push $(DOCKERHUB_USER)/securedbank-accounts-api:$(IMAGE_TAG)

cards-image-push:
	docker push $(DOCKERHUB_USER)/securedbank-cards-api:$(IMAGE_TAG)

loans-image-push:
	docker push $(DOCKERHUB_USER)/securedbank-loans-api:$(IMAGE_TAG)

message-image-push:
	docker push $(DOCKERHUB_USER)/securedbank-message:$(IMAGE_TAG)

config-server-image-push:
	docker push $(DOCKERHUB_USER)/securedbank-config-server:$(IMAGE_TAG)

eureka-server-image-push:
	docker push $(DOCKERHUB_USER)/securedbank-eureka-server:$(IMAGE_TAG)

gateway-server-image-push:
	docker push $(DOCKERHUB_USER)/securedbank-gateway-server:$(IMAGE_TAG)

images-push: accounts-image-push cards-image-push loans-image-push message-image-push \
	config-server-image-push eureka-server-image-push gateway-server-image-push

images-build-push: images-build images-push

images-pull:
	$(COMPOSE_IMAGE) pull accounts-api cards-api loans-api message \
		config-server eureka-server gateway-server

# ------------------------------------------------------------------------------
# Immutable tags — TAG is required (fails fast if omitted)
# Example: make images-build-push-tag TAG=2026.03.04
# ------------------------------------------------------------------------------
require-tag:
	@test -n "$(TAG)" || (echo "TAG is required. Example: make images-build-push-tag TAG=v1.0.0" && exit 1)

accounts-image-build-tag: require-tag
	$(MAKE) accounts-image-build IMAGE_TAG=$(TAG)

cards-image-build-tag: require-tag
	$(MAKE) cards-image-build IMAGE_TAG=$(TAG)

loans-image-build-tag: require-tag
	$(MAKE) loans-image-build IMAGE_TAG=$(TAG)

message-image-build-tag: require-tag
	$(MAKE) message-image-build IMAGE_TAG=$(TAG)

config-server-image-build-tag: require-tag
	$(MAKE) config-server-image-build IMAGE_TAG=$(TAG)

eureka-server-image-build-tag: require-tag
	$(MAKE) eureka-server-image-build IMAGE_TAG=$(TAG)

gateway-server-image-build-tag: require-tag
	$(MAKE) gateway-server-image-build IMAGE_TAG=$(TAG)

images-build-tag: require-tag
	$(MAKE) images-build IMAGE_TAG=$(TAG)

accounts-image-push-tag: require-tag
	$(MAKE) accounts-image-push IMAGE_TAG=$(TAG)

cards-image-push-tag: require-tag
	$(MAKE) cards-image-push IMAGE_TAG=$(TAG)

loans-image-push-tag: require-tag
	$(MAKE) loans-image-push IMAGE_TAG=$(TAG)

message-image-push-tag: require-tag
	$(MAKE) message-image-push IMAGE_TAG=$(TAG)

config-server-image-push-tag: require-tag
	$(MAKE) config-server-image-push IMAGE_TAG=$(TAG)

eureka-server-image-push-tag: require-tag
	$(MAKE) eureka-server-image-push IMAGE_TAG=$(TAG)

gateway-server-image-push-tag: require-tag
	$(MAKE) gateway-server-image-push IMAGE_TAG=$(TAG)

images-push-tag: require-tag
	$(MAKE) images-push IMAGE_TAG=$(TAG)

images-build-push-tag: require-tag
	$(MAKE) images-build-push IMAGE_TAG=$(TAG)

accounts-image-up:
	$(COMPOSE_IMAGE) up accounts-db accounts-api -d --no-build

cards-image-up:
	$(COMPOSE_IMAGE) up cards-db cards-api -d --no-build

loans-image-up:
	$(COMPOSE_IMAGE) up loans-db loans-api -d --no-build

message-image-up:
	$(COMPOSE_IMAGE) up kafka message -d --no-build

services-image-up:
	$(COMPOSE_IMAGE) up accounts-api cards-api loans-api kafka message -d --no-build

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
	$(COMPOSE) watch

watch-all-up: all-up watch
	@echo "start all and watch"

watch-accounts:
	$(COMPOSE) watch accounts-api

watch-cards:
	$(COMPOSE) watch cards-api

watch-loans:
	$(COMPOSE) watch loans-api

watch-gateway:
	$(COMPOSE) watch gateway-server

watch-message:
	$(COMPOSE) watch message

# ==============================================================================
# OpenTofu (Keycloak realm, clients, users)
# ==============================================================================
# Requires OpenTofu (tofu) on PATH and a reachable Keycloak at keycloak_url.
# Copy infra/terraform.tfvars.example to infra/terraform.tfvars on first use.

keycloak-up:
	$(COMPOSE_KEYCLOAK) up -d

keycloak-down:
	$(COMPOSE_KEYCLOAK) down -v

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

# ==============================================================================
# Kubernetes (kind) — apply manifests
# ==============================================================================
# Platform resources live under kubernetes/; service objects under <service>/k8s/.
# Calico version pin for NetworkPolicy enforcement (kindnet does not enforce).
CALICO_VERSION := v3.29.2

k8s-keycloak:
	kubectl apply -f kubernetes/1_keycloak.yml

k8s-configmap:
	kubectl apply -f kubernetes/2_configmap.yml

# Requires a kind cluster created with disableDefaultCNI: true (see docs/kubernetes.md).
k8s-calico:
	kubectl apply -f https://raw.githubusercontent.com/projectcalico/calico/$(CALICO_VERSION)/manifests/calico.yaml

k8s-kafka:
	kubectl apply -f kubernetes/9_kafka.yml

k8s-accounts:
	kubectl apply -f accounts/k8s/

k8s-cards:
	kubectl apply -f cards/k8s/

k8s-loans:
	kubectl apply -f loans/k8s/

k8s-message:
	kubectl apply -f message/k8s/

k8s-config-server:
	kubectl apply -f config-server/k8s/

k8s-eureka-server:
	kubectl apply -f eureka-server/k8s/

k8s-gateway-server:
	kubectl apply -f gateway-server/k8s/

k8s-platform: k8s-keycloak k8s-configmap
	@echo "platform manifests applied (keycloak + configmap)"

k8s-services: k8s-config-server k8s-eureka-server k8s-kafka k8s-accounts k8s-cards k8s-loans k8s-message k8s-gateway-server
	@echo "service manifests applied"

k8s-up: k8s-platform k8s-services
	@echo "all kubernetes manifests applied"

# ==============================================================================
# Helm (umbrella chart — alternative to make k8s-*)
# ==============================================================================
# No Bitnami deps. Library chart: helm/securedbank/charts/securedbank-lib
# Raw kubernetes/ and */k8s/ remain valid for learning / granular apply.
HELM_CHART := helm/securedbank
HELM_RELEASE := securedbank

helm-deps:
	helm dependency update $(HELM_CHART)

helm-lint: helm-deps
	helm lint $(HELM_CHART)

helm-template: helm-deps
	helm template $(HELM_RELEASE) $(HELM_CHART)

helm-up: helm-deps
	helm upgrade --install $(HELM_RELEASE) $(HELM_CHART)

helm-down:
	helm uninstall $(HELM_RELEASE)

apis-up:
	$(COMPOSE) up accounts-api cards-api loans-api -d --build

apis-down:
	$(COMPOSE) down accounts-api cards-api loans-api -v
