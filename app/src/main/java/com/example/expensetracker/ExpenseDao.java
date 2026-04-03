package com.example.expensetracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    LiveData<List<Expense>> getAllExpenses();

    @Query("SELECT * FROM expenses ORDER BY name ASC")
    LiveData<List<Expense>> getAllExpensesSortedByName();

    @Query("SELECT * FROM expenses ORDER BY amount ASC")
    LiveData<List<Expense>> getAllExpensesSortedByAmount();
}
