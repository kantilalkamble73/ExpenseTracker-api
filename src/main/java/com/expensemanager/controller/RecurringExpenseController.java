package com.expensemanager.controller;

import com.expensemanager.dto.RecurringRequest;
import com.expensemanager.entity.RecurringExpense;
import com.expensemanager.security.CustomUserDetails;
import com.expensemanager.service.RecurringExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    @GetMapping
    public ResponseEntity<List<RecurringExpense>> getAll(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(recurringExpenseService.getAll(principal.getUserId()));
    }

    @PostMapping
    public ResponseEntity<RecurringExpense> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @Valid @RequestBody RecurringRequest request) {
        return ResponseEntity.ok(recurringExpenseService.create(principal.getUser(), request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringExpense> toggle(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(recurringExpenseService.toggleActive(principal.getUserId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        recurringExpenseService.delete(principal.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
