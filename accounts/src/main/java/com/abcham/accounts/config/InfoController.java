package com.abcham.accounts.config;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Getter
@Setter
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class InfoController {

    private final ContactInfo contactInfo;
    private final Environment environment;

    @GetMapping("/contact-info")
    public ResponseEntity<ContactInfo> getContactInfo() {

        return ResponseEntity.ok(contactInfo);
    }

    @GetMapping("/name-info")
    public ResponseEntity<String> getNameInfo() {

        return ResponseEntity.ok("Alhagie Bai Cham");
    }

    @GetMapping("/build-info")
    @Retry(name = "getBuildInfo", fallbackMethod = "getBuildInfoFallback")
    public ResponseEntity<String> getCustomerDetailsDto() {

        return ResponseEntity.ok("buildVersion");
    }

    public ResponseEntity<String> getBuildInfoFallback(Throwable throwable) {

        log.debug("getBuildInfoFallback() method Invoked");
        return ResponseEntity.ok("0.9");
    }

    @GetMapping("/java-version")
    @RateLimiter(name = "getJavaVersion", fallbackMethod = "getJavaVersionFallback")
    public ResponseEntity<String> getJavaVersion() {

        return ResponseEntity.ok(environment.getProperty("JAVA_HOME"));
    }

    public ResponseEntity<String> getJavaVersionFallback(Throwable throwable) {

        log.debug("getJavaVersionFallback() method Invoked");
        return ResponseEntity.ok("25");
    }

}
