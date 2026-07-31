package com.poc.paymentengine.account.repository;

import com.poc.paymentengine.account.dto.response.AccountResponse;
import com.poc.paymentengine.account.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.id = :id
            """)
    Optional<Account> findAndLockById(@Param("id") Long id);

    @Query("""
        SELECT new com.poc.paymentengine.account.dto.response.AccountResponse(
            a.id,
            a.accountNumber,
            a.balance,
            a.status
        )
        FROM Account a
        WHERE a.id = :id
    """)
    Optional<AccountResponse> findAccountById(Long id);

    boolean existsByAccountNumber(String accountNumber);
}