package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.LuccasAlmeida.marketlists.api.ApiClient;
import com.LuccasAlmeida.marketlists.api.ApiService;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesActivity extends AppCompatActivity {

    private String listaIdStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        listaIdStr = getIntent().getStringExtra(ListDetailsActivity.EXTRA_LISTA_ID);

        NavigationRailView navRail = findViewById(R.id.navRail);

        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_categorias);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(CategoriesActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(CategoriesActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        Toast.makeText(CategoriesActivity.this, "Histórico de compras — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_categorias) {
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        Toast.makeText(CategoriesActivity.this, "Leitura de notas fiscais — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_despesas) {
                        Toast.makeText(CategoriesActivity.this, "Relatórios de despesas — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return false;
                }
            });
        }

        carregarCategoriasDaApi();
    }

    private void carregarCategoriasDaApi() {
        ApiService api = ApiClient.getApiService(this);
        Log.d("API_LISTA", "GET /categorias -> Carregando categorias oficiais do Supabase...");

        api.obterCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(@NonNull Call<List<Categoria>> call, @NonNull Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Categoria> listaApi = response.body();
                    Log.d("API_LISTA", "GET /categorias -> Sucesso HTTP " + response.code() + ". Total de categorias: " + listaApi.size());
                    carregarCategoriasNoGrid(listaApi);
                } else {
                    tratarErroHttp(response, "GET /categorias");
                    // Fallback para as categorias padrao em caso de falha de rede
                    carregarCategoriasNoGrid(CategoryData.getCategoriasPadrao());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Categoria>> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "GET /categorias -> Falha de comunicação/servidor offline", t);
                Toast.makeText(CategoriesActivity.this, "Falha ao carregar categorias do servidor", Toast.LENGTH_SHORT).show();
                carregarCategoriasNoGrid(CategoryData.getCategoriasPadrao());
            }
        });
    }

    private void carregarCategoriasNoGrid(List<Categoria> lista) {
        GridLayout gridCategorias = findViewById(R.id.gridTodasCategorias);
        if (gridCategorias == null) return;

        gridCategorias.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Categoria cat : lista) {
            View cardView = inflater.inflate(R.layout.item_categoria, gridCategorias, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            cardView.setLayoutParams(params);

            ImageView img = cardView.findViewById(R.id.imgCategoria);
            TextView txt = cardView.findViewById(R.id.txtNomeCategoria);

            if (txt != null) {
                txt.setText(cat.getNome());
            }

            if (img != null) {
                String imgUrl = (cat.getUrl() != null && !cat.getUrl().isEmpty()) ? cat.getUrl() : "https://res.cloudinary.com/zxzzyu6h/image/upload/v1792699479/mercearia.jpg";
                Glide.with(this)
                        .load(imgUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(img);
            }

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(CategoriesActivity.this, CategoryProductsActivity.class);
                intent.putExtra(CategoryProductsActivity.EXTRA_CATEGORIA_ID, cat.getId());
                intent.putExtra(CategoryProductsActivity.EXTRA_CATEGORIA_NOME, cat.getNome());
                intent.putExtra(CategoryProductsActivity.EXTRA_CATEGORIA_SLUG, cat.getSlug());
                if (listaIdStr != null) {
                    intent.putExtra(ListDetailsActivity.EXTRA_LISTA_ID, listaIdStr);
                }
                startActivity(intent);
            });

            gridCategorias.addView(cardView);
        }
    }

    private void tratarErroHttp(Response<?> response, String endpointInfo) {
        int code = response.code();
        Log.e("API_LISTA", endpointInfo + " -> Erro HTTP " + code);
        try {
            if (response.errorBody() != null) {
                Log.e("API_LISTA", response.errorBody().string());
            }
        } catch (IOException e) {
            Log.e("API_LISTA", "Erro ao ler resposta de erro HTTP", e);
        }
    }
}