package com.bonus.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history",
        indexes = {
                @Index(name = "idx_card_history", columnList = "cardNumber, createdAt DESC"),
                @Index(name = "idx_reference_id", columnList = "referenceId"),
                @Index(name = "idx_created_at", columnList = "createdAt DESC")
        })
@Data
@EntityListeners(AuditingEntityListener.class)
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, length = 50)
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "description", length = 500)
    private String description;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}