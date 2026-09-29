package com.expensemanager.repository;

import com.expensemanager.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    List<RecurringExpense> findByUserIdOrderByNextDueDateAsc(Long userId);
    Optional<RecurringExpense> findByIdAndUserId(Long id, Long userId);
    List<RecurringExpense> findByActiveTrueAndNextDueDateLessThanEqual(LocalDate date);
    boolean existsByCategoryId(Long categoryId);
}
