package com.expensemanager.controller;

import com.expensemanager.dto.BudgetRequest;
import com.expensemanager.dto.BudgetResponse;
import com.expensemanager.security.CustomUserDetails;
import com.expensemanager.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(budgetService.getBudgetsForMonth(principal.getUserId(), month, year));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createOrUpdateBudget(@AuthenticationPrincipal CustomUserDetails principal,
                                                                 @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(principal.getUser(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal CustomUserDetails principal,
                                              @PathVariable Long id) {
        budgetService.deleteBudget(principal.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
