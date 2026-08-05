.PHONY: accounts-build cards-build loans-build

accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up:
	cd accounts && docker compose up accounts-db -d

accounts-db-down:
	cd accounts && docker compose down accounts-db -v

accounts-api-run:
	cd accounts && docker compose up accounts-api -d

accounts: accounts-db-up accounts-api-run
	echo "accounts service is running"

cards-build:
	cd cards && ./gradlew clean build

cards-db-up:
	cd cards && docker compose up cards-db -d

cards-db-down:
	cd cards && docker compose down cards-db -v

cards-api-run:
	cd cards && docker compose up cards-api -d


cards: cards-db-up cards-api-run
	echo "cards service is running"
	

loans-build:
	cd loans && ./gradlew clean build

loans-db-up:
	cd loans && docker compose up loans-db -d

loans-db-down:
	cd loans && docker compose down loans-db -v

loans-api:
	cd loans && docker compose up loans-api -d

loans: loans-db-up loans-api-run
	echo "loans service is running"

dbs-down: accounts-db-down cards-db-down loans-db-down
	echo "all dbs are down"
