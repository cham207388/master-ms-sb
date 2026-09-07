package com.abcham.accounts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Accounts extends BaseEntity {

    @Id
    private Long accountNumber;

    private Long customerId;

    private String accountType;

    private String branchAddress;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "communication_sw")
    private Boolean communicationSw;

}
