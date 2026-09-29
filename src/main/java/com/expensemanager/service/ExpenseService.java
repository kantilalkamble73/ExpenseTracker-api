package com.expensemanager.service;

import com.expensemanager.dto.ExpenseRequest;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public List<Expense> getAllExpenses(Long userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId);
    }

    public List<Expense> getExpensesInRange(Long userId, LocalDate start, LocalDate end) {
        return expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, start, end);
    }

    public Expense createExpense(User user, ExpenseRequest request) {
        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMode(request.getPaymentMode());
        expense.setCategory(category);
        expense.setUser(user);

        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Long userId, Long expenseId, ExpenseRequest request) {
        Expense expense = getOwnedExpense(userId, expenseId);
        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        expense.setAmount(request.getAmount());
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMode(request.getPaymentMode());
        expense.setCategory(category);

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long userId, Long expenseId) {
        Expense expense = getOwnedExpense(userId, expenseId);
        expenseRepository.delete(expense);
    }

    /**
     * Smart category suggestion: looks at the user's past expenses whose title
     * is similar to the one being typed and returns the category most
     * frequently used for it (a lightweight, explainable recommender).
     */
    public Optional<Long> suggestCategory(Long userId, String title) {
        if (title == null || title.trim().length() < 3) {
            return Optional.empty();
        }
        List<Long> matches = expenseRepository.findCategoryIdsByTitle(userId, title.trim(), PageRequest.of(0, 1));
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    private Expense getOwnedExpense(Long userId, Long expenseId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }
}
