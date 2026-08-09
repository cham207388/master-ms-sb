package com.abcham.loans;

import com.abcham.loans.config.ContactInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;

@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(value = {ContactInfo.class})
public class LoansApplication {

    static void main(String[] args) {
        SpringApplication.run(LoansApplication.class, args);
    }

}
