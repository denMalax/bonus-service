package com.bonus.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BurnRequest {
    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Номер карты должен содержать 10-20 цифр")
    private String cardNumber;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @DecimalMax(value = "1000000", message = "Сумма не может превышать 1,000,000")
    private BigDecimal amount;

    private String orderId;

    private String description;
}