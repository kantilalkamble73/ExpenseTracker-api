package com.expensemanager.service;

import com.expensemanager.dto.CategoryRequest;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.exception.BadRequestException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.BudgetRepository;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.RecurringExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;

    public List<Category> getCategoriesForUser(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    public Category createCategory(User user, CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long userId, Long categoryId, CategoryRequest request) {
        Category category = getOwnedCategory(userId, categoryId);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = getOwnedCategory(userId, categoryId);

        if (expenseRepository.existsByCategoryId(categoryId)) {
            throw new BadRequestException(
                    "This category has expenses linked to it. Reassign or delete those expenses first.");
        }
        if (recurringExpenseRepository.existsByCategoryId(categoryId)) {
            throw new BadRequestException(
                    "This category is used by a recurring expense. Remove that recurring expense first.");
        }

        budgetRepository.deleteByCategoryId(categoryId);
        categoryRepository.delete(category);
    }

    private Category getOwnedCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
