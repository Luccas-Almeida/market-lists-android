package com.LuccasAlmeida.marketlists.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    public interface OnSessionExpiredListener {
        void onSessionExpired();
    }

    private static OnSessionExpiredListener sessionExpiredListener;

    public static void setOnSessionExpiredListener(OnSessionExpiredListener listener) {
        sessionExpiredListener = listener;
    }

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();

        Request.Builder builder = originalRequest.newBuilder();

        // Não anexa token para rotas públicas de login, cadastro e health check
        boolean isPublicRoute = path.contains("/auth/login") || path.contains("/auth/register") || path.contains("/health");

        if (!isPublicRoute) {
            String token = SessionManager.getInstance(context).getAccessToken();
            if (token != null && !token.trim().isEmpty()) {
                String tokenLimpo = token.trim();
                builder.header("Authorization", "Bearer " + tokenLimpo);
                Log.d("API_LISTA", "AuthInterceptor -> [" + originalRequest.method() + " " + path + "] Cabeçalho Authorization anexado com sucesso (Tamanho do Token: " + tokenLimpo.length() + ")");
            } else {
                Log.w("API_LISTA", "AuthInterceptor -> [" + originalRequest.method() + " " + path + "] AVISO: Nenhum token encontrado no SessionManager para esta requisição protegida!");
            }
        }

        Request request = builder.build();
        Response response = chain.proceed(request);

        // Se retornar HTTP 401 em rota protegida, invalida a sessão
        if (response.code() == 401 && !isPublicRoute) {
            Log.e("API_LISTA", "AuthInterceptor -> HTTP 401 Unauthorized recebido no endpoint " + path + ". Limpando sessão...");
            SessionManager.getInstance(context).clearSession();

            if (sessionExpiredListener != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (sessionExpiredListener != null) {
                        sessionExpiredListener.onSessionExpired();
                    }
                });
            }
        }

        return response;
    }
}