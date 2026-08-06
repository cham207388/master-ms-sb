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

# ==============================================================================
# Global / Teardown
# ==============================================================================
dbs-down: accounts-db-down cards-db-down loans-db-down
	@echo "all dbs are down"
