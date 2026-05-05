package com.bonus.service;

import com.bonus.dto.*;
import com.bonus.entity.BonusCard;
import com.bonus.entity.OperationType;
import com.bonus.entity.TransactionHistory;
import com.bonus.exception.InsufficientBalanceException;
import com.bonus.exception.NotFoundException;
import com.bonus.repository.BonusCardRepository;
import com.bonus.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BonusService {

    private final BonusCardRepository bonusCardRepository;
    private final TransactionHistoryRepository historyRepository;

    // 1) Начисление бонусов
    public TransactionResponse earnBonuses(EarnRequest request) {
        BonusCard card = getOrCreateCard(request.getCardNumber());
        BigDecimal balanceBefore = card.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(request.getAmount());

        card.setBalance(balanceAfter);
        card.setUpdatedAt(LocalDateTime.now());
        bonusCardRepository.save(card);

        TransactionHistory history = createHistory(card.getCardNumber(),
                OperationType.EARN, request.getAmount(), balanceBefore, balanceAfter, request.getOrderId());
        historyRepository.save(history);

        log.info("Начислено {} бонусов на карту {}", request.getAmount(), request.getCardNumber());
        return mapToResponse(history);
    }

    // 2) Списание бонусов
    public TransactionResponse burnBonuses(BurnRequest request) {
        BonusCard card = bonusCardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));

        if (card.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Недостаточно бонусов");
        }

        BigDecimal balanceBefore = card.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(request.getAmount());

        card.setBalance(balanceAfter);
        card.setUpdatedAt(LocalDateTime.now());
        bonusCardRepository.save(card);

        TransactionHistory history = createHistory(card.getCardNumber(),
                OperationType.BURN, request.getAmount(), balanceBefore, balanceAfter, request.getOrderId());
        historyRepository.save(history);

        log.info("Списано {} бонусов с карты {}", request.getAmount(), request.getCardNumber());
        return mapToResponse(history);
    }

    // 3) Возврат бонусов
    public TransactionResponse refundBonuses(RefundRequest request) {
        // Находим исходную операцию по referenceId
        TransactionHistory originalTx = historyRepository.findByReferenceId(request.getOriginalTransactionId())
                .orElseThrow(() -> new NotFoundException("Исходная операция не найдена"));

        BonusCard card = bonusCardRepository.findByCardNumber(originalTx.getCardNumber())
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));

        BigDecimal balanceBefore = card.getBalance();
        BigDecimal balanceAfter;
        OperationType refundType;

        if (originalTx.getOperationType() == OperationType.EARN) {
            // Возврат начисленных - списываем обратно
            refundType = OperationType.REFUND_EARN;
            balanceAfter = balanceBefore.subtract(originalTx.getAmount());
        } else if (originalTx.getOperationType() == OperationType.BURN) {
            // Возврат списанных - возвращаем на баланс
            refundType = OperationType.REFUND_BURN;
            balanceAfter = balanceBefore.add(originalTx.getAmount());
        } else {
            throw new IllegalArgumentException("Невозможно сделать возврат для данного типа операции");
        }

        card.setBalance(balanceAfter);
        card.setUpdatedAt(LocalDateTime.now());
        bonusCardRepository.save(card);

        TransactionHistory history = createHistory(card.getCardNumber(),
                refundType, originalTx.getAmount(), balanceBefore, balanceAfter, request.getOriginalTransactionId());
        historyRepository.save(history);

        log.info("Выполнен возврат по операции {}", request.getOriginalTransactionId());
        return mapToResponse(history);
    }

    // 4) Получить баланс
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String cardNumber) {
        BonusCard card = bonusCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));
        return new BalanceResponse(card.getCardNumber(), card.getBalance());
    }

    // 5) Получить историю
    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistory(String cardNumber) {
        return historyRepository.findByCardNumberOrderByCreatedAtDesc(cardNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BonusCard getOrCreateCard(String cardNumber) {
        return bonusCardRepository.findByCardNumber(cardNumber)
                .orElseGet(() -> {
                    BonusCard newCard = new BonusCard();
                    newCard.setCardNumber(cardNumber);
                    newCard.setBalance(BigDecimal.ZERO);
                    newCard.setCreatedAt(LocalDateTime.now());
                    newCard.setUpdatedAt(LocalDateTime.now());
                    return bonusCardRepository.save(newCard);
                });
    }

    private TransactionHistory createHistory(String cardNumber, OperationType type,
                                             BigDecimal amount, BigDecimal before, BigDecimal after, String refId) {
        TransactionHistory history = new TransactionHistory();
        history.setCardNumber(cardNumber);
        history.setOperationType(type);
        history.setAmount(amount);
        history.setBalanceBefore(before);
        history.setBalanceAfter(after);
        history.setReferenceId(refId);
        history.setCreatedAt(LocalDateTime.now());
        return history;
    }

    private TransactionResponse mapToResponse(TransactionHistory history) {
        return new TransactionResponse(
                history.getId(),
                history.getCardNumber(),
                history.getOperationType().toString(),
                history.getAmount(),
                history.getBalanceBefore(),
                history.getBalanceAfter(),
                history.getReferenceId(),
                history.getCreatedAt()
        );
    }
}
