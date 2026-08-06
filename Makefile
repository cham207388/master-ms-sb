.PHONY: accounts accounts-build accounts-db-up accounts-db-down accounts-api-run \
        cards cards-build cards-db-up cards-db-down cards-api-run \
        loans loans-build loans-db-up loans-db-down loans-api dbs-down

# ==============================================================================
# Accounts Service
# ==============================================================================
accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up:
	cd accounts && docker compose up accounts-db -d

accounts-db-down:
	docker compose -f accounts/compose.yml down accounts-db -v

accounts-api-run:
	docker compose -f accounts/compose.yml up accounts-api -d --build

accounts:
	docker compose -f accounts/compose.yml up -d

accounts-down:
	docker compose -f accounts/compose.yml down -d

# ==============================================================================
# Cards Service
# ==============================================================================
cards-build:
	cd cards && ./gradlew clean build

cards-db-up:
	docker compose -f cards/compose.yml up cards-db -d

cards-db-down:
	docker compose -f cards/compose.yml down cards-db -v

cards-api-run:
	docker compose -f cards/compose.yml up cards-api -d

cards:
	docker compose -f cards/compose.yml up -d

cards-down:
	docker compose -f cards/compose.yml down -d


# ==============================================================================
# Loans Service
# ==============================================================================
loans-build:
	cd loans && ./gradlew clean build

loans-db-up:
	docker compose -f loans/compose.yml up loans-db -d

loans-db-down:
	docker compose -f loans/compose.yml down loans-db -v

loans-api:
	docker compose -f loans/compose.yml up loans-api -d

loans:
	docker compose -f loans/compose.yml up -d

loans-down:
	docker compose -f loans/compose.yml down -d

# ==============================================================================
# Config Server
# ==============================================================================
rabbit-mq-up:
	docker compose -f ../master-ms-sb-config-server/compose.yml up rabbit-mq -d

rabbit-mq-down:
	docker compose -f ../master-ms-sb-config-server/compose.yml down rabbit-mq -v

config-server-up:
	docker compose -f ../master-ms-sb-config-server/compose.yml up config-server -d

config-server-down:
	docker compose -f ../master-ms-sb-config-server/compose.yml down config-server -v

config-all-up:
	docker compose -f ../master-ms-sb-config-server/compose.yml up -d

config-all-down:
	docker compose -f ../master-ms-sb-config-server/compose.yml down -v

# ==============================================================================
# Global / Teardown
# ==============================================================================
dbs-down: accounts-db-down cards-db-down loans-db-down
	@echo "all dbs are down"
