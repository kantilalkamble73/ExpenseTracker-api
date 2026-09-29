package com.expensemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @PositiveOrZero(message = "Monthly income cannot be negative")
    private Double monthlyIncome;
}
