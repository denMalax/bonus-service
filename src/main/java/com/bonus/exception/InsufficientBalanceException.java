package com.bonus.exception;

import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class InsufficientBalanceException extends RuntimeException {

    private final String cardNumber;
    private final BigDecimal requestedAmount;
    private final BigDecimal currentBalance;

    public InsufficientBalanceException(String message) {
        super(message);
        this.cardNumber = null;
        this.requestedAmount = null;
        this.currentBalance = null;
    }

    public InsufficientBalanceException(String cardNumber, BigDecimal requestedAmount, BigDecimal currentBalance) {
        super(String.format("Недостаточно бонусов на карте %s. Запрошено: %s, доступно: %s",
                cardNumber, requestedAmount, currentBalance));
        this.cardNumber = cardNumber;
        this.requestedAmount = requestedAmount;
        this.currentBalance = currentBalance;
    }
}