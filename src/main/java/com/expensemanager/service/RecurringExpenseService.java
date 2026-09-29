package com.expensemanager.service;

import com.expensemanager.dto.RecurringRequest;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.RecurringExpense;
import com.expensemanager.entity.User;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.RecurringExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private static final Logger log = LoggerFactory.getLogger(RecurringExpenseService.class);

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public List<RecurringExpense> getAll(Long userId) {
        return recurringExpenseRepository.findByUserIdOrderByNextDueDateAsc(userId);
    }

    public RecurringExpense create(User user, RecurringRequest request) {
        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        RecurringExpense r = new RecurringExpense();
        r.setTitle(request.getTitle());
        r.setAmount(request.getAmount());
        r.setCategory(category);
        r.setFrequency(request.getFrequency());
        r.setNextDueDate(request.getStartDate());
        r.setPaymentMode(request.getPaymentMode());
        r.setUser(user);
        r.setActive(true);
        return recurringExpenseRepository.save(r);
    }

    public RecurringExpense toggleActive(Long userId, Long id) {
        RecurringExpense r = recurringExpenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring expense not found"));
        r.setActive(!r.isActive());
        return recurringExpenseRepository.save(r);
    }

    public void delete(Long userId, Long id) {
        RecurringExpense r = recurringExpenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring expense not found"));
        recurringExpenseRepository.delete(r);
    }

    /**
     * Runs once a day. Any active recurring template whose nextDueDate has
     * arrived is materialised into a real Expense row, and nextDueDate is
     * rolled forward according to its frequency.
     */
    @Scheduled(cron = "0 5 0 * * *") // 00:05 every day
    public void processDueRecurringExpenses() {
        List<RecurringExpense> due = recurringExpenseRepository.findByActiveTrueAndNextDueDateLessThanEqual(LocalDate.now());
        for (RecurringExpense r : due) {
            Expense expense = new Expense();
            expense.setAmount(r.getAmount());
            expense.setTitle(r.getTitle() + " (auto)");
            expense.setDescription("Auto-generated from recurring expense");
            expense.setExpenseDate(r.getNextDueDate());
            expense.setPaymentMode(r.getPaymentMode());
            expense.setCategory(r.getCategory());
            expense.setUser(r.getUser());
            expenseRepository.save(expense);

            r.setNextDueDate(nextDate(r.getNextDueDate(), r.getFrequency()));
            recurringExpenseRepository.save(r);
            log.info("Generated recurring expense '{}' for user {}", r.getTitle(), r.getUser().getId());
        }
    }

    private LocalDate nextDate(LocalDate from, RecurringExpense.Frequency frequency) {
        return switch (frequency) {
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case YEARLY -> from.plusYears(1);
        };
    }
}
