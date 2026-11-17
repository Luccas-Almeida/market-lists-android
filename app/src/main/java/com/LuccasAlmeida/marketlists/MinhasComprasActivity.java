package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.List;
import java.util.Locale;

public class MinhasComprasActivity extends AppCompatActivity {

    private LinearLayout containerCompras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_compras);

        containerCompras = findViewById(R.id.containerMinhasCompras);

        NavigationRailView navRail = findViewById(R.id.navRail);
        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_compras);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(MinhasComprasActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(MinhasComprasActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        return true;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(MinhasComprasActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        startActivity(new Intent(MinhasComprasActivity.this, EscanearNotaActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_despesas) {
                        startActivity(new Intent(MinhasComprasActivity.this, MinhasComprasDespesasActivity.class));
                        finish();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarMinhasCompras();
    }

    private void carregarMinhasCompras() {
        if (containerCompras == null) return;
        containerCompras.removeAllViews();

        List<CompraFinalizada> compras = ProdutoRepository.getInstance().getComprasFinalizadas();

        if (compras.isEmpty()) {
            TextView txtVazio = new TextView(this);
            txtVazio.setText("Nenhuma compra finalizada ainda.\nFinalize uma lista na aba 'Minhas Listas'!");
            txtVazio.setPadding(16, 32, 16, 16);
            txtVazio.setTextSize(16);
            txtVazio.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            containerCompras.addView(txtVazio);
            return;
        }

        for (CompraFinalizada compra : compras) {
            MaterialCardView card = criarCardParaCompra(compra);
            containerCompras.addView(card);
        }
    }

    private MaterialCardView criarCardParaCompra(CompraFinalizada compra) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(90)
        );
        layoutParams.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(layoutParams);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(2));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        contentLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32)));
        icon.setImageResource(android.R.drawable.ic_menu_myplaces);

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
        txtTitle.setText(compra.getNomeLista() + " - " + compra.getEstabelecimento());
        txtTitle.setTextSize(16);
        txtTitle.setTypeface(null, Typeface.BOLD);

        TextView txtSubtitle = new TextView(this);
        txtSubtitle.setText(String.format(Locale.getDefault(), "R$ %.2f - %s (%d itens)", compra.getValorTotal(), compra.getData(), compra.getItensComprados().size()));
        txtSubtitle.setTextColor(Color.parseColor("#666666"));
        txtSubtitle.setTextSize(13);

        textLayout.addView(txtTitle);
        textLayout.addView(txtSubtitle);

        ImageButton btnExcluir = new ImageButton(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        btnExcluir.setLayoutParams(btnParams);
        btnExcluir.setImageResource(android.R.drawable.ic_menu_delete);
        
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true);
        btnExcluir.setBackgroundResource(typedValue.resourceId);
        btnExcluir.setContentDescription("Excluir Compra");

        btnExcluir.setOnClickListener(v -> exibirDialogConfirmarExclusao(compra));

        contentLayout.addView(icon);
        contentLayout.addView(textLayout);
        contentLayout.addView(btnExcluir);

        card.addView(contentLayout);

        return card;
    }

    private void exibirDialogConfirmarExclusao(CompraFinalizada compra) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Excluir Compra");
        builder.setMessage("Deseja realmente excluir a compra '" + compra.getNomeLista() + "'? O valor será removido das estatísticas de Despesas.");
        builder.setPositiveButton("Excluir", (dialog, which) -> {
            boolean removido = ProdutoRepository.getInstance().removerCompraFinalizada(compra.getId());
            if (removido) {
                carregarMinhasCompras();
                Toast.makeText(MinhasComprasActivity.this, "Compra excluída com sucesso!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}