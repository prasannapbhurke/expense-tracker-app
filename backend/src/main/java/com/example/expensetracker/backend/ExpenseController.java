package com.example.expensetracker.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Expense> getAllExpenses(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam(defaultValue = "id") String sortBy,
                                        @RequestParam(defaultValue = "asc") String sortDir) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return expenseRepository.findByUser(user, Sort.by(direction, sortBy));
    }

    @PostMapping
    public Expense createExpense(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Expense expense) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    @PutMapping("/{id}")
    public Expense updateExpense(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody Expense expenseDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Expense expense = expenseRepository.findById(id).orElseThrow();
        if (expense.getUser().equals(user)) {
            expense.setName(expenseDetails.getName());
            expense.setAmount(expenseDetails.getAmount());
            return expenseRepository.save(expense);
        }
        return null; // Or throw an exception
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Expense expense = expenseRepository.findById(id).orElseThrow();
        if (expense.getUser().equals(user)) {
            expenseRepository.deleteById(id);
        }
    }
}
