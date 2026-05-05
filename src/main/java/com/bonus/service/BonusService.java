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
import org.springframework.dao.OptimisticLockingFailureException;
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
        try {
            BonusCard card = getOrCreateCard(request.getCardNumber());
            BigDecimal balanceBefore = card.getBalance();
            BigDecimal balanceAfter = balanceBefore.add(request.getAmount());

            card.setBalance(balanceAfter);
            card.setUpdatedAt(LocalDateTime.now());
            bonusCardRepository.save(card);

            TransactionHistory history = createHistory(
                    card.getCardNumber(),
                    OperationType.EARN,
                    request.getAmount(),
                    balanceBefore,
                    balanceAfter,
                    request.getOrderId(),
                    request.getDescription()
            );
            historyRepository.save(history);

            log.info("Начислено {} бонусов на карту {}", request.getAmount(), request.getCardNumber());
            return mapToResponse(history);

        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock exception, retrying earn operation...");
            return earnBonuses(request); // Ретрай
        }
    }

    // 2) Списание бонусов
    public TransactionResponse burnBonuses(BurnRequest request) {
        try {
            BonusCard card = bonusCardRepository.findByCardNumber(request.getCardNumber())
                    .orElseThrow(() -> new NotFoundException("Карта", request.getCardNumber()));

            if (card.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        request.getCardNumber(),
                        request.getAmount(),
                        card.getBalance()
                );
            }

            BigDecimal balanceBefore = card.getBalance();
            BigDecimal balanceAfter = balanceBefore.subtract(request.getAmount());

            card.setBalance(balanceAfter);
            card.setUpdatedAt(LocalDateTime.now());
            bonusCardRepository.save(card);

            TransactionHistory history = createHistory(
                    card.getCardNumber(),
                    OperationType.BURN,
                    request.getAmount(),
                    balanceBefore,
                    balanceAfter,
                    request.getOrderId(),
                    request.getDescription()
            );
            historyRepository.save(history);

            log.info("Списано {} бонусов с карты {}", request.getAmount(), request.getCardNumber());
            return mapToResponse(history);

        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock exception, retrying burn operation...");
            return burnBonuses(request);
        }
    }

    // 3) Возврат бонусов
    public TransactionResponse refundBonuses(RefundRequest request) {
        try {
            // Находим исходную операцию по referenceId
            TransactionHistory originalTx = historyRepository.findByReferenceId(request.getOriginalTransactionId())
                    .orElseThrow(() -> new NotFoundException("Транзакция", request.getOriginalTransactionId()));

            BonusCard card = bonusCardRepository.findByCardNumber(originalTx.getCardNumber())
                    .orElseThrow(() -> new NotFoundException("Карта", originalTx.getCardNumber()));

            BigDecimal balanceBefore = card.getBalance();
            BigDecimal balanceAfter;
            OperationType refundType;
            String description;

            if (originalTx.getOperationType() == OperationType.EARN) {
                // Возврат начисленных - списываем обратно
                refundType = OperationType.REFUND_EARN;
                balanceAfter = balanceBefore.subtract(originalTx.getAmount());
                description = "Возврат начисленных бонусов по операции " + request.getOriginalTransactionId();

                if (balanceBefore.compareTo(originalTx.getAmount()) < 0) {
                    throw new InsufficientBalanceException(
                            card.getCardNumber(),
                            originalTx.getAmount(),
                            balanceBefore
                    );
                }
            } else if (originalTx.getOperationType() == OperationType.BURN) {
                // Возврат списанных - возвращаем на баланс
                refundType = OperationType.REFUND_BURN;
                balanceAfter = balanceBefore.add(originalTx.getAmount());
                description = "Возврат списанных бонусов по операции " + request.getOriginalTransactionId();
            } else {
                throw new IllegalArgumentException("Невозможно сделать возврат для операции типа: " + originalTx.getOperationType());
            }

            card.setBalance(balanceAfter);
            card.setUpdatedAt(LocalDateTime.now());
            bonusCardRepository.save(card);

            TransactionHistory history = createHistory(
                    card.getCardNumber(),
                    refundType,
                    originalTx.getAmount(),
                    balanceBefore,
                    balanceAfter,
                    request.getOriginalTransactionId(),
                    description
            );
            historyRepository.save(history);

            log.info("Выполнен возврат по операции {}", request.getOriginalTransactionId());
            return mapToResponse(history);

        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock exception, retrying refund operation...");
            return refundBonuses(request);
        }
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
                .orElseGet(() -> createNewCard(cardNumber));
    }

    private TransactionHistory createHistory(String cardNumber, OperationType type,
                                             BigDecimal amount, BigDecimal before, BigDecimal after, String refId, String description) {
        TransactionHistory history = new TransactionHistory();
        history.setCardNumber(cardNumber);
        history.setOperationType(type);
        history.setAmount(amount);
        history.setBalanceBefore(before);
        history.setBalanceAfter(after);
        history.setReferenceId(refId);
        history.setDescription(description != null ? description : getDefaultDescription(type, amount));
        history.setCreatedAt(LocalDateTime.now());
        return history;
    }

    private String getDefaultDescription(OperationType type, BigDecimal amount) {
        switch (type) {
            case EARN:
                return "Начисление бонусов: " + amount;
            case BURN:
                return "Списание бонусов: " + amount;
            case REFUND_EARN:
                return "Возврат начисленных бонусов: " + amount;
            case REFUND_BURN:
                return "Возврат списанных бонусов: " + amount;
            default:
                return "Операция с бонусами: " + amount;
        }
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

    private BonusCard createNewCard(String cardNumber) {
        log.info("Создаем новую карту с номером: {}", cardNumber);

        BonusCard newCard = new BonusCard();
        newCard.setCardNumber(cardNumber);
        newCard.setBalance(BigDecimal.ZERO);
        newCard.setCreatedAt(LocalDateTime.now());
        newCard.setUpdatedAt(LocalDateTime.now());
        // Если есть поле version, оно автоматически установится в 0

        BonusCard savedCard = bonusCardRepository.save(newCard);

        // Создаем приветственную транзакцию в истории
        TransactionHistory welcomeHistory = createHistory(
                savedCard.getCardNumber(),
                OperationType.EARN,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "CARD_CREATION",
                "Карта создана"
        );
        historyRepository.save(welcomeHistory);

        return savedCard;
    }
}
