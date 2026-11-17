package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.LuccasAlmeida.marketlists.model.ListaCompra;
import com.LuccasAlmeida.marketlists.model.ListaCreateRequest;
import com.LuccasAlmeida.marketlists.model.LoginRequest;
import com.LuccasAlmeida.marketlists.model.LoginResponse;
import com.LuccasAlmeida.marketlists.model.UserResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editSenha;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBarLogin);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> realizarLogin());
        }

        TextView txtIrParaCadastro = findViewById(R.id.txtIrParaCadastro);
        if (txtIrParaCadastro != null) {
            txtIrParaCadastro.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    private void realizarLogin() {
        String email = editEmail != null ? editEmail.getText().toString().trim() : "";
        String senha = editSenha != null ? editSenha.getText().toString().trim() : "";

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha o e-mail e a senha", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        ApiService api = ApiClient.getApiService(this);
        LoginRequest request = new LoginRequest(email, senha);

        Log.d("API_LISTA", "POST /auth/login -> Efetuando login...");

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    Log.d("API_LISTA", "POST /auth/login -> Login efetuado com sucesso!");

                    // Salva a sessão de forma segura via Keystore no SessionManager
                    SessionManager.getInstance(LoginActivity.this)
                            .saveSession(body.getAccessToken(), body.getRefreshToken());

                    Log.d("API_LISTA", "LoginActivity -> Token salvo com sucesso no SessionManager (Tamanho: " + (body.getAccessToken() != null ? body.getAccessToken().length() : 0) + ")");

                    // Valida a identidade do usuário e carrega as listas
                    consultarUsuarioEMostrarListas();
                } else {
                    setLoading(false);
                    int code = response.code();
                    if (code == 401) {
                        Toast.makeText(LoginActivity.this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Erro no login: HTTP " + code, Toast.LENGTH_SHORT).show();
                    }
                    Log.e("API_LISTA", "POST /auth/login -> Erro HTTP " + code);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e("API_LISTA", "POST /auth/login -> Falha de conexão ou servidor offline", t);
                Toast.makeText(LoginActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void consultarUsuarioEMostrarListas() {
        ApiService api = ApiClient.getApiService(this);

        Log.d("API_LISTA", "GET /auth/me -> Verificando identidade do usuário autenticado...");

        api.getMe().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    Log.d("API_LISTA", "GET /auth/me -> Sucesso HTTP " + response.code() + ". Usuário autenticado: " + user.getEmail() + " | UUID: " + user.getId());
                } else {
                    Log.w("API_LISTA", "GET /auth/me -> Aviso HTTP " + response.code());
                }
                carregarOuCriarListaDoUsuario();
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "GET /auth/me -> Falha na comunicação", t);
                carregarOuCriarListaDoUsuario();
            }
        });
    }

    private void carregarOuCriarListaDoUsuario() {
        ApiService api = ApiClient.getApiService(this);

        Log.d("API_LISTA", "GET /listas -> Carregando listas do usuário autenticado...");

        api.obterListas().enqueue(new Callback<List<ListaCompra>>() {
            @Override
            public void onResponse(@NonNull Call<List<ListaCompra>> call, @NonNull Response<List<ListaCompra>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ListaCompra> listas = response.body();
                    Log.d("API_LISTA", "GET /listas -> Sucesso HTTP " + response.code() + ". Total de listas encontradas: " + listas.size());

                    if (!listas.isEmpty()) {
                        // Caso A: Usuário possui listas -> Seleciona a primeira lista existente
                        ListaCompra listaExistente = listas.get(0);
                        Log.d("API_LISTA", "GET /listas -> Selecionando lista existente ID: " + listaExistente.getId() + " | Nome: " + listaExistente.getNome());
                        abrirTelaDetalhesLista(listaExistente.getId(), listaExistente.getNome());
                    } else {
                        // Caso B: Coleção vazia -> Cria a lista padrão "Minha lista de compras"
                        criarListaPadrao();
                    }
                } else {
                    setLoading(false);
                    Log.e("API_LISTA", "GET /listas -> Erro HTTP " + response.code());
                    Toast.makeText(LoginActivity.this, "Erro ao carregar listas do usuário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ListaCompra>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e("API_LISTA", "GET /listas -> Falha ao carregar listas", t);
                Toast.makeText(LoginActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void criarListaPadrao() {
        ApiService api = ApiClient.getApiService(this);
        ListaCreateRequest request = new ListaCreateRequest("Minha lista de compras");

        Log.d("API_LISTA", "POST /listas -> Criando lista padrão 'Minha lista de compras'...");

        api.criarLista(request).enqueue(new Callback<ListaCompra>() {
            @Override
            public void onResponse(@NonNull Call<ListaCompra> call, @NonNull Response<ListaCompra> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ListaCompra novaLista = response.body();
                    Log.d("API_LISTA", "POST /listas -> Lista criada com sucesso! ID gerado no PostgreSQL: " + novaLista.getId());
                    abrirTelaDetalhesLista(novaLista.getId(), novaLista.getNome());
                } else {
                    setLoading(false);
                    Log.e("API_LISTA", "POST /listas -> Erro HTTP " + response.code());
                    Toast.makeText(LoginActivity.this, "Erro ao criar lista padrão", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListaCompra> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e("API_LISTA", "POST /listas -> Falha ao criar lista", t);
                Toast.makeText(LoginActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirTelaDetalhesLista(long realListaId, String nomeLista) {
        Intent intent = new Intent(LoginActivity.this, ListDetailsActivity.class);
        intent.putExtra(ListDetailsActivity.EXTRA_LISTA_ID, String.valueOf(realListaId));
        intent.putExtra(ListDetailsActivity.EXTRA_LISTA_NOME, nomeLista);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!loading);
        }
    }
}