package com.bonus.repository;

import com.bonus.entity.OperationType;
import com.bonus.entity.TransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    // Получить историю по карте (от новых к старым)
    List<TransactionHistory> findByCardNumberOrderByCreatedAtDesc(String cardNumber);

    // Пагинированная история
    Page<TransactionHistory> findByCardNumberOrderByCreatedAtDesc(String cardNumber, Pageable pageable);

    // Поиск по referenceId
    Optional<TransactionHistory> findByReferenceId(String referenceId);

    // Поиск по типу операции
    List<TransactionHistory> findByOperationType(OperationType type);

    // Поиск за период
    List<TransactionHistory> findByCardNumberAndCreatedAtBetween(
            String cardNumber,
            LocalDateTime start,
            LocalDateTime end
    );

    // Сумма всех начислений по карте (для аналитики)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionHistory t " +
            "WHERE t.cardNumber = :cardNumber AND t.operationType IN ('EARN', 'REFUND_BURN')")
    BigDecimal sumEarnedBonuses(@Param("cardNumber") String cardNumber);

    // Сумма всех списаний по карте
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionHistory t " +
            "WHERE t.cardNumber = :cardNumber AND t.operationType IN ('BURN', 'REFUND_EARN')")
    BigDecimal sumBurnedBonuses(@Param("cardNumber") String cardNumber);

    // Последние операции по карте
    Optional<TransactionHistory> findFirstByCardNumberOrderByCreatedAtDesc(String cardNumber);
}