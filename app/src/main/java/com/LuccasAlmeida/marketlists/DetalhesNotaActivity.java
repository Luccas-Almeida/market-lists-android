package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.Locale;

public class DetalhesNotaActivity extends AppCompatActivity {

    public static final String EXTRA_NOTA_ID = "extra_nota_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_nota);

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        NavigationRailView navRail = findViewById(R.id.navRail);
        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_escanear);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(DetalhesNotaActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(DetalhesNotaActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(DetalhesNotaActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        startActivity(new Intent(DetalhesNotaActivity.this, EscanearNotaActivity.class));
                        finish();
                        return true;
                    }
                    return false;
                }
            });
        }

        String idNota = getIntent().getStringExtra(EXTRA_NOTA_ID);
        NotaFiscal nota = ProdutoRepository.getInstance().buscarNotaPorId(idNota != null ? idNota : "NFCe-001");

        if (nota == null && !ProdutoRepository.getInstance().getNotasSalvas().isEmpty()) {
            nota = ProdutoRepository.getInstance().getNotasSalvas().get(0);
        }

        exibirDetalhesNota(nota);
    }

    private void exibirDetalhesNota(NotaFiscal nota) {
        if (nota == null) return;

        TextView txtLojaNota = findViewById(R.id.txtLojaNota);
        TextView txtDataNota = findViewById(R.id.txtDataNota);
        TextView txtTotalNota = findViewById(R.id.txtTotalNota);
        LinearLayout containerItens = findViewById(R.id.containerItensNota);
        MaterialButton btnVerSefazWeb = findViewById(R.id.btnVerSefazWeb);

        if (txtLojaNota != null) {
            txtLojaNota.setText(nota.getEstabelecimento());
        }
        if (txtDataNota != null) {
            txtDataNota.setText(String.format("Data: %s", nota.getData()));
        }
        if (txtTotalNota != null) {
            txtTotalNota.setText(String.format(Locale.getDefault(), "R$ %.2f", nota.getValorTotal()));
        }

        if (btnVerSefazWeb != null) {
            final String urlSefaz = nota.getUrlSefaz();
            btnVerSefazWeb.setOnClickListener(v -> {
                if (urlSefaz != null && !urlSefaz.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlSefaz));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(this, "URL da SEFAZ não disponível", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (containerItens != null) {
            containerItens.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (Produto prod : nota.getItens()) {
                View itemView = inflater.inflate(R.layout.item_produto, containerItens, false);

                ImageView img = itemView.findViewById(R.id.imgProduto);
                TextView txtNome = itemView.findViewById(R.id.txtNomeProduto);
                TextView txtPreco = itemView.findViewById(R.id.txtPrecoProduto);
                MaterialButton btnAdd = itemView.findViewById(R.id.btnAdicionarProduto);

                if (txtNome != null) {
                    txtNome.setText(prod.getNome());
                }
                if (txtPreco != null) {
                    txtPreco.setText(String.format(Locale.getDefault(), "R$ %.2f", prod.getPreco()));
                }

                if (img != null) {
                    Glide.with(this)
                            .load(prod.getUrlImagem())
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(img);
                }

                if (btnAdd != null) {
                    btnAdd.setOnClickListener(v -> Toast.makeText(DetalhesNotaActivity.this, prod.getNome() + " adicionado ao seu catálogo!", Toast.LENGTH_SHORT).show());
                }

                containerItens.addView(itemView);
            }
        }
    }
}