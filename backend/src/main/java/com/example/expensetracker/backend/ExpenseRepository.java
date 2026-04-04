package com.example.expensetracker.backend;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user, Sort sort);
    List<Expense> findByUserAndTimestampGreaterThanEqual(User user, Long timestamp, Sort sort);
}
