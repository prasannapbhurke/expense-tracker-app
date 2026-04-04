package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView expenseList;
    private ExpenseAdapter adapter;
    private ExpenseViewModel expenseViewModel;
    private List<Expense> expenses = new ArrayList<>();
    private final String[] CATEGORIES = {"Food", "Transport", "Shopping", "Bills", "Other"};
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            expenseList = findViewById(R.id.expense_list);
            if (expenseList != null) {
                expenseList.setLayoutManager(new LinearLayoutManager(this));
                registerForContextMenu(expenseList);

                adapter = new ExpenseAdapter((ArrayList<Expense>) expenses);
                expenseList.setAdapter(adapter);
            }

            expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
            expenseViewModel.getAllExpenses().observe(this, newExpenses -> {
                if (newExpenses != null) {
                    expenses.clear();
                    expenses.addAll(newExpenses);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });

            FloatingActionButton addExpenseButton = findViewById(R.id.add_expense_button);
            if (addExpenseButton != null) {
                addExpenseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddExpenseDialog(null);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (adapter == null) return super.onContextItemSelected(item);
        
        int position = adapter.getLongClickedPosition();
        if (position == -1 || position >= expenses.size()) {
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case 1: // Edit
                showAddExpenseDialog(expenses.get(position));
                break;
            case 2: // Delete
                showDeleteConfirmationDialog(expenses.get(position));
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showAddExpenseDialog(final Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(expense == null ? "Add Expense" : "Edit Expense");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        builder.setView(view);

        final TextInputEditText expenseNameInput = view.findViewById(R.id.expense_name_input);
        final TextInputEditText expenseAmountInput = view.findViewById(R.id.expense_amount_input);
        final AutoCompleteTextView categoryInput = view.findViewById(R.id.expense_category_input);
        final TextInputEditText dateInput = view.findViewById(R.id.expense_date_input);

        // Set up category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        categoryInput.setAdapter(categoryAdapter);

        // Date Picker logic
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (expense != null && expense.getTimestamp() != null) {
            try {
                // Assuming timestamp is stored as a string or long. Let's use current time as fallback
                dateInput.setText(sdf.format(Long.parseLong(expense.getTimestamp())));
            } catch (Exception e) {
                dateInput.setText(sdf.format(calendar.getTime()));
            }
        } else {
            dateInput.setText(sdf.format(calendar.getTime()));
        }

        dateInput.setOnClickListener(v -> {
            new DatePickerDialog(MainActivity.this, (view1, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateInput.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (expense != null) {
            expenseNameInput.setText(expense.getName());
            expenseAmountInput.setText(String.valueOf(expense.getAmount()));
            categoryInput.setText(expense.getCategory(), false);
        }

        builder.setPositiveButton(expense == null ? "Add" : "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = expenseNameInput.getText().toString();
                String amountStr = expenseAmountInput.getText().toString();
                String category = categoryInput.getText().toString();
                String timestamp = String.valueOf(calendar.getTimeInMillis());

                if (!name.isEmpty() && !amountStr.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (expense == null) { // Add new
                            Expense newExpense = new Expense(name, amount, category);
                            newExpense.setTimestamp(timestamp);
                            expenseViewModel.insert(newExpense);
                        } else { // Edit existing
                            expense.setName(name);
                            expense.setAmount(amount);
                            expense.setCategory(category);
                            expense.setTimestamp(timestamp);
                            expenseViewModel.update(expense);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter both name and amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showDeleteConfirmationDialog(final Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expenseViewModel.delete(expense);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
