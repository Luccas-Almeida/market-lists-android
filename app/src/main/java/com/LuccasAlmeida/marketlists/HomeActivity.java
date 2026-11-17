package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Vincular componentes do XML com as variáveis Java
        ImageView imgDestaque1 = findViewById(R.id.imgDestaque1);
        TextView txtDestaque1 = findViewById(R.id.txtDestaque1);
        MaterialCardView cardDestaque1 = findViewById(R.id.cardDestaque1);

        ImageView imgDestaque2 = findViewById(R.id.imgDestaque2);
        TextView txtDestaque2 = findViewById(R.id.txtDestaque2);
        MaterialCardView cardDestaque2 = findViewById(R.id.cardDestaque2);

        EditText editPesquisa = findViewById(R.id.editPesquisa);
        if (editPesquisa != null) {
            editPesquisa.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(HomeActivity.this, "Pesquisa de produtos — Em breve", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }

        NavigationRailView navRail = findViewById(R.id.navRail);

        // Configurar navegação lateral
        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_home);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(HomeActivity.this, MinhasListasActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        startActivity(new Intent(HomeActivity.this, MinhasComprasActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(HomeActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        Toast.makeText(HomeActivity.this, "Leitura de notas fiscais — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_despesas) {
                        Toast.makeText(HomeActivity.this, "Relatórios de despesas — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return false;
                }
            });
        }

        // Títulos e imagens dos cartões superiores
        String urlCloudinary1 = "https://res.cloudinary.com/zxzzyu6h/image/upload/v1793109571/minhas_listas.jpg";
        String titulo1 = "Minhas Listas";

        String urlCloudinary2 = "https://res.cloudinary.com/zxzzyu6h/image/upload/v1793109572/escanear_nota.jpg";
        String titulo2 = "Escanear Nota";

        if (txtDestaque1 != null) {
            txtDestaque1.setText(titulo1);
        }
        if (txtDestaque2 != null) {
            txtDestaque2.setText(titulo2);
        }

        // Carregar as imagens usando o Glide
        if (imgDestaque1 != null) {
            Glide.with(this)
                    .load(urlCloudinary1)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgDestaque1);
        }

        if (imgDestaque2 != null) {
            Glide.with(this)
                    .load(urlCloudinary2)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgDestaque2);
        }

        // Eventos de clique nos cartões superiores
        if (cardDestaque1 != null) {
            cardDestaque1.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MinhasListasActivity.class)));
        }

        if (cardDestaque2 != null) {
            cardDestaque2.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Leitura de notas fiscais — Em breve", Toast.LENGTH_SHORT).show());
        }

        // Preenchimento dinamico das categorias cadastradas
        carregarCategoriasNoGrid(CategoryData.getCategoriasPadrao());
    }

    private void carregarCategoriasNoGrid(List<Categoria> lista) {
        GridLayout gridCategorias = findViewById(R.id.gridCategorias);
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
                Glide.with(this)
                        .load(cat.getUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(img);
            }

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CategoryProductsActivity.class);
                intent.putExtra(CategoryProductsActivity.EXTRA_CATEGORIA_ID, cat.getId());
                intent.putExtra(CategoryProductsActivity.EXTRA_CATEGORIA_NOME, cat.getNome());
                startActivity(intent);
            });

            gridCategorias.addView(cardView);
        }
    }
}