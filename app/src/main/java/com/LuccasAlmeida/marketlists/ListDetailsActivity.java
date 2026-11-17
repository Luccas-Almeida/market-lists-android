package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.LuccasAlmeida.marketlists.api.ApiClient;
import com.LuccasAlmeida.marketlists.api.ApiService;
import com.LuccasAlmeida.marketlists.api.SessionManager;
import com.LuccasAlmeida.marketlists.model.DeleteItemResponse;
import com.LuccasAlmeida.marketlists.model.ItemCreateRequest;
import com.LuccasAlmeida.marketlists.model.ItemLista;
import com.LuccasAlmeida.marketlists.model.ItemUpdateRequest;
import com.LuccasAlmeida.marketlists.model.ResumoListaResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_LISTA_ID = "extra_lista_id";
    public static final String EXTRA_LISTA_NOME = "extra_lista_nome";

    private ListaDeCompras listaDeCompras;
    private TextView txtTotalLista;
    private LinearLayout containerItens;
    private long apiListaId = -1L; // ID inválido por padrão (sem ID fixo fictício)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);

        String listaIdStr = getIntent().getStringExtra(EXTRA_LISTA_ID);
        String listaNome = getIntent().getStringExtra(EXTRA_LISTA_NOME);

        try {
            if (listaIdStr != null && listaIdStr.matches("\\d+")) {
                apiListaId = Long.parseLong(listaIdStr);
            }
        } catch (NumberFormatException ignored) {
        }

        listaDeCompras = ProdutoRepository.getInstance().buscarListaPorId(listaIdStr);
        if (listaDeCompras == null) {
            listaDeCompras = ProdutoRepository.getInstance().criarListaDeCompras(listaNome != null ? listaNome : "Minha Lista de Compras");
        }

        TextView txtTitulo = findViewById(R.id.txtTituloLista);
        if (txtTitulo != null) {
            txtTitulo.setText(listaDeCompras.getNome());
        }

        txtTotalLista = findViewById(R.id.txtTotalLista);

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        NavigationRailView navRail = findViewById(R.id.navRail);
        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_listas);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(ListDetailsActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(ListDetailsActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        startActivity(new Intent(ListDetailsActivity.this, MinhasComprasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(ListDetailsActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        startActivity(new Intent(ListDetailsActivity.this, EscanearNotaActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_despesas) {
                        startActivity(new Intent(ListDetailsActivity.this, MinhasComprasDespesasActivity.class));
                        finish();
                        return true;
                    }
                    return false;
                }
            });
        }

        containerItens = findViewById(R.id.containerItensLista);

        MaterialButton btnAddProduto = findViewById(R.id.btnAdicionarItemLista);
        if (btnAddProduto != null) {
            btnAddProduto.setText("+ Adicionar item");
            btnAddProduto.setOnClickListener(v -> exibirDialogNovoItemManual());
        }

        MaterialButton btnFinalizar = findViewById(R.id.btnFinalizarLista);
        if (btnFinalizar != null) {
            btnFinalizar.setOnClickListener(v -> finalizarListaDeCompras());
        }

        // Validação estrita do ID da lista antes de qualquer requisição
        if (apiListaId <= 0) {
            Log.e("API_LISTA", "Erro: Nenhuma lista válida selecionada. ID da lista = " + apiListaId);
            Toast.makeText(this, "Nenhuma lista válida selecionada", Toast.LENGTH_SHORT).show();
            atualizarUiLista();
            return;
        }

        // Carrega os itens oficiais e o resumo diretamente do Supabase via FastAPI
        carregarItensOficiaisDaApi();
    }

    private void carregarItensOficiaisDaApi() {
        if (apiListaId <= 0) return;

        ApiService api = ApiClient.getApiService(this);
        Call<List<ItemLista>> chamada = api.listarItens(apiListaId);

        Log.d("API_LISTA", "GET /listas/" + apiListaId + "/itens -> Solicitando produtos oficiais do Supabase...");

        chamada.enqueue(new Callback<List<ItemLista>>() {
            @Override
            public void onResponse(@NonNull Call<List<ItemLista>> call, @NonNull Response<List<ItemLista>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ItemLista> itensApi = response.body();
                    Log.d("API_LISTA", "GET /listas/" + apiListaId + "/itens -> Sucesso HTTP " + response.code() + ". Total de itens do Supabase: " + itensApi.size());

                    // Limpa e atualiza os itens locais com os objetos e IDs reais do PostgreSQL
                    listaDeCompras.getItens().clear();
                    for (ItemLista item : itensApi) {
                        double preco = item.getPrecoUsuario() != null ? item.getPrecoUsuario().doubleValue() : 0.0;
                        Produto prod = new Produto((int) item.getId(), item.getNome(), preco, item.getImagemUrl());
                        ItemListaCompras itemLocal = new ItemListaCompras(prod, item.getQuantidade());
                        itemLocal.setPego(item.isComprado());
                        listaDeCompras.getItens().add(itemLocal);
                    }

                    atualizarUiLista();
                    obterResumoApi();
                } else {
                    tratarErroHttp(response, "GET /listas/" + apiListaId + "/itens");
                    atualizarUiLista();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ItemLista>> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "GET /listas/" + apiListaId + "/itens -> Falha de comunicação/servidor offline", t);
                Toast.makeText(ListDetailsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
                atualizarUiLista();
            }
        });
    }

    private void exibirDialogNovoItemManual() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("+ Adicionar item");

        LinearLayout layoutForm = new LinearLayout(this);
        layoutForm.setOrientation(LinearLayout.VERTICAL);
        layoutForm.setPadding(32, 24, 32, 16);

        final EditText editNome = new EditText(this);
        editNome.setHint("Nome do produto (ex: Arroz)");
        editNome.setInputType(InputType.TYPE_CLASS_TEXT);

        final EditText editPreco = new EditText(this);
        editPreco.setHint("Preço R$ (opcional, ex: 25.90)");
        editPreco.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        final EditText editQtde = new EditText(this);
        editQtde.setHint("Quantidade (padrão: 1)");
        editQtde.setText("1");
        editQtde.setInputType(InputType.TYPE_CLASS_NUMBER);

        layoutForm.addView(editNome);
        layoutForm.addView(editPreco);
        layoutForm.addView(editQtde);

        builder.setView(layoutForm);

        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String nome = editNome.getText().toString().trim();
            String precoStr = editPreco.getText().toString().trim();
            String qtdeStr = editQtde.getText().toString().trim();

            if (nome.isEmpty()) {
                Toast.makeText(ListDetailsActivity.this, "O nome do produto é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            int qtde = 1;
            try {
                if (!qtdeStr.isEmpty()) {
                    qtde = Integer.parseInt(qtdeStr);
                }
            } catch (NumberFormatException ignored) {
            }

            BigDecimal precoDecimal = null;
            if (!precoStr.isEmpty()) {
                try {
                    precoDecimal = new BigDecimal(precoStr.replace(",", "."));
                } catch (NumberFormatException ignored) {
                }
            }

            ItemCreateRequest novoReq = new ItemCreateRequest(
                    nome,
                    null,
                    null,
                    false,
                    precoDecimal,
                    qtde
            );

            cadastrarProdutoNaApi(novoReq);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void cadastrarProdutoNaApi(ItemCreateRequest request) {
        if (apiListaId <= 0) return;

        ApiService api = ApiClient.getApiService(this);
        Call<ItemLista> chamada = api.criarItem(apiListaId, request);

        Log.d("API_LISTA", "POST /listas/" + apiListaId + "/itens -> Enviando produto '" + request.getNome() + "' ao Supabase...");

        chamada.enqueue(new Callback<ItemLista>() {
            @Override
            public void onResponse(@NonNull Call<ItemLista> call, @NonNull Response<ItemLista> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ItemLista itemCriado = response.body();
                    Log.d("API_LISTA", "POST /listas/" + apiListaId + "/itens -> Sucesso HTTP " + response.code() + ". ID gerado no PostgreSQL: " + itemCriado.getId());

                    double precoRetornado = itemCriado.getPrecoUsuario() != null ? itemCriado.getPrecoUsuario().doubleValue() : 0.0;
                    Produto prodComIdReal = new Produto((int) itemCriado.getId(), itemCriado.getNome(), precoRetornado, itemCriado.getImagemUrl());

                    ItemListaCompras novoItemLocal = new ItemListaCompras(prodComIdReal, itemCriado.getQuantidade());
                    novoItemLocal.setPego(itemCriado.isComprado());

                    listaDeCompras.getItens().add(novoItemLocal);
                    atualizarUiLista();
                    obterResumoApi();
                    Toast.makeText(ListDetailsActivity.this, itemCriado.getNome() + " salvo no Supabase!", Toast.LENGTH_SHORT).show();
                } else {
                    tratarErroHttp(response, "POST /listas/" + apiListaId + "/itens");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemLista> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "POST /listas/" + apiListaId + "/itens -> Falha ao cadastrar produto", t);
                Toast.makeText(ListDetailsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finalizarListaDeCompras() {
        if (listaDeCompras == null || listaDeCompras.getItens().isEmpty()) {
            Toast.makeText(this, "Sua lista está vazia. Adicione produtos antes de finalizar!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean algumPego = false;
        for (ItemListaCompras item : listaDeCompras.getItens()) {
            if (item.isPego()) {
                algumPego = true;
                break;
            }
        }

        if (!algumPego) {
            Toast.makeText(this, "Marque ao menos um item como 'pego' para finalizar a compra!", Toast.LENGTH_LONG).show();
            return;
        }

        CompraFinalizada novaCompra = ProdutoRepository.getInstance().finalizarCompraDaLista(listaDeCompras, "Supermercado Extra");
        if (novaCompra != null) {
            Toast.makeText(this, "Compra finalizada com sucesso! Dados registrados em Despesas.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ListDetailsActivity.this, MinhasComprasDespesasActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void atualizarUiLista() {
        if (txtTotalLista != null) {
            txtTotalLista.setText(String.format(Locale.getDefault(), "R$ %.2f", listaDeCompras.getValorTotalLista()));
        }

        if (containerItens == null) return;
        containerItens.removeAllViews();

        if (listaDeCompras.getItens().isEmpty()) {
            TextView txtVazio = new TextView(this);
            txtVazio.setText("Sua lista de compras está vazia.\nClique em '+ Adicionar item' acima para incluir produtos!");
            txtVazio.setPadding(16, 32, 16, 16);
            txtVazio.setTextSize(16);
            txtVazio.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            containerItens.addView(txtVazio);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (ItemListaCompras itemLista : listaDeCompras.getItens()) {
            Produto prod = itemLista.getProduto();
            View itemView = inflater.inflate(R.layout.item_produto_lista, containerItens, false);

            MaterialCardView cardItem = (MaterialCardView) itemView;
            ImageView img = itemView.findViewById(R.id.imgProdutoLista);
            TextView txtNome = itemView.findViewById(R.id.txtNomeProdutoLista);
            TextView txtPrecoQtde = itemView.findViewById(R.id.txtPrecoEQtdeLista);
            TextView txtQtde = itemView.findViewById(R.id.txtQuantidade);
            MaterialButton btnInc = itemView.findViewById(R.id.btnIncrementar);
            MaterialButton btnDec = itemView.findViewById(R.id.btnDecrementar);
            ImageButton btnExcluir = itemView.findViewById(R.id.btnExcluirItem);

            if (txtNome != null) {
                txtNome.setText(prod.getNome());
            }

            if (txtPrecoQtde != null) {
                txtPrecoQtde.setText(String.format(Locale.getDefault(), "R$ %.2f un. | Total: R$ %.2f", prod.getPreco(), itemLista.getValorTotalItem()));
            }

            if (txtQtde != null) {
                txtQtde.setText(String.valueOf(itemLista.getQuantidade()));
            }

            if (img != null) {
                Glide.with(this)
                        .load(prod.getUrlImagem())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(img);
            }

            // Estilização para o estado "pego" / "não pego"
            if (itemLista.isPego()) {
                if (cardItem != null) {
                    cardItem.setCardBackgroundColor(Color.parseColor("#E5E5E5"));
                }
                if (txtNome != null) {
                    txtNome.setPaintFlags(txtNome.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    txtNome.setTextColor(Color.parseColor("#777777"));
                }
                if (txtPrecoQtde != null) {
                    txtPrecoQtde.setPaintFlags(txtPrecoQtde.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    txtPrecoQtde.setTextColor(Color.parseColor("#888888"));
                }
                if (img != null) {
                    img.setAlpha(0.4f);
                }
            } else {
                if (cardItem != null) {
                    cardItem.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                }
                if (txtNome != null) {
                    txtNome.setPaintFlags(txtNome.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    txtNome.setTextColor(Color.parseColor("#000000"));
                }
                if (txtPrecoQtde != null) {
                    txtPrecoQtde.setPaintFlags(txtPrecoQtde.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    txtPrecoQtde.setTextColor(getColor(R.color.colorPrimary));
                }
                if (img != null) {
                    img.setAlpha(1.0f);
                }
            }

            // Alternar "comprado" via PATCH
            itemView.setOnClickListener(v -> {
                itemLista.togglePego();
                atualizarUiLista();
                atualizarItemApi((long) prod.getId(), ItemUpdateRequest.comComprado(itemLista.isPego()));
            });

            // Clique longo ou toque no preço abre formulário completo de edição
            if (txtPrecoQtde != null) {
                txtPrecoQtde.setOnClickListener(v -> exibirDialogEditarProdutoCompleto(itemLista));
            }
            itemView.setOnLongClickListener(v -> {
                exibirDialogEditarProdutoCompleto(itemLista);
                return true;
            });

            if (btnInc != null) {
                btnInc.setOnClickListener(v -> {
                    itemLista.setQuantidade(itemLista.getQuantidade() + 1);
                    atualizarUiLista();
                    atualizarItemApi((long) prod.getId(), ItemUpdateRequest.comQuantidade(itemLista.getQuantidade()));
                });
            }

            if (btnDec != null) {
                btnDec.setOnClickListener(v -> {
                    if (itemLista.getQuantidade() > 1) {
                        itemLista.setQuantidade(itemLista.getQuantidade() - 1);
                        atualizarUiLista();
                        atualizarItemApi((long) prod.getId(), ItemUpdateRequest.comQuantidade(itemLista.getQuantidade()));
                    } else {
                        exibirDialogConfirmarExclusao(itemLista);
                    }
                });
            }

            if (btnExcluir != null) {
                btnExcluir.setOnClickListener(v -> exibirDialogConfirmarExclusao(itemLista));
            }

            containerItens.addView(itemView);
        }
    }

    private void exibirDialogEditarProdutoCompleto(ItemListaCompras itemLista) {
        Produto prod = itemLista.getProduto();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Produto");

        LinearLayout layoutForm = new LinearLayout(this);
        layoutForm.setOrientation(LinearLayout.VERTICAL);
        layoutForm.setPadding(32, 24, 32, 16);

        final EditText editNome = new EditText(this);
        editNome.setHint("Nome do produto");
        editNome.setText(prod.getNome());

        final EditText editPreco = new EditText(this);
        editPreco.setHint("Preço (R$)");
        editPreco.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editPreco.setText(String.format(Locale.US, "%.2f", prod.getPreco()));

        final EditText editQtde = new EditText(this);
        editQtde.setHint("Quantidade");
        editQtde.setInputType(InputType.TYPE_CLASS_NUMBER);
        editQtde.setText(String.valueOf(itemLista.getQuantidade()));

        layoutForm.addView(editNome);
        layoutForm.addView(editPreco);
        layoutForm.addView(editQtde);

        builder.setView(layoutForm);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novoNome = editNome.getText().toString().trim();
            String precoStr = editPreco.getText().toString().trim();
            String qtdeStr = editQtde.getText().toString().trim();

            if (novoNome.isEmpty()) {
                Toast.makeText(ListDetailsActivity.this, "O nome não pode ficar vazio", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal novoPreco = null;
            if (!precoStr.isEmpty()) {
                try {
                    novoPreco = new BigDecimal(precoStr.replace(",", "."));
                    prod.setPreco(novoPreco.doubleValue());
                } catch (NumberFormatException ignored) {
                }
            }

            Integer novaQtde = null;
            if (!qtdeStr.isEmpty()) {
                try {
                    novaQtde = Integer.parseInt(qtdeStr);
                    itemLista.setQuantidade(novaQtde);
                } catch (NumberFormatException ignored) {
                }
            }

            prod.setNome(novoNome);
            atualizarUiLista();

            ItemUpdateRequest req = new ItemUpdateRequest();
            req.setNome(novoNome);
            if (novoPreco != null) req.setPrecoUsuario(novoPreco);
            if (novaQtde != null) req.setQuantidade(novaQtde);

            atualizarItemApi((long) prod.getId(), req);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void exibirDialogConfirmarExclusao(ItemListaCompras itemLista) {
        Produto prod = itemLista.getProduto();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Excluir Produto");
        builder.setMessage("Deseja realmente remover '" + prod.getNome() + "' da lista?");
        builder.setPositiveButton("Excluir", (dialog, which) -> excluirItemApi((long) prod.getId(), itemLista));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void atualizarItemApi(long itemId, ItemUpdateRequest request) {
        if (apiListaId <= 0) return;

        ApiService api = ApiClient.getApiService(this);
        Call<ItemLista> chamada = api.atualizarItem(apiListaId, itemId, request);

        Log.d("API_LISTA", "PATCH /listas/" + apiListaId + "/itens/" + itemId + " -> Enviando atualização...");

        chamada.enqueue(new Callback<ItemLista>() {
            @Override
            public void onResponse(@NonNull Call<ItemLista> call, @NonNull Response<ItemLista> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_LISTA", "PATCH /listas/" + apiListaId + "/itens/" + itemId + " -> Sucesso HTTP " + response.code());
                    obterResumoApi();
                } else {
                    tratarErroHttp(response, "PATCH /listas/" + apiListaId + "/itens/" + itemId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemLista> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "PATCH /listas/" + apiListaId + "/itens/" + itemId + " -> Falha de conexão/servidor", t);
                Toast.makeText(ListDetailsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void excluirItemApi(long itemId, ItemListaCompras itemLocal) {
        if (apiListaId <= 0) return;

        ApiService api = ApiClient.getApiService(this);
        Call<DeleteItemResponse> chamada = api.excluirItem(apiListaId, itemId);

        Log.d("API_LISTA", "DELETE /listas/" + apiListaId + "/itens/" + itemId + " -> Enviando solicitação de exclusão...");

        chamada.enqueue(new Callback<DeleteItemResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteItemResponse> call, @NonNull Response<DeleteItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeleteItemResponse body = response.body();
                    Log.d("API_LISTA", "DELETE /listas/" + apiListaId + "/itens/" + itemId + " -> Sucesso HTTP " + response.code() + ". Mensagem: " + body.getMensagem());
                    listaDeCompras.getItens().remove(itemLocal);
                    atualizarUiLista();
                    Toast.makeText(ListDetailsActivity.this, "Produto removido com sucesso!", Toast.LENGTH_SHORT).show();
                    obterResumoApi();
                } else {
                    tratarErroHttp(response, "DELETE /listas/" + apiListaId + "/itens/" + itemId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteItemResponse> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "DELETE /listas/" + apiListaId + "/itens/" + itemId + " -> Falha ao excluir item", t);
                Toast.makeText(ListDetailsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obterResumoApi() {
        if (apiListaId <= 0) return;

        ApiService api = ApiClient.getApiService(this);
        Call<ResumoListaResponse> chamada = api.obterResumoLista(apiListaId);

        Log.d("API_LISTA", "GET /listas/" + apiListaId + "/resumo -> Solicitando resumo financeiro...");

        chamada.enqueue(new Callback<ResumoListaResponse>() {
            @Override
            public void onResponse(@NonNull Call<ResumoListaResponse> call, @NonNull Response<ResumoListaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResumoListaResponse resumo = response.body();
                    Log.d("API_LISTA", "GET /listas/" + apiListaId + "/resumo -> Sucesso HTTP " + response.code() + ". Estimado: " + resumo.getTotalEstimado() + " | Comprado: " + resumo.getTotalComprado() + " | Pendente: " + resumo.getTotalPendente());
                    if (txtTotalLista != null && resumo.getTotalEstimado() != null) {
                        txtTotalLista.setText(String.format("R$ %s", resumo.getTotalEstimado()));
                    }
                } else {
                    tratarErroHttp(response, "GET /listas/" + apiListaId + "/resumo");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResumoListaResponse> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "GET /listas/" + apiListaId + "/resumo -> Falha ao carregar resumo", t);
            }
        });
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
            Intent intent = new Intent(ListDetailsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        } else if (code == 404) {
            mensagemErro = "Produto ou lista não encontrados no servidor.";
        } else if (code == 422) {
            mensagemErro = "Dados inválidos para a requisição.";
            if (!detalheValidacao.isEmpty()) {
                Log.e("API_LISTA", endpointInfo + " -> Detalhes de Validação 422: " + detalheValidacao);
            }
        } else if (code == 503) {
            mensagemErro = "Servidor indisponível ou falha na operação com o banco de dados.";
        } else {
            mensagemErro = "Erro na requisição: HTTP " + code;
        }

        Toast.makeText(this, mensagemErro, Toast.LENGTH_SHORT).show();
        Log.e("API_LISTA", endpointInfo + " -> Erro HTTP " + code + " | Resposta: " + detalheValidacao);
    }
}