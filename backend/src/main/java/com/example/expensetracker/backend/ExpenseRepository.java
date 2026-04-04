package com.example.expensetracker.backend;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user, Sort sort);
    List<Expense> findByUserAndTimestampAfter(User user, LocalDateTime timestamp, Sort sort);
}
