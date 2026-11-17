package com.LuccasAlmeida.marketlists.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://market-lists-api.onrender.com/";
    private static Retrofit retrofitWithAuth;
    private static Retrofit retrofitWithoutAuth;

    public static synchronized ApiService getApiService(Context context) {
        if (context != null) {
            if (retrofitWithAuth == null) {
                OkHttpClient clientWithAuth = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(90, TimeUnit.SECONDS)
                        .writeTimeout(90, TimeUnit.SECONDS)
                        .callTimeout(120, TimeUnit.SECONDS)
                        .addInterceptor(new AuthInterceptor(context.getApplicationContext()))
                        .build();

                retrofitWithAuth = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(clientWithAuth)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofitWithAuth.create(ApiService.class);
        }

        if (retrofitWithoutAuth == null) {
            OkHttpClient clientWithoutAuth = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(90, TimeUnit.SECONDS)
                    .callTimeout(120, TimeUnit.SECONDS)
                    .build();

            retrofitWithoutAuth = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(clientWithoutAuth)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitWithoutAuth.create(ApiService.class);
    }

    public static ApiService getApiService() {
        return getApiService(null);
    }
}