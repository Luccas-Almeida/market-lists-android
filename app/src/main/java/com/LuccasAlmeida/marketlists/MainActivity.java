package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.LuccasAlmeida.marketlists.api.ApiClient;
import com.LuccasAlmeida.marketlists.api.ApiService;
import com.LuccasAlmeida.marketlists.api.SessionManager;
import com.LuccasAlmeida.marketlists.model.HealthResponse;
import com.LuccasAlmeida.marketlists.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_KEY_ONBOARDING = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Teste de conectividade direta com o endpoint /health do Render
        executarHealthCheckApi();

        // 1. Verifica se já existe uma sessão criptografada salva no Keystore
        SessionManager sessionManager = SessionManager.getInstance(this);

        if (sessionManager.isLoggedIn()) {
            Log.d("API_LISTA", "MainActivity -> Sessão salva encontrada. Validando via GET /auth/me...");
            validarSessaoEAbrirApp();
            return;
        }

        // 2. Se não houver sessão, verifica se o onboarding já foi visto
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean onboardingConcluido = prefs.getBoolean(PREF_KEY_ONBOARDING, false);

        if (onboardingConcluido) {
            Log.d("API_LISTA", "MainActivity -> Sem sessão salva, onboarding já concluído. Abrindo LoginActivity...");
            abrirLoginActivity();
            return;
        }

        // 3. Primeiro acesso: Configuração do botão "Next" para ir à SlideActivity
        TextView tv_next = findViewById(R.id.tv_next);
        if (tv_next != null) {
            tv_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prefs.edit().putBoolean(PREF_KEY_ONBOARDING, true).apply();
                    startActivity(new Intent(MainActivity.this, SlideActivity.class));
                }
            });
        }
    }

    private void executarHealthCheckApi() {
        ApiService api = ApiClient.getApiService(this);
        Log.d("API_LISTA", "GET https://market-lists-api.onrender.com/health -> Testando conectividade com o Render...");

        api.testarHealth().enqueue(new Callback<HealthResponse>() {
            @Override
            public void onResponse(@NonNull Call<HealthResponse> call, @NonNull Response<HealthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_LISTA", "GET /health -> Sucesso HTTP " + response.code() + " | Status Render: " + response.body().getStatus());
                } else {
                    Log.e("API_LISTA", "GET /health -> Erro HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<HealthResponse> call, @NonNull Throwable t) {
                String exClass = t.getClass().getSimpleName();
                String exMsg = t.getMessage();
                String exCause = t.getCause() != null ? t.getCause().getMessage() : "sem causa registrada";
                Log.e("API_LISTA", "GET /health -> onFailure! Exceção: " + exClass + " | Mensagem: " + exMsg + " | Causa: " + exCause, t);
            }
        });
    }

    private void validarSessaoEAbrirApp() {
        ApiService api = ApiClient.getApiService(this);

        api.getMe().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    Log.d("API_LISTA", "MainActivity -> GET /auth/me Sucesso HTTP 200. Usuário autenticado: " + user.getEmail() + ". Redirecionando para HomeActivity...");
                    abrirHomeActivity();
                } else if (response.code() == 401) {
                    Log.e("API_LISTA", "MainActivity -> GET /auth/me HTTP 401 Unauthorized. Sessão expirada/inválida. Limpando Keystore...");
                    SessionManager.getInstance(MainActivity.this).clearSession();
                    Toast.makeText(MainActivity.this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
                    abrirLoginActivity();
                } else {
                    Log.w("API_LISTA", "MainActivity -> GET /auth/me HTTP " + response.code() + ". Mantendo acesso com sessão salva...");
                    abrirHomeActivity();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                String exClass = t.getClass().getSimpleName();
                String exMsg = t.getMessage();
                Log.w("API_LISTA", "MainActivity -> GET /auth/me onFailure (" + exClass + ": " + exMsg + "). Mantendo sessão para acesso offline.", t);
                abrirHomeActivity();
            }
        });
    }

    private void abrirHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void abrirLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}