package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.LuccasAlmeida.marketlists.api.ApiClient;
import com.LuccasAlmeida.marketlists.api.ApiService;
import com.LuccasAlmeida.marketlists.api.SessionManager;
import com.LuccasAlmeida.marketlists.model.ListaCompra;
import com.LuccasAlmeida.marketlists.model.ListaCreateRequest;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MinhasListasActivity extends AppCompatActivity {

    private LinearLayout containerListas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_listas);

        containerListas = findViewById(R.id.containerListasCadastradas);

        NavigationRailView navRail = findViewById(R.id.navRail);

        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_listas);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(MinhasListasActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        startActivity(new Intent(MinhasListasActivity.this, MinhasComprasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(MinhasListasActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        Toast.makeText(MinhasListasActivity.this, "Leitura de notas fiscais — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_despesas) {
                        Toast.makeText(MinhasListasActivity.this, "Relatórios de despesas — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return false;
                }
            });
        }

        MaterialCardView cardCriarLista = findViewById(R.id.cardCriarLista);
        if (cardCriarLista != null) {
            cardCriarLista.setOnClickListener(v -> exibirDialogCriarLista());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarListasCadastradas();
    }

    private void carregarListasCadastradas() {
        if (containerListas == null) return;

        ApiService api = ApiClient.getApiService(this);
        Log.d("API_LISTA", "GET /listas -> Solicitando listas oficiais do usuário no Supabase...");

        api.obterListas().enqueue(new Callback<List<ListaCompra>>() {
            @Override
            public void onResponse(@NonNull Call<List<ListaCompra>> call, @NonNull Response<List<ListaCompra>> response) {
                containerListas.removeAllViews();
                if (response.isSuccessful() && response.body() != null) {
                    List<ListaCompra> listas = response.body();
                    Log.d("API_LISTA", "GET /listas -> Sucesso HTTP " + response.code() + ". Total de listas no Supabase: " + listas.size());

                    if (listas.isEmpty()) {
                        TextView txtVazio = new TextView(MinhasListasActivity.this);
                        txtVazio.setText("Nenhuma lista encontrada no Supabase.\nClique em 'Criar Lista' acima para cadastrar!");
                        txtVazio.setPadding(16, 24, 16, 16);
                        txtVazio.setTextSize(15);
                        txtVazio.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                        containerListas.addView(txtVazio);
                        return;
                    }

                    for (ListaCompra lista : listas) {
                        MaterialCardView card = criarCardParaLista(lista);
                        containerListas.addView(card);
                    }
                } else {
                    tratarErroHttp(response, "GET /listas");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ListaCompra>> call, @NonNull Throwable t) {
                if (containerListas != null) containerListas.removeAllViews();
                Log.e("API_LISTA", "GET /listas -> Falha de comunicação/servidor offline", t);
                Toast.makeText(MinhasListasActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exibirDialogCriarLista() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Criar Nova Lista no Supabase");

        final EditText input = new EditText(this);
        input.setHint("Nome da lista (ex: Compras da Semana)");
        input.setPadding(32, 32, 32, 32);
        builder.setView(input);

        builder.setPositiveButton("Criar", (dialog, which) -> {
            String nomeLista = input.getText().toString().trim();
            if (!nomeLista.isEmpty()) {
                criarListaNoSupabase(nomeLista);
            } else {
                Toast.makeText(MinhasListasActivity.this, "Digite o nome da lista", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void criarListaNoSupabase(String nomeLista) {
        ApiService api = ApiClient.getApiService(this);
        ListaCreateRequest request = new ListaCreateRequest(nomeLista);

        Log.d("API_LISTA", "POST /listas -> Criando lista '" + nomeLista + "' no Supabase...");

        api.criarLista(request).enqueue(new Callback<ListaCompra>() {
            @Override
            public void onResponse(@NonNull Call<ListaCompra> call, @NonNull Response<ListaCompra> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ListaCompra novaLista = response.body();
                    Log.d("API_LISTA", "POST /listas -> Sucesso HTTP " + response.code() + ". Lista criada no PostgreSQL ID: " + novaLista.getId());
                    Toast.makeText(MinhasListasActivity.this, "Lista '" + novaLista.getNome() + "' salva no Supabase!", Toast.LENGTH_SHORT).show();
                    carregarListasCadastradas();
                    abrirDetalhesLista(novaLista.getId(), novaLista.getNome());
                } else {
                    tratarErroHttp(response, "POST /listas");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListaCompra> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "POST /listas -> Falha ao criar lista", t);
                Toast.makeText(MinhasListasActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MaterialCardView criarCardParaLista(ListaCompra lista) {
        MaterialCardView novoCard = new MaterialCardView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(80)
        );
        layoutParams.setMargins(0, 0, 0, dpToPx(10));
        novoCard.setLayoutParams(layoutParams);
        novoCard.setRadius(dpToPx(16));
        novoCard.setCardElevation(dpToPx(2));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        contentLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        android.widget.ImageView icon = new android.widget.ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(android.R.drawable.ic_menu_agenda);

        LinearLayout textLayout = new LinearLayout(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textParams.setMarginStart(dpToPx(12));
        textLayout.setLayoutParams(textParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView txtTitle = new TextView(this);
        txtTitle.setText(lista.getNome());
        txtTitle.setTextSize(16);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtSubtitle = new TextView(this);
        txtSubtitle.setText("ID Supabase: " + lista.getId());
        txtSubtitle.setTextColor(android.graphics.Color.parseColor("#666666"));
        txtSubtitle.setTextSize(12);

        textLayout.addView(txtTitle);
        textLayout.addView(txtSubtitle);

        contentLayout.addView(icon);
        contentLayout.addView(textLayout);

        novoCard.addView(contentLayout);

        novoCard.setOnClickListener(v -> abrirDetalhesLista(lista.getId(), lista.getNome()));

        return novoCard;
    }

    private void abrirDetalhesLista(long realListaId, String nomeLista) {
        Intent intent = new Intent(MinhasListasActivity.this, ListDetailsActivity.class);
        intent.putExtra(ListDetailsActivity.EXTRA_LISTA_ID, String.valueOf(realListaId));
        intent.putExtra(ListDetailsActivity.EXTRA_LISTA_NOME, nomeLista);
        startActivity(intent);
    }

    private void tratarErroHttp(Response<?> response, String endpointInfo) {
        int code = response.code();
        String mensagemErro;
        String detalheValidacao = "";

        try {
            if (response.errorBody() != null) {
                detalheValidacao = response.errorBody().string();
            }
        } catch (IOException e) {
            Log.e("API_LISTA", "Erro ao ler corpo de erro HTTP", e);
        }

        if (code == 401) {
            mensagemErro = "Sessão expirada. Faça login novamente.";
            Log.e("API_LISTA", endpointInfo + " -> HTTP 401 Unauthorized. Redirecionando para login...");
            SessionManager.getInstance(this).clearSession();
            Intent intent = new Intent(MinhasListasActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        } else if (code == 404) {
            mensagemErro = "Recurso não encontrado no servidor.";
        } else if (code == 422) {
            mensagemErro = "Dados inválidos para a requisição.";
            if (!detalheValidacao.isEmpty()) {
                Log.e("API_LISTA", endpointInfo + " -> Detalhes de Validação 422: " + detalheValidacao);
            }
        } else if (code == 503) {
            mensagemErro = "Servidor indisponível ou falha no banco de dados.";
        } else {
            mensagemErro = "Erro na requisição: HTTP " + code;
        }

        Toast.makeText(this, mensagemErro, Toast.LENGTH_SHORT).show();
        Log.e("API_LISTA", endpointInfo + " -> Erro HTTP " + code + " | Resposta: " + detalheValidacao);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}