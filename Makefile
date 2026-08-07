.PHONY: accounts accounts-build accounts-db-up accounts-db-down accounts-api-run \
        cards cards-build cards-db-up cards-db-down cards-api-run \
        loans loans-build loans-db-up loans-db-down loans-api \
        eureka-server-build eureka-server-up eureka-server-down dbs-down

# ==============================================================================
# Accounts Service
# ==============================================================================
accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up:
	docker compose up accounts-db -d

accounts-db-down:
	docker compose down accounts-db -v

accounts-api-run:
	docker compose up accounts-api -d --build

accounts:
	docker compose up accounts-db accounts-api -d

accounts-down:
	docker compose down accounts-db accounts-api -v

# ==============================================================================
# Cards Service
# ==============================================================================
cards-build:
	cd cards && ./gradlew clean build

cards-db-up:
	docker compose up cards-db -d

cards-db-down:
	docker compose down cards-db -v

cards-api-run:
	docker compose up cards-api -d --build

cards:
	docker compose up cards-db cards-api -d

cards-down:
	docker compose down cards-db cards-api -v


# ==============================================================================
# Loans Service
# ==============================================================================
loans-build:
	cd loans && ./gradlew clean build

loans-db-up:
	docker compose up loans-db -d

loans-db-down:
	docker compose down loans-db -v

loans-api:
	docker compose up loans-api -d --build

loans:
	docker compose up loans-db loans-api -d

loans-down:
	docker compose down loans-db loans-api -v

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
dbs-down: accounts-db-down cards-db-down loans-db-down
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