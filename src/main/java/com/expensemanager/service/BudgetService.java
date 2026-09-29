package com.expensemanager.service;

import com.expensemanager.dto.BudgetRequest;
import com.expensemanager.dto.BudgetResponse;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.exception.BadRequestException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.BudgetRepository;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    private static final double WARNING_THRESHOLD = 0.80;

    public List<BudgetResponse> getBudgetsForMonth(Long userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream().map(b -> toResponse(userId, b)).collect(Collectors.toList());
    }

    public BudgetResponse createOrUpdateBudget(User user, BudgetRequest request) {
        if (request.getMonth() < 1 || request.getMonth() > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Budget budget = budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(user.getId(), request.getCategoryId(), request.getMonth(), request.getYear())
                .orElse(new Budget());

        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        budget.setCategory(category);
        budget.setUser(user);

        budget = budgetRepository.save(budget);
        return toResponse(user.getId(), budget);
    }

    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Long userId, Budget budget) {
        Double spent = expenseRepository.sumByUserCategoryAndMonth(
                userId, budget.getCategory().getId(), budget.getMonth(), budget.getYear());
        if (spent == null) spent = 0.0;

        double percentage = budget.getLimitAmount() > 0 ? (spent / budget.getLimitAmount()) * 100 : 0;

        String status = percentage >= 100 ? "EXCEEDED" : percentage >= WARNING_THRESHOLD * 100 ? "WARNING" : "SAFE";

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getName(),
                budget.getCategory().getId(),
                budget.getCategory().getColor(),
                budget.getLimitAmount(),
                spent,
                Math.round(percentage * 100.0) / 100.0,
                budget.getMonth(),
                budget.getYear(),
                status
        );
    }
}
