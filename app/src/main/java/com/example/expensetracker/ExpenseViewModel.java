package com.example.expensetracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.remote.ApiService;
import com.example.expensetracker.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseViewModel extends AndroidViewModel {
    private ApiService apiService;
    private MutableLiveData<List<Expense>> allExpenses = new MutableLiveData<>();

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getClient(application).create(ApiService.class);
        loadExpenses();
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }

    public void loadExpenses() {
        apiService.getAllExpenses().enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful()) {
                    allExpenses.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    public void insert(Expense expense) {
        apiService.createExpense(expense).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful()) {
                    loadExpenses();
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                // Handle failure
            }
        });
    }

    public void update(Expense expense) {
        apiService.updateExpense(expense.getId(), expense).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful()) {
                    loadExpenses();
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                // Handle failure
            }
        });
    }

    public void delete(Expense expense) {
        apiService.deleteExpense(expense.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadExpenses();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle failure
            }
        });
    }
}
