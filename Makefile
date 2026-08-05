.PHONY: db-compose-up db-compose-down accounts-schema cards-schema build-account build-cards

COMPOSE = docker compose

compose-up:
	$(COMPOSE) up -d

compose-down:
	$(COMPOSE) down -v

accounts-schema:
	psql -U postgres -d microservice -p 5423 -f accounts/src/main/resources/schema.sql

cards-schema:
	psql -U postgres -d microservice -p 5423 -f cards/src/main/resources/schema.sql

build-account:
	cd accounts && ./gradlew build -x test

build-cards:
	cd cards && ./gradlew build -x test
