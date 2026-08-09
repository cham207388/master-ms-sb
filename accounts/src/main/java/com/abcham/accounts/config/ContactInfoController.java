package com.abcham.accounts.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Getter
@Setter
@RestController
@RequiredArgsConstructor
public class ContactInfoController {

    private final ContactInfo contactInfo;

    @GetMapping("/api/accounts/contact-info")
    public ResponseEntity<ContactInfo> getContactInfo() {

        return ResponseEntity.ok(contactInfo);
    }

}
