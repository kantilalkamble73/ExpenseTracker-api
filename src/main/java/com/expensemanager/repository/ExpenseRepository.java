package com.expensemanager.repository;

import com.expensemanager.entity.Expense;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @EntityGraph(attributePaths = "category")
    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    @EntityGraph(attributePaths = "category")
    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(Long userId, LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = "category")
    List<Expense> findTop5ByUserIdAndExpenseDateBetweenOrderByAmountDesc(Long userId, LocalDate start, LocalDate end);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndExpenseDateBetween(Long userId, LocalDate start, LocalDate end);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND e.category.id = :categoryId AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    Double sumByUserCategoryAndMonth(@Param("userId") Long userId,
                                      @Param("categoryId") Long categoryId,
                                      @Param("month") Integer month,
                                      @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    Double sumByUserAndMonth(@Param("userId") Long userId,
                             @Param("month") Integer month,
                             @Param("year") Integer year);

    @Query("SELECT e.category.name, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year " +
           "GROUP BY e.category.name")
    List<Object[]> categoryWiseTotals(@Param("userId") Long userId,
                                       @Param("month") Integer month,
                                       @Param("year") Integer year);

    @Query("SELECT e.paymentMode, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end GROUP BY e.paymentMode")
    List<Object[]> paymentModeTotals(@Param("userId") Long userId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

    @Query(value = "SELECT expense_date, COALESCE(SUM(amount), 0) " +
           "FROM expenses WHERE user_id = :userId AND expense_date BETWEEN :start AND :end " +
           "GROUP BY expense_date ORDER BY expense_date", nativeQuery = true)
    List<Object[]> dailyTotals(@Param("userId") Long userId,
                                @Param("start") LocalDate start,
                                @Param("end") LocalDate end);

    /** Smart category suggestion: category the user most often used for a similar title. */
    @Query("SELECT e.category.id FROM Expense e WHERE e.user.id = :userId " +
           "AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "GROUP BY e.category.id ORDER BY COUNT(e) DESC")
    List<Long> findCategoryIdsByTitle(@Param("userId") Long userId,
                                       @Param("title") String title,
                                       Pageable pageable);
}
