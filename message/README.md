# Getting Started

## Docker

From the repository root, build and start Message and its RabbitMQ dependency:

```bash
docker compose up -d --build message
docker compose logs -f message
```

The root Compose stack includes Message, so `make all-up` starts it too.
Message connects to `rabbitmq:5672` on the shared `securedbank` network and
consumes `send-communication` through the `email|sms` function, publishing results
to `communication-sent`. It is a background worker with no HTTP port to publish.
The Java 25 image runs as a non-root user and includes the OpenTelemetry agent
used by the shared Docker configuration.

## Dependencies

* main `org.springframework.cloud:spring-cloud-function-context`
* turn into a web app:
  * `org.springframework.cloud:spring-cloud-function-web`
  * `org.springframework.boot:spring-boot-starter-webmvc`

---

- spring-cloud-stream

## Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/gradle-plugin/packaging-oci-image.html)
* [Function](https://docs.spring.io/spring-cloud-function/reference/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [Various sample apps using Spring Cloud Function](https://github.com/spring-cloud/spring-cloud-function/tree/main/spring-cloud-function-samples)
