package com.expensemanager.service;

import com.expensemanager.dto.DashboardResponse;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import com.expensemanager.repository.BudgetRepository;
import com.expensemanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the dashboard summary along with a small, fully explainable
 * rule-based "smart" layer:
 *  - month-over-month trend detection
 *  - category concentration detection
 *  - savings-rate benchmarking
 *  - budget breach detection
 *  - a 0-100 Financial Health Score built from 4 weighted factors
 *  - a simple linear month-end spend projection
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public DashboardResponse getDashboard(User user, Integer month, Integer year) {
        Long userId = user.getId();

        YearMonth current = YearMonth.of(year, month);
        YearMonth previous = current.minusMonths(1);
        LocalDate start = current.atDay(1);
        LocalDate end = current.atEndOfMonth();

        double currentTotal = nz(expenseRepository.sumByUserAndMonth(userId, month, year));
        double previousTotal = nz(expenseRepository.sumByUserAndMonth(userId, previous.getMonthValue(), previous.getYear()));

        double percentageChange = previousTotal > 0
                ? ((currentTotal - previousTotal) / previousTotal) * 100
                : (currentTotal > 0 ? 100.0 : 0.0);

        double income = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : 0.0;
        double remainingBudget = income - currentTotal;
        double savingsRate = income > 0 ? ((income - currentTotal) / income) * 100 : 0.0;

        boolean isCurrentMonth = YearMonth.now().equals(current);
        int daysElapsed = isCurrentMonth ? LocalDate.now().getDayOfMonth() : current.lengthOfMonth();
        double dailyAverage = daysElapsed > 0 ? currentTotal / daysElapsed : 0.0;
        double projectedMonthEnd = isCurrentMonth ? dailyAverage * current.lengthOfMonth() : currentTotal;

        List<DashboardResponse.CategoryTotal> categoryBreakdown = new ArrayList<>();
        double finalCurrentTotal = currentTotal;
        expenseRepository.categoryWiseTotals(userId, month, year).forEach(row -> {
            String name = (String) row[0];
            double amount = ((Number) row[1]).doubleValue();
            double pct = finalCurrentTotal > 0 ? (amount / finalCurrentTotal) * 100 : 0;
            categoryBreakdown.add(new DashboardResponse.CategoryTotal(name, amount, round2(pct)));
        });
        categoryBreakdown.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        List<DashboardResponse.DailyTotal> dailyTrend = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd MMM");
        expenseRepository.dailyTotals(userId, start, end).forEach(row -> {
            LocalDate d = ((java.sql.Date) row[0]).toLocalDate();
            double amount = ((Number) row[1]).doubleValue();
            dailyTrend.add(new DashboardResponse.DailyTotal(d.format(dayFmt), amount));
        });

        List<DashboardResponse.MonthlyTotal> monthlyTrend = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM");
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            double total = nz(expenseRepository.sumByUserAndMonth(userId, ym.getMonthValue(), ym.getYear()));
            monthlyTrend.add(new DashboardResponse.MonthlyTotal(ym.format(monthFmt), round2(total)));
        }

        List<DashboardResponse.TopExpense> topExpenses = new ArrayList<>();
        for (Expense e : expenseRepository.findTop5ByUserIdAndExpenseDateBetweenOrderByAmountDesc(userId, start, end)) {
            topExpenses.add(new DashboardResponse.TopExpense(
                    e.getTitle() == null || e.getTitle().isBlank() ? "Untitled" : e.getTitle(),
                    e.getCategory().getName(),
                    e.getCategory().getColor(),
                    e.getAmount(),
                    e.getExpenseDate().toString()
            ));
        }

        List<DashboardResponse.NameAmount> paymentModes = new ArrayList<>();
        expenseRepository.paymentModeTotals(userId, start, end).forEach(row ->
                paymentModes.add(new DashboardResponse.NameAmount(row[0].toString(), ((Number) row[1]).doubleValue())));

        long totalTransactions = expenseRepository.countByUserIdAndExpenseDateBetween(userId, start, end);

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        List<DashboardResponse.HealthFactor> healthBreakdown = new ArrayList<>();

        int savingsScore = income > 0
                ? (int) Math.max(0, Math.min(40, (savingsRate / 20.0) * 40))
                : (currentTotal == 0 ? 40 : 20);
        healthBreakdown.add(new DashboardResponse.HealthFactor("Savings Rate", savingsScore, 40));

        int budgetScore;
        if (budgets.isEmpty()) {
            budgetScore = 20;
        } else {
            long withinLimit = budgets.stream().filter(b -> {
                double spent = nz(expenseRepository.sumByUserCategoryAndMonth(userId, b.getCategory().getId(), month, year));
                return spent <= b.getLimitAmount();
            }).count();
            budgetScore = (int) Math.round((withinLimit / (double) budgets.size()) * 30);
        }
        healthBreakdown.add(new DashboardResponse.HealthFactor("Budget Adherence", budgetScore, 30));

        int consistencyScore = previousTotal == 0 ? 15
                : (int) Math.max(0, 15 - Math.min(15, Math.abs(percentageChange) / 5));
        healthBreakdown.add(new DashboardResponse.HealthFactor("Spending Consistency", consistencyScore, 15));

        double topShare = categoryBreakdown.isEmpty() ? 0 : categoryBreakdown.get(0).getPercentage();
        int diversityScore = topShare <= 40 ? 15 : (int) Math.max(0, 15 - ((topShare - 40) / 4));
        healthBreakdown.add(new DashboardResponse.HealthFactor("Spending Diversity", diversityScore, 15));

        int healthScore = savingsScore + budgetScore + consistencyScore + diversityScore;
        String healthLabel = healthScore >= 80 ? "Excellent" : healthScore >= 60 ? "Good" : healthScore >= 40 ? "Fair" : "Needs Attention";

        List<String> insights = buildSmartInsights(percentageChange, previousTotal, categoryBreakdown, income,
                savingsRate, currentTotal, budgets, userId, month, year, projectedMonthEnd, income);

        return DashboardResponse.builder()
                .totalSpentThisMonth(round2(currentTotal))
                .totalSpentLastMonth(round2(previousTotal))
                .monthlyIncome(income)
                .remainingBudget(round2(remainingBudget))
                .percentageChange(round2(percentageChange))
                .savingsRate(round2(savingsRate))
                .dailyAverage(round2(dailyAverage))
                .projectedMonthEnd(round2(projectedMonthEnd))
                .healthScore(healthScore)
                .healthLabel(healthLabel)
                .healthBreakdown(healthBreakdown)
                .categoryBreakdown(categoryBreakdown)
                .dailyTrend(dailyTrend)
                .monthlyTrend(monthlyTrend)
                .topExpenses(topExpenses)
                .paymentModes(paymentModes)
                .smartInsights(insights)
                .totalTransactions(totalTransactions)
                .build();
    }

    private List<String> buildSmartInsights(double percentageChange, double previousTotal,
                                             List<DashboardResponse.CategoryTotal> categoryBreakdown,
                                             double income, double savingsRate, double currentTotal,
                                             List<Budget> budgets, Long userId, Integer month, Integer year,
                                             double projectedMonthEnd, double incomeForProjection) {
        List<String> insights = new ArrayList<>();

        if (previousTotal > 0) {
            if (percentageChange > 15) {
                insights.add(String.format(
                        "Your spending increased by %.1f%% compared to last month. Consider reviewing discretionary expenses.",
                        percentageChange));
            } else if (percentageChange < -15) {
                insights.add(String.format("Great job! You spent %.1f%% less than last month.", Math.abs(percentageChange)));
            } else {
                insights.add("Your spending is fairly consistent with last month.");
            }
        }

        if (!categoryBreakdown.isEmpty()) {
            DashboardResponse.CategoryTotal top = categoryBreakdown.get(0);
            if (top.getPercentage() > 40) {
                insights.add(String.format(
                        "%s accounts for %.0f%% of your spending this month \u2014 your biggest expense area.",
                        top.getCategory(), top.getPercentage()));
            }
        }

        if (income > 0) {
            if (savingsRate < 0) {
                insights.add("You have spent more than your monthly income. Your budget is in deficit.");
            } else if (savingsRate < 20) {
                insights.add(String.format(
                        "You are saving only %.0f%% of your income this month. Financial experts recommend at least 20%%.",
                        savingsRate));
            } else {
                insights.add(String.format("You are on track, saving %.0f%% of your income this month.", savingsRate));
            }

            boolean currentMonth = YearMonth.now().equals(YearMonth.of(year, month));
            if (currentMonth && projectedMonthEnd > incomeForProjection && incomeForProjection > 0) {
                insights.add(String.format(
                        "At your current pace, you're projected to spend \u20b9%.0f by month-end \u2014 that would exceed your income.",
                        projectedMonthEnd));
            }
        }

        for (Budget budget : budgets) {
            double spent = nz(expenseRepository.sumByUserCategoryAndMonth(userId, budget.getCategory().getId(), month, year));
            if (budget.getLimitAmount() > 0 && spent >= budget.getLimitAmount()) {
                insights.add(String.format("You have exceeded your %s budget of \u20b9%.0f for this month.",
                        budget.getCategory().getName(), budget.getLimitAmount()));
            }
        }

        if (insights.isEmpty()) {
            insights.add("Not enough data yet \u2014 keep tracking your expenses to unlock smart insights.");
        }
        return insights;
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
