package com.bonus.repository;

import com.bonus.entity.BonusCard;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OptimizedBonusCardRepository extends JpaRepository<BonusCard, Long> {

    @Cacheable(value = "cardBalance", key = "#cardNumber")
    Optional<BonusCard> findByCardNumber(String cardNumber);

    @CacheEvict(value = "cardBalance", key = "#card.cardNumber")
    BonusCard save(BonusCard card);

    @Query("SELECT b.balance FROM BonusCard b WHERE b.cardNumber = :cardNumber")
    Optional<BigDecimal> findBalanceByCardNumber(@Param("cardNumber") String cardNumber);

    Page<BonusCard> findAllByOrderByBalanceDesc(Pageable pageable);

    @Modifying
    @Query("UPDATE BonusCard b SET b.balance = b.balance + :amount WHERE b.cardNumber = :cardNumber")
    int updateBalance(@Param("cardNumber") String cardNumber, @Param("amount") BigDecimal amount);

    @Query("SELECT b FROM BonusCard b WHERE b.cardNumber IN :cardNumbers")
    List<BonusCard> findAllByCardNumbers(@Param("cardNumbers") List<String> cardNumbers);
}