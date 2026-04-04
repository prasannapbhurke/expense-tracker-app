package com.example.expensetracker.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
                                        @RequestParam(defaultValue = "asc") String sortDir,
                                        @RequestParam(required = false) String range) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        if (range != null && !range.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime after = null;
            switch (range) {
                case "daily": after = now.minusDays(1); break;
                case "2days": after = now.minusDays(2); break;
                case "3days": after = now.minusDays(3); break;
                case "weekly": after = now.minusWeeks(1); break;
                case "monthly": after = now.minusMonths(1); break;
                case "yearly": after = now.minusYears(1); break;
            }
            if (after != null) {
                return expenseRepository.findByUserAndTimestampAfter(user, after, sort);
            }
        }
        
        return expenseRepository.findByUser(user, sort);
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
            expense.setCategory(expenseDetails.getCategory());
            return expenseRepository.save(expense);
        }
        return null;
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
