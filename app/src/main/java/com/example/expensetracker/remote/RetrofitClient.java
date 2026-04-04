package com.example.expensetracker.remote;

import android.content.Context;
import android.util.Log;
import com.example.expensetracker.SessionManager;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://expense-tracker-app-wbxo.onrender.com/"; 

    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // Increased for Render wake-up
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);
            
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    String sessionId = sessionManager.getSessionId();
                    if (sessionId != null) {
                        Log.d("Network", "Sending Cookie: " + sessionId);
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
        
        // Specifically look for JSESSIONID cookie
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            for (String header : originalResponse.headers("Set-Cookie")) {
                if (header.contains("JSESSIONID")) {
                    Log.d("Network", "Saving Session Cookie: " + header);
                    sessionManager.saveSessionId(header);
                }
            }
        }
        return originalResponse;
    }
}
