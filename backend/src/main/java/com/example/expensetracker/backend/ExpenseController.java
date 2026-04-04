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
                                        @RequestParam(defaultValue = "asc") String sortDir,
                                        @RequestParam(required = false) String range) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        if (range != null && !range.isEmpty()) {
            long now = System.currentTimeMillis();
            long after = 0;
            long dayMillis = 24 * 60 * 60 * 1000L;
            
            switch (range) {
                case "daily": after = now - dayMillis; break;
                case "2days": after = now - (2 * dayMillis); break;
                case "3days": after = now - (3 * dayMillis); break;
                case "weekly": after = now - (7 * dayMillis); break;
                case "monthly": after = now - (30 * dayMillis); break;
                case "yearly": after = now - (365 * dayMillis); break;
            }
            if (after != 0) {
                return expenseRepository.findByUserAndTimestampGreaterThanEqual(user, after, sort);
            }
        }
        
        return expenseRepository.findByUser(user, sort);
    }

    @PostMapping
    public Expense createExpense(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Expense expense) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        expense.setUser(user);
        if (expense.getTimestamp() == null) {
            expense.setTimestamp(System.currentTimeMillis());
        }
        return expenseRepository.save(expense);
    }

    @PutMapping("/{id}")
    public Expense updateExpense(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody Expense expenseDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Expense expense = expenseRepository.findById(id).orElseThrow();
        if (expense.getUser().getId().equals(user.getId())) {
            expense.setName(expenseDetails.getName());
            expense.setAmount(expenseDetails.getAmount());
            expense.setCategory(expenseDetails.getCategory());
            expense.setTimestamp(expenseDetails.getTimestamp());
            return expenseRepository.save(expense);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Expense expense = expenseRepository.findById(id).orElseThrow();
        if (expense.getUser().getId().equals(user.getId())) {
            expenseRepository.deleteById(id);
        }
    }
}
