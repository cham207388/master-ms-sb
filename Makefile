.PHONY: accounts-build cards-build loans-build

accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up:
	docker compose up accounts-db -d

accounts-db-down:
	docker compose down accounts-db -v

accounts-api-run:
	docker compose up accounts-api -d

accounts: accounts-build accounts-db-up accounts-api-run
	echo "accounts service is running"

cards-build:
	cd cards && ./gradlew clean build

cards-db:
	docker compose up cards-db -d

cards-db-down:
	docker compose down cards-db -v

cards-api:
	docker compose up cards-api -d

cards: cards-build cards-db-up cards-api-run
	echo "cards service is running"
	

loans-build:
	cd loans && ./gradlew clean build

loans-db:
	docker compose up loans-db -d

loans-db-down:
	docker compose down loans-db -v

loans-api:
	docker compose up loans-api -d

loans: loans-build loans-db-up loans-api-run
	echo "loans service is running"

dbs-up:
    docker compose up -d

dbs-down:
    docker compose down -v