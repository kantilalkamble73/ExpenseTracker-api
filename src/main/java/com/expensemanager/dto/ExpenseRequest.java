package com.expensemanager.dto;

import com.expensemanager.entity.Expense;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseRequest {
    @NotNull
    @Positive
    private Double amount;

    private String title;
    private String description;

    @NotNull
    private LocalDate expenseDate;

    private Expense.PaymentMode paymentMode = Expense.PaymentMode.CASH;

    @NotNull
    private Long categoryId;
}
