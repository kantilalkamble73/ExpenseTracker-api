package com.expensemanager.dto;

import com.expensemanager.entity.Expense;
import com.expensemanager.entity.RecurringExpense;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecurringRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotNull
    private Long categoryId;

    @NotNull
    private RecurringExpense.Frequency frequency;

    @NotNull
    private LocalDate startDate;

    private Expense.PaymentMode paymentMode = Expense.PaymentMode.CASH;
}
