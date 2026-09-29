package com.expensemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponse {
    private Long id;
    private String categoryName;
    private Long categoryId;
    private String categoryColor;
    private Double limitAmount;
    private Double spentAmount;
    private Double percentageUsed;
    private Integer month;
    private Integer year;
    private String status; // SAFE, WARNING, EXCEEDED
}
