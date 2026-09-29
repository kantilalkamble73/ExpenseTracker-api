package com.expensemanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BudgetRequest {
    @NotNull
    @Positive
    private Double limitAmount;

    @NotNull
    private Integer month;

    @NotNull
    private Integer year;

    @NotNull
    private Long categoryId;
}
