package com.abcham.accounts.repository;

import com.abcham.accounts.entity.Accounts;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    Optional<Accounts> findByCustomerId(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Accounts a where a.accountNumber = :accountNumber")
    Optional<Accounts> findByIdForUpdate(@Param("accountNumber") Long accountNumber);

    @Transactional
    @Modifying
    void deleteByCustomerId(Long customerId);

}
