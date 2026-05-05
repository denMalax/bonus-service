package com.bonus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundRequest {
    @NotBlank(message = "ID исходной транзакции обязателен")
    private String originalTransactionId; // ID операции, которую нужно откатить
}