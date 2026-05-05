package com.bonus.controller;

import com.bonus.dto.*;
import com.bonus.service.BonusService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
public class BonusController {

    private final BonusService bonusService;

    @PostMapping("/earn")
    @Operation(summary = "Начислить бонусы по карте клиента")
    public ResponseEntity<TransactionResponse> earnBonuses(@Valid @RequestBody EarnRequest request) {
        return ResponseEntity.ok(bonusService.earnBonuses(request));
    }

    @PostMapping("/burn")
    @Operation(summary = "Списать бонусы по карте клиента")
    public ResponseEntity<TransactionResponse> burnBonuses(@Valid @RequestBody BurnRequest request) {
        return ResponseEntity.ok(bonusService.burnBonuses(request));
    }

    @PostMapping("/refund")
    @Operation(summary = "Возврат бонусов (при возврате товара)")
    public ResponseEntity<TransactionResponse> refundBonuses(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(bonusService.refundBonuses(request));
    }

    @GetMapping("/balance/{cardNumber}")
    @Operation(summary = "Получить баланс по номеру карты")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String cardNumber) {
        return ResponseEntity.ok(bonusService.getBalance(cardNumber));
    }

    @GetMapping("/history/{cardNumber}")
    @Operation(summary = "Получить историю операций по номеру карты")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable String cardNumber) {
        return ResponseEntity.ok(bonusService.getHistory(cardNumber));
    }
}