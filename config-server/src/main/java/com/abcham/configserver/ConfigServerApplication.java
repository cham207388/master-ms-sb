package com.abcham.configserver;

import org.springframework.amqp.core.DeclarableCustomizer;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Bean;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

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
}
