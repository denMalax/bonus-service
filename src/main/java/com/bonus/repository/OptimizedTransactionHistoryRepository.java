package com.bonus.repository;

import com.bonus.entity.TransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Repository
public interface OptimizedTransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    Page<TransactionHistory> findByCardNumberOrderByCreatedAtDesc(String cardNumber, Pageable pageable);

    @Query("SELECT th FROM TransactionHistory th " +
            "WHERE th.cardNumber = :cardNumber " +
            "AND th.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY th.createdAt DESC")
    List<TransactionHistory> findHistoryBetweenDates(
            @Param("cardNumber") String cardNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT " +
            "SUM(CASE WHEN th.operationType IN ('EARN', 'REFUND_BURN') THEN th.amount ELSE 0 END) as totalEarned, " +
            "SUM(CASE WHEN th.operationType IN ('BURN', 'REFUND_EARN') THEN th.amount ELSE 0 END) as totalSpent " +
            "FROM TransactionHistory th " +
            "WHERE th.cardNumber = :cardNumber " +
            "AND th.createdAt >= :since")
    Object[] getAggregatedStats(@Param("cardNumber") String cardNumber, @Param("since") LocalDateTime since);

    @Query(value = "SELECT * FROM mv_card_statistics WHERE card_number = :cardNumber", nativeQuery = true)
    Map<String, Object> getCardStatistics(@Param("cardNumber") String cardNumber);

    @Query("SELECT th FROM TransactionHistory th WHERE th.createdAt > :since")
    Stream<TransactionHistory> streamHistorySince(@Param("since") LocalDateTime since);
}