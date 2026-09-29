package com.expensemanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A recurring expense template (rent, subscriptions, EMI...). A scheduler
 * turns it into real Expense rows whenever nextDueDate arrives.
 */
@Entity
@Table(name = "recurring_expenses")
@Getter
@Setter
@NoArgsConstructor
public class RecurringExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private Frequency frequency = Frequency.MONTHLY;

    private LocalDate nextDueDate;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private Expense.PaymentMode paymentMode = Expense.PaymentMode.CASH;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
}
