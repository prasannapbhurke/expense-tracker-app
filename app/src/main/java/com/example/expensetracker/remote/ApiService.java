package com.example.expensetracker.remote;

import com.example.expensetracker.Expense;
import com.example.expensetracker.backend.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/expenses")
    Call<List<Expense>> getAllExpenses();

    @POST("api/expenses")
    Call<Expense> createExpense(@Body Expense expense);

    @PUT("api/expenses/{id}")
    Call<Expense> updateExpense(@Path("id") Long id, @Body Expense expense);

    @DELETE("api/expenses/{id}")
    Call<Void> deleteExpense(@Path("id") Long id);

    @POST("api/users/register")
    Call<Void> registerUser(@Body User user);

    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> login(@Field("username") String username, @Field("password") String password);
}
