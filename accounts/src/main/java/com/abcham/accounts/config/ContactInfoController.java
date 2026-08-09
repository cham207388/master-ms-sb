package com.abcham.accounts.config;

import com.abcham.accounts.dto.CustomerDetailsDto;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class ContactInfoController {

    private final ContactInfo contactInfo;

    @GetMapping("/contact-info")
    public ResponseEntity<ContactInfo> getContactInfo() {

        return ResponseEntity.ok(contactInfo);
    }

    @GetMapping("/build-info")
    @Retry(name = "getBuildInfo",fallbackMethod = "getBuildInfoFallback")
    public ResponseEntity<String> getCustomerDetailsDto() {
        return ResponseEntity.ok("buildVersion");
    }

    public ResponseEntity<String> getBuildInfoFallback(Throwable throwable) {
        log.debug("getBuildInfoFallback() method Invoked");
        return ResponseEntity.ok("0.9");
    }
}
