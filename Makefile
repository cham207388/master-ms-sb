.PHONY: accounts-build cards-build loans-build

accounts-build:
	cd accounts && ./gradlew clean build

accounts-db-up:
	docker compose up accounts-db -d

accounts-db-down:
	docker compose down accounts-db -v

accounts-api-run:
	docker compose up accounts-api -d

accounts: accounts-db-up accounts-api-run
	echo "accounts service is running"

cards-build:
	cd cards && ./gradlew clean build

cards-db-up:
	docker compose up cards-db -d

cards-db-down:
	docker compose down cards-db -v

cards-api:
	docker compose up cards-api -d

cards: cards-build cards-db-up cards-api-run
	echo "cards service is running"
	

loans-build:
	cd loans && ./gradlew clean build

loans-db-up:
	docker compose up loans-db -d

loans-db-down:
	docker compose down loans-db -v

loans-api:
	docker compose up loans-api -d

loans: loans-build loans-db-up loans-api-run
	echo "loans service is running"

dbs-up: accounts-db-up cards-db-up loans-db-up
	echo "all dbs are running"

dbs-down: accounts-db-down cards-db-down loans-db-down
	echo "all dbs are down"
