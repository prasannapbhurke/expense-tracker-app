package com.example.expensetracker.remote;

import android.content.Context;
import com.example.expensetracker.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    // Pointing to your NEW LIVE Render server
    private static final String BASE_URL = "https://expense-tracker-app-wbxo.onrender.com/";

    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            
            // Handle Session ID (Cookies) for Login
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    String sessionId = sessionManager.getSessionId();
                    if (sessionId != null) {
                        requestBuilder.addHeader("Cookie", sessionId);
                    }
                    return chain.proceed(requestBuilder.build());
                }
            });
            
            httpClient.addInterceptor(new ReceivedCookiesInterceptor(sessionManager));

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

class ReceivedCookiesInterceptor implements Interceptor {
    private SessionManager sessionManager;

    public ReceivedCookiesInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        if (originalResponse.headers("Set-Cookie") != null && !originalResponse.headers("Set-Cookie").isEmpty()) {
            // Store the session cookie so we stay logged in
            String sessionId = originalResponse.header("Set-Cookie");
            sessionManager.saveSessionId(sessionId);
        }
        return originalResponse;
    }
}
