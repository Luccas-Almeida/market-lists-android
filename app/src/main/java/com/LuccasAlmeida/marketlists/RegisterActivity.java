package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.LuccasAlmeida.marketlists.api.ApiClient;
import com.LuccasAlmeida.marketlists.api.ApiService;
import com.LuccasAlmeida.marketlists.api.SessionManager;
import com.LuccasAlmeida.marketlists.model.LoginResponse;
import com.LuccasAlmeida.marketlists.model.RegisterRequest;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editSenha;
    private EditText editConfirmarSenha;
    private MaterialButton btnRegistrar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editEmail = findViewById(R.id.editEmailRegister);
        editSenha = findViewById(R.id.editSenhaRegister);
        editConfirmarSenha = findViewById(R.id.editConfirmarSenhaRegister);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progressBar = findViewById(R.id.progressBarRegister);

        if (btnRegistrar != null) {
            btnRegistrar.setOnClickListener(v -> realizarCadastro());
        }

        TextView txtIrParaLogin = findViewById(R.id.txtIrParaLogin);
        if (txtIrParaLogin != null) {
            txtIrParaLogin.setOnClickListener(v -> {
                finish();
            });
        }
    }

    private void realizarCadastro() {
        String email = editEmail != null ? editEmail.getText().toString().trim() : "";
        String senha = editSenha != null ? editSenha.getText().toString().trim() : "";
        String confirmarSenha = editConfirmarSenha != null ? editConfirmarSenha.getText().toString().trim() : "";

        // 1. Validação de e-mail
        if (email.isEmpty()) {
            Toast.makeText(this, "O e-mail é obrigatório", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Informe um e-mail válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validação de senha
        if (senha.isEmpty()) {
            Toast.makeText(this, "A senha é obrigatória", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.length() < 8) {
            Toast.makeText(this, "A senha deve ter pelo menos 8 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validação de confirmação de senha
        if (confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Confirme sua senha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        ApiService api = ApiClient.getApiService(this);
        RegisterRequest request = new RegisterRequest(email, senha);

        Log.d("API_LISTA", "POST /auth/register -> Enviando requisição de cadastro...");

        api.register(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    Log.d("API_LISTA", "POST /auth/register -> Cadastro concluído com sucesso!");

                    if (body.getAccessToken() != null && !body.getAccessToken().isEmpty()) {
                        // Se o backend retornou sessão/token imediatamente
                        SessionManager.getInstance(RegisterActivity.this)
                                .saveSession(body.getAccessToken(), body.getRefreshToken());
                        Toast.makeText(RegisterActivity.this, "Conta criada e autenticada com sucesso!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Se o Supabase exigir confirmação de e-mail
                        Toast.makeText(RegisterActivity.this, "Conta criada. Verifique seu e-mail para confirmar o cadastro antes de entrar.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    int code = response.code();
                    String detalheErro = "";
                    try {
                        if (response.errorBody() != null) {
                            detalheErro = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("API_LISTA", "Erro ao ler resposta do cadastro", e);
                    }

                    Log.e("API_LISTA", "POST /auth/register -> Erro HTTP " + code + " | Resposta: " + detalheErro);

                    if (code == 400 || code == 409) {
                        Toast.makeText(RegisterActivity.this, "Este e-mail já está cadastrado ou inválido.", Toast.LENGTH_LONG).show();
                    } else if (code == 422) {
                        Toast.makeText(RegisterActivity.this, "Dados inválidos para o cadastro.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Erro ao criar conta: HTTP " + code, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e("API_LISTA", "POST /auth/register -> Falha de comunicação/servidor offline", t);
                Toast.makeText(RegisterActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnRegistrar != null) {
            btnRegistrar.setEnabled(!loading);
        }
    }
}