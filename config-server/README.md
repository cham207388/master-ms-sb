# Config Server

- resources/config/application-name.yml
- resources/config/application-name-profile.yml
- [see info](http://localhost:8071/application-name)

---

## encrypt|decrypt

add an `encrypt.key` property to your `application.properties` or `application.yml` file.

- [resource](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_encryption_and_decryption
- [encrypt](http://localhost:8071/encrypt)
- [decrypt](http://localhost:8071/decrypt)

## refresh

**manual refresh: by every client**

- needs actuator dependency
- POST: /actuator/refresh 
  - refresh is not exposed by default, 
  - you need to add `management.endpoints.web.exposure.include=refresh` to your `application.properties` or `application.yml` file)
  - refresh endpoint to be invoked by each client

**Using Spring Cloud Bus: manual by only one client**

- requires rabbit mq or Kafka instance
- add *spring-cloud-starter-bus-amqp* dependency to your **config server** and **config client**
- refresh endpoint to be invoked by only one client

**automatic refresh**

- add *spring-cloud-starter-bus-monitor* dependency to your **config server**
- you need to add `management.endpoints.web.exposure.include=refresh`
- create web hook to your git repository, and point it to your config server refresh endpoint
- hookdeck.com (webhook testing service) can be used to test the web hook

---

<details>
<summary>🛠️ Troubleshooting: Spring Cloud Bus RabbitMQ PRECONDITION_FAILED (x-queue-leader-locator)</summary>

<br/>

### ⚠️ Symptom & Error Log
When running the Config Server with Spring Cloud Bus and RabbitMQ, you may encounter continuous `PRECONDITION_FAILED` and `NOT_FOUND` warnings/errors during queue declaration:

```text
Shutdown Signal: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - invalid arg 'x-queue-leader-locator' for queue 'springCloudBus.anonymous.qc1DgpGYQ3uX7X6I0N1GDQ' in vhost '/' of queue type rabbit_classic_queue, class-id=50, method-id=10)
```

---

### 🔍 Root Cause Analysis
1. **Classic Queue Incompatibility**: Spring Cloud Bus creates anonymous classic queues (`rabbit_classic_queue`) to listen for refresh events.
2. **Unsupported Queue Argument**: Spring AMQP (`spring-rabbit` 3.2.11+ / 4.1.0) automatically attaches `x-queue-leader-locator=client-local` to anonymous queue declarations.
3. **Broker Rejection**: RabbitMQ 3.12+ / 3.13+ / 4.x enforces queue arguments and strictly rejects `x-queue-leader-locator` on classic queues, closing the channel with code `406 PRECONDITION_FAILED`.

---

### ✅ Solution
Register a `DeclarableCustomizer` bean in your main application class ([`MasterMsSbConfigServerApplication.java`](file:///Users/baicham/develop/java-projects/master-ms-sb-config-server/src/main/java/com/abcham/configserver/MasterMsSbConfigServerApplication.java)). This customizer strips unsupported leader locator arguments right before `RabbitAdmin` declares the queue:

```java
@Bean
public DeclarableCustomizer queueLeaderLocatorRemover() {
    return declarable -> {
        if (declarable instanceof Queue queue) {
            queue.getArguments().remove("x-queue-leader-locator");
            queue.getArguments().remove("x-queue-master-locator");
        }
        return declarable;
    };
}
```

---

### 📚 Official Documentation & References
* **Spring AMQP GitHub Issue [#3478](https://github.com/spring-projects/spring-amqp/issues/3478)**: *`[Regression] x-queue-leader-locator breaks classic queue declarations`*
* **Spring AMQP GitHub Issue [#3419](https://github.com/spring-projects/spring-amqp/issues/3419)**: *`queue-master-locator argument denied by default for classic queues`*
* **[RabbitMQ Queue Documentation](https://www.rabbitmq.com/docs/queues)**: Explains that `x-queue-leader-locator` is strictly for Quorum Queues / Streams and rejected on classic queues.
* **[Spring AMQP Reference Guide](https://docs.spring.io/spring-amqp/reference/amqp/broker-configuration.html)**: Details `DeclarableCustomizer` usage with `RabbitAdmin`.

</details>