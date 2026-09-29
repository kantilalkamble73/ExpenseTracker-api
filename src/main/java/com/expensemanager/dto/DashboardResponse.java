package com.expensemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Double totalSpentThisMonth;
    private Double totalSpentLastMonth;
    private Double monthlyIncome;
    private Double remainingBudget;
    private Double percentageChange;
    private Double savingsRate;
    private Double dailyAverage;
    private Double projectedMonthEnd;
    private Integer healthScore;
    private String healthLabel;
    private List<HealthFactor> healthBreakdown;
    private List<CategoryTotal> categoryBreakdown;
    private List<DailyTotal> dailyTrend;
    private List<MonthlyTotal> monthlyTrend;
    private List<TopExpense> topExpenses;
    private List<NameAmount> paymentModes;
    private List<String> smartInsights;
    private Long totalTransactions;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CategoryTotal { private String category; private Double amount; private Double percentage; }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class DailyTotal { private String date; private Double amount; }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyTotal { private String label; private Double amount; }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TopExpense { private String title; private String category; private String color; private Double amount; private String date; }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class NameAmount { private String name; private Double amount; }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class HealthFactor { private String label; private Integer score; private Integer max; }
}
