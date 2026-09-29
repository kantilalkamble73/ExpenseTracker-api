package com.expensemanager.controller;

import com.expensemanager.dto.ExpenseRequest;
import com.expensemanager.entity.Expense;
import com.expensemanager.security.CustomUserDetails;
import com.expensemanager.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        if (start != null && end != null) {
            return ResponseEntity.ok(expenseService.getExpensesInRange(principal.getUserId(), start, end));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses(principal.getUserId()));
    }

    @GetMapping("/suggest-category")
    public ResponseEntity<Long> suggestCategory(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @RequestParam String title) {
        return ResponseEntity.ok(expenseService.suggestCategory(principal.getUserId(), title).orElse(null));
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.createExpense(principal.getUser(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@AuthenticationPrincipal CustomUserDetails principal,
                                               @PathVariable Long id) {
        expenseService.deleteExpense(principal.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
