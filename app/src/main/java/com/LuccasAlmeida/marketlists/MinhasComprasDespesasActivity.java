package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MinhasComprasDespesasActivity extends AppCompatActivity {

    private LinearLayout containerConteudo;
    private AutoCompleteTextView spinnerMes;
    private TabLayout tabLayout;
    private String mesSelecionado = "Todos os Meses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_compras_despesas);

        containerConteudo = findViewById(R.id.containerConteudoDespesas);
        spinnerMes = findViewById(R.id.spinnerMes);
        tabLayout = findViewById(R.id.tabLayoutDespesas);

        NavigationRailView navRail = findViewById(R.id.navRail);
        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_despesas);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(MinhasComprasDespesasActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(MinhasComprasDespesasActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(MinhasComprasDespesasActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        startActivity(new Intent(MinhasComprasDespesasActivity.this, EscanearNotaActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_despesas) {
                        return true;
                    }
                    return false;
                }
            });
        }

        // Configura o Seletor de Mês
        configurarSeletorMes();

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    carregarConteudoAba(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        // Carrega por padrão a primeira aba (Despesas)
        carregarConteudoAba(0);
    }

    private void configurarSeletorMes() {
        List<CompraFinalizada> todasCompras = ProdutoRepository.getInstance().getComprasFinalizadas();
        Set<String> mesesEncontrados = new HashSet<>();
        mesesEncontrados.add("Todos os Meses");

        for (CompraFinalizada compra : todasCompras) {
            String mesAno = extrairMesAno(compra.getData());
            if (!mesAno.isEmpty()) {
                mesesEncontrados.add(mesAno);
            }
        }

        List<String> opcoesMeses = new ArrayList<>(mesesEncontrados);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opcoesMeses);

        if (spinnerMes != null) {
            spinnerMes.setAdapter(adapter);
            spinnerMes.setText("Todos os Meses", false);
            spinnerMes.setOnItemClickListener((parent, view, position, id) -> {
                mesSelecionado = adapter.getItem(position);
                int abaAtiva = tabLayout != null ? tabLayout.getSelectedTabPosition() : 0;
                carregarConteudoAba(abaAtiva);
            });
        }
    }

    private String extrairMesAno(String dataStr) {
        if (dataStr == null || dataStr.isEmpty()) return "";
        if (dataStr.equalsIgnoreCase("Hoje")) return "Outubro/2026";
        String[] partes = dataStr.split("/");
        if (partes.length == 3) {
            String mesNum = partes[1];
            String ano = partes[2];
            String nomeMes = converterNumeroParaNomeMes(mesNum);
            return nomeMes + "/" + ano;
        }
        return dataStr;
    }

    private String converterNumeroParaNomeMes(String mesNum) {
        switch (mesNum) {
            case "01": case "1": return "Janeiro";
            case "02": case "2": return "Fevereiro";
            case "03": case "3": return "Março";
            case "04": case "4": return "Abril";
            case "05": case "5": return "Maio";
            case "06": case "6": return "Junho";
            case "07": case "7": return "Julho";
            case "08": case "8": return "Agosto";
            case "09": case "9": return "Setembro";
            case "10": return "Outubro";
            case "11": return "Novembro";
            case "12": return "Dezembro";
            default: return mesNum;
        }
    }

    private List<CompraFinalizada> getComprasFiltradasPorMes() {
        List<CompraFinalizada> todasCompras = ProdutoRepository.getInstance().getComprasFinalizadas();
        if (mesSelecionado == null || mesSelecionado.equalsIgnoreCase("Todos os Meses")) {
            return todasCompras;
        }

        List<CompraFinalizada> filtradas = new ArrayList<>();
        for (CompraFinalizada c : todasCompras) {
            if (extrairMesAno(c.getData()).equalsIgnoreCase(mesSelecionado)) {
                filtradas.add(c);
            }
        }
        return filtradas;
    }

    private void carregarConteudoAba(int posicao) {
        if (containerConteudo == null) return;
        containerConteudo.removeAllViews();

        if (posicao == 0) {
            exibirAbaDespesas();
        } else if (posicao == 1) {
            exibirAbaItens();
        } else if (posicao == 2) {
            exibirAbaMercados();
        }
    }

    private void exibirAbaDespesas() {
        List<CompraFinalizada> compras = getComprasFiltradasPorMes();
        double totalGasto = 0.0;
        for (CompraFinalizada c : compras) {
            totalGasto += c.getValorTotal();
        }
        double ticketMedio = compras.isEmpty() ? 0.0 : totalGasto / compras.size();

        // Cartão Resumo
        MaterialCardView cardResumo = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(16));
        cardResumo.setLayoutParams(params);
        cardResumo.setRadius(dpToPx(16));
        cardResumo.setCardElevation(dpToPx(3));

        LinearLayout layoutResumo = new LinearLayout(this);
        layoutResumo.setOrientation(LinearLayout.VERTICAL);
        layoutResumo.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        TextView txtTotalLabel = new TextView(this);
        txtTotalLabel.setText("Total Gasto (" + mesSelecionado + "):");
        txtTotalLabel.setTextSize(14);

        TextView txtTotalValor = new TextView(this);
        txtTotalValor.setText(String.format(Locale.getDefault(), "R$ %.2f", totalGasto));
        txtTotalValor.setTextSize(26);
        txtTotalValor.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtDetalhes = new TextView(this);
        txtDetalhes.setText(String.format(Locale.getDefault(), "%d listas finalizadas | Ticket Médio: R$ %.2f", compras.size(), ticketMedio));
        txtDetalhes.setTextSize(12);
        txtDetalhes.setTextColor(android.graphics.Color.parseColor("#666666"));

        layoutResumo.addView(txtTotalLabel);
        layoutResumo.addView(txtTotalValor);
        layoutResumo.addView(txtDetalhes);
        cardResumo.addView(layoutResumo);
        containerConteudo.addView(cardResumo);

        // Seção Histórico
        TextView txtHistoricoTitle = new TextView(this);
        txtHistoricoTitle.setText("Compras Finalizadas das Listas");
        txtHistoricoTitle.setTextSize(16);
        txtHistoricoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        txtHistoricoTitle.setPadding(0, dpToPx(8), 0, dpToPx(8));
        containerConteudo.addView(txtHistoricoTitle);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CompraFinalizada compra : compras) {
            View cardCompra = inflater.inflate(R.layout.item_produto, containerConteudo, false);

            ImageView img = cardCompra.findViewById(R.id.imgProduto);
            TextView txtNome = cardCompra.findViewById(R.id.txtNomeProduto);
            TextView txtPreco = cardCompra.findViewById(R.id.txtPrecoProduto);
            View btnAdd = cardCompra.findViewById(R.id.btnAdicionarProduto);

            if (btnAdd != null) btnAdd.setVisibility(View.GONE);

            if (txtNome != null) txtNome.setText(compra.getNomeLista() + " - " + compra.getEstabelecimento());
            if (txtPreco != null) txtPreco.setText(String.format(Locale.getDefault(), "R$ %.2f - %s", compra.getValorTotal(), compra.getData()));

            if (img != null) {
                img.setImageResource(android.R.drawable.ic_menu_agenda);
            }

            containerConteudo.addView(cardCompra);
        }
    }

    private void exibirAbaItens() {
        List<CompraFinalizada> comprasFiltradas = getComprasFiltradasPorMes();
        List<Produto> catalogo = ProdutoRepository.getInstance().getCatalogoProdutos();

        // Mapeia a quantidade de cada produto comprada nas listas finalizadas filtradas
        Map<Integer, Integer> qtdeNoMesPorProduto = new HashMap<>();
        for (CompraFinalizada compra : comprasFiltradas) {
            for (ItemListaCompras itemCompra : compra.getItensComprados()) {
                if (itemCompra.getProduto() != null) {
                    int idProd = itemCompra.getProduto().getId();
                    int qtdeAnterior = qtdeNoMesPorProduto.getOrDefault(idProd, 0);
                    qtdeNoMesPorProduto.put(idProd, qtdeAnterior + itemCompra.getQuantidade());
                }
            }
        }

        TextView txtTitle = new TextView(this);
        txtTitle.setText("Produtos Adquiridos (" + mesSelecionado + ")");
        txtTitle.setTextSize(16);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        txtTitle.setPadding(0, 0, 0, dpToPx(12));
        containerConteudo.addView(txtTitle);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Produto prod : catalogo) {
            View itemView = inflater.inflate(R.layout.item_produto, containerConteudo, false);

            ImageView img = itemView.findViewById(R.id.imgProduto);
            TextView txtNome = itemView.findViewById(R.id.txtNomeProduto);
            TextView txtPreco = itemView.findViewById(R.id.txtPrecoProduto);
            View btnAdd = itemView.findViewById(R.id.btnAdicionarProduto);

            if (btnAdd != null) btnAdd.setVisibility(View.GONE);

            int qtdeNoMes = qtdeNoMesPorProduto.getOrDefault(prod.getId(), 0);

            if (txtNome != null) txtNome.setText(prod.getNome());
            if (txtPreco != null) {
                txtPreco.setText(String.format(Locale.getDefault(), "R$ %.2f | Comprados no mês: %d un.", prod.getPreco(), qtdeNoMes));
            }

            if (img != null) {
                Glide.with(this)
                        .load(prod.getUrlImagem())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(img);
            }

            containerConteudo.addView(itemView);
        }
    }

    private void exibirAbaMercados() {
        List<CompraFinalizada> compras = getComprasFiltradasPorMes();
        Map<String, Double> gastosPorMercado = new HashMap<>();
        Map<String, Integer> visitasPorMercado = new HashMap<>();

        for (CompraFinalizada compra : compras) {
            String mercado = compra.getEstabelecimento();
            gastosPorMercado.put(mercado, gastosPorMercado.getOrDefault(mercado, 0.0) + compra.getValorTotal());
            visitasPorMercado.put(mercado, visitasPorMercado.getOrDefault(mercado, 0) + 1);
        }

        TextView txtTitle = new TextView(this);
        txtTitle.setText("Mercados e Listas (" + mesSelecionado + ")");
        txtTitle.setTextSize(16);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        txtTitle.setPadding(0, 0, 0, dpToPx(12));
        containerConteudo.addView(txtTitle);

        for (Map.Entry<String, Double> entry : gastosPorMercado.entrySet()) {
            String nomeMercado = entry.getKey();
            double totalMercado = entry.getValue();
            int visitas = visitasPorMercado.getOrDefault(nomeMercado, 1);

            MaterialCardView cardMercado = new MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(80)
            );
            params.setMargins(0, 0, 0, dpToPx(10));
            cardMercado.setLayoutParams(params);
            cardMercado.setRadius(dpToPx(16));
            cardMercado.setCardElevation(dpToPx(2));

            LinearLayout layoutInternal = new LinearLayout(this);
            layoutInternal.setOrientation(LinearLayout.HORIZONTAL);
            layoutInternal.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            layoutInternal.setGravity(android.view.Gravity.CENTER_VERTICAL);

            ImageView icon = new ImageView(this);
            icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28)));
            icon.setImageResource(android.R.drawable.ic_menu_myplaces);

            LinearLayout textLayout = new LinearLayout(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textParams.setMarginStart(dpToPx(12));
            textLayout.setLayoutParams(textParams);
            textLayout.setOrientation(LinearLayout.VERTICAL);

            TextView txtNomeM = new TextView(this);
            txtNomeM.setText(nomeMercado);
            txtNomeM.setTextSize(16);
            txtNomeM.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView txtSub = new TextView(this);
            txtSub.setText(String.format(Locale.getDefault(), "%d compra(s) | Total: R$ %.2f", visitas, totalMercado));
            txtSub.setTextColor(android.graphics.Color.parseColor("#666666"));
            txtSub.setTextSize(12);

            textLayout.addView(txtNomeM);
            textLayout.addView(txtSub);

            layoutInternal.addView(icon);
            layoutInternal.addView(textLayout);

            cardMercado.addView(layoutInternal);
            containerConteudo.addView(cardMercado);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}