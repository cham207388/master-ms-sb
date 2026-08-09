package com.abcham.cards;

import com.abcham.cards.config.ContactInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(value = {ContactInfo.class})
public class CardsApplication {

    static void main(String[] args) {

        SpringApplication.run(CardsApplication.class, args);
    }

}
