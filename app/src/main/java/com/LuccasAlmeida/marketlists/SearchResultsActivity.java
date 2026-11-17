package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

public class SearchResultsActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_query";
    private LinearLayout containerResultados;
    private TextView txtTituloBusca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        String query = getIntent().getStringExtra(EXTRA_QUERY);

        containerResultados = findViewById(R.id.containerResultadosBusca);
        txtTituloBusca = findViewById(R.id.txtTituloBusca);

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        NavigationRailView navRail = findViewById(R.id.navRail);
        if (navRail != null) {
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(SearchResultsActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(SearchResultsActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        Toast.makeText(SearchResultsActivity.this, "Histórico de compras — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(SearchResultsActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        Toast.makeText(SearchResultsActivity.this, "Leitura de notas fiscais — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_despesas) {
                        Toast.makeText(SearchResultsActivity.this, "Relatórios de despesas — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return false;
                }
            });
        }

        if (txtTituloBusca != null) {
            txtTituloBusca.setText("Pesquisa de produtos — Em breve");
        }

        if (containerResultados != null) {
            containerResultados.removeAllViews();
            TextView txtAviso = new TextView(this);
            txtAviso.setText("A funcionalidade de pesquisa avançada estará disponível em breve.");
            txtAviso.setPadding(16, 32, 16, 16);
            txtAviso.setTextSize(16);
            txtAviso.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            containerResultados.addView(txtAviso);
        }
    }
}