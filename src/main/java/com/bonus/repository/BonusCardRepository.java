package com.bonus.repository;

import com.bonus.entity.BonusCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface BonusCardRepository extends JpaRepository<BonusCard, Long> {

    // Поиск карты по номеру
    Optional<BonusCard> findByCardNumber(String cardNumber);

    // Проверка существования карты
    boolean existsByCardNumber(String cardNumber);

    // Подсчет всех карт с балансом выше порога (для отчетов)
    @Query("SELECT COUNT(b) FROM BonusCard b WHERE b.balance > :threshold")
    long countCardsWithBalanceAbove(@Param("threshold") java.math.BigDecimal threshold);
}