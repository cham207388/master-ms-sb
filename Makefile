.PHONY: build-account build-cards build-loans


build-account:
	cd accounts && ./gradlew build

build-cards:
	cd cards && ./gradlew build

build-loans:
	cd loans && ./gradlew build
