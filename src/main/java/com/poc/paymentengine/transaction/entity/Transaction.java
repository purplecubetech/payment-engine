package com.poc.paymentengine.transaction.entity;


import com.poc.paymentengine.account.entity.Account;
import com.poc.paymentengine.cbs.enums.CbsSyncStatus;
import com.poc.paymentengine.common.entity.BaseEntity;
import com.poc.paymentengine.transaction.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_reference", nullable = false, unique = true)
    private String requestReference;

    @Column(name = "transaction_reference", nullable = false, unique = true)

    private String transactionReference;

    @Column(name = "sender_account_id", nullable = false)
    private Long sender;

    @Column(name = "receiver_account_id", nullable = false)
    private Long receiver;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "cbs_sync_status", nullable = false)
    private CbsSyncStatus cbsSyncStatus;
}