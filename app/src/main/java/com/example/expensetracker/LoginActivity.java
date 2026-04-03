package com.example.expensetracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.remote.ApiService;
import com.example.expensetracker.remote.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);

        apiService = RetrofitClient.getClient(this).create(ApiService.class);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            login(username, password);
        });

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login(String username, String password) {
        apiService.login(username, password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class MainActivity extends AppCompatActivity {

        private RecyclerView expenseList;
        private ExpenseAdapter adapter;
        private ExpenseViewModel expenseViewModel;
        private List<Expense> expenses = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            expenseList = findViewById(R.id.expense_list);
            expenseList.setLayoutManager(new LinearLayoutManager(this));
            registerForContextMenu(expenseList);

            adapter = new ExpenseAdapter((ArrayList<Expense>) expenses);
            expenseList.setAdapter(adapter);

            expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
            expenseViewModel.getAllExpenses().observe(this, newExpenses -> {
                expenses.clear();
                expenses.addAll(newExpenses);
                adapter.notifyDataSetChanged();
            });

            FloatingActionButton addExpenseButton = findViewById(R.id.add_expense_button);
            addExpenseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddExpenseDialog(null);
                }
            });
        }

        // Removed onCreateOptionsMenu and onOptionsItemSelected as sorting is now handled by the backend (or not implemented yet)

        @Override
        public boolean onContextItemSelected(@NonNull MenuItem item) {
            int position = adapter.getLongClickedPosition();
            if (position == -1) {
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

            final EditText expenseNameInput = view.findViewById(R.id.expense_name_input);
            final EditText expenseAmountInput = view.findViewById(R.id.expense_amount_input);

            if (expense != null) {
                expenseNameInput.setText(expense.getName());
                expenseAmountInput.setText(String.valueOf(expense.getAmount()));
            }

            builder.setPositiveButton(expense == null ? "Add" : "Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = expenseNameInput.getText().toString();
                    String amountStr = expenseAmountInput.getText().toString();

                    if (!name.isEmpty() && !amountStr.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            if (expense == null) { // Add new
                                expenseViewModel.insert(new Expense(name, amount));
                            } else { // Edit existing
                                expense.setName(name);
                                expense.setAmount(amount);
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
}
