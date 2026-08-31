.PHONY: accounts accounts-build accounts-db-up accounts-db-down accounts-api-run \
        cards cards-build cards-db-up cards-db-down cards-api-run \
        loans loans-build loans-db-up loans-db-down loans-api \
        bank-db-up bank-db-down \
        eureka-server-build eureka-server-up eureka-server-down dbs-down \
        watch watch-accounts watch-cards watch-loans watch-gateway

# ==============================================================================
# Database (Shared Bank PostgreSQL)
# ==============================================================================
bank-db-up:
	docker compose up bank-db -d

bank-db-down:
	docker compose down bank-db -v

# ==============================================================================
# Accounts Service
# ==============================================================================
accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up: bank-db-up

accounts-db-down:
	@echo "Shared bank database is running. Use 'make dbs-down' to stop and wipe bank-db."

accounts-api-run:
	docker compose up accounts-api -d --build --no-deps

accounts:
	docker compose up bank-db accounts-api -d

accounts-down:
	docker compose stop accounts-api && docker compose rm -f accounts-api

# ==============================================================================
# Cards Service
# ==============================================================================
cards-build:
	cd cards && ./gradlew clean build

cards-db-up: bank-db-up

cards-db-down:
	@echo "Shared bank database is running. Use 'make dbs-down' to stop and wipe bank-db."

cards-api-run:
	docker compose up cards-api -d --build --no-deps

cards:
	docker compose up bank-db cards-api -d

cards-down:
	docker compose stop cards-api && docker compose rm -f cards-api


# ==============================================================================
# Loans Service
# ==============================================================================
loans-build:
	cd loans && ./gradlew clean build

loans-db-up: bank-db-up

loans-db-down:
	@echo "Shared bank database is running. Use 'make dbs-down' to stop and wipe bank-db."

loans-api:
	docker compose up loans-api -d --build --no-deps

loans:
	docker compose up bank-db loans-api -d

loans-down:
	docker compose stop loans-api && docker compose rm -f loans-api

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
dbs-down:
	docker compose down bank-db -v
	@echo "Shared bank database is down"

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

gateway-down:
	docker compose down gateway-server -v

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
