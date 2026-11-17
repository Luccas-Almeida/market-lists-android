package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.LuccasAlmeida.marketlists.api.ApiClient;
import com.LuccasAlmeida.marketlists.api.ApiService;
import com.LuccasAlmeida.marketlists.api.SessionManager;
import com.LuccasAlmeida.marketlists.model.ItemAddFromCatalogRequest;
import com.LuccasAlmeida.marketlists.model.ItemLista;
import com.LuccasAlmeida.marketlists.model.ListaCompra;
import com.LuccasAlmeida.marketlists.model.ProdutoCatalog;
import com.LuccasAlmeida.marketlists.model.ProdutoPersonalizadoRequest;
import com.LuccasAlmeida.marketlists.model.UploadImagemResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORIA_ID = "extra_categoria_id";
    public static final String EXTRA_CATEGORIA_NOME = "extra_categoria_nome";
    public static final String EXTRA_CATEGORIA_SLUG = "extra_categoria_slug";

    private long idCategoria = 0L;
    private String nomeCategoria;
    private String slugCategoria;

    private ActivityResultLauncher<String> getContentLauncher;
    private Uri selectedImageUri = null;
    private ImageView imgPreviewDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        idCategoria = getIntent().getLongExtra(EXTRA_CATEGORIA_ID, 0L);
        if (idCategoria == 0L) {
            idCategoria = getIntent().getIntExtra(EXTRA_CATEGORIA_ID, 0);
        }
        nomeCategoria = getIntent().getStringExtra(EXTRA_CATEGORIA_NOME);
        slugCategoria = getIntent().getStringExtra(EXTRA_CATEGORIA_SLUG);

        Log.d("API_LISTA", "CategoryProductsActivity -> Categoria ID: " + idCategoria + " | Nome: " + nomeCategoria);

        TextView txtTitulo = findViewById(R.id.txtTituloCategoria);
        if (txtTitulo != null && nomeCategoria != null) {
            txtTitulo.setText(nomeCategoria);
        }

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        // Lançador nativo para seleção de fotografia na galeria
        getContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                Log.d("API_LISTA", "Imagem selecionada da galeria: " + uri);
                if (imgPreviewDialog != null) {
                    imgPreviewDialog.setVisibility(View.VISIBLE);
                    Glide.with(CategoryProductsActivity.this)
                            .load(uri)
                            .into(imgPreviewDialog);
                }
            }
        });

        MaterialButton btnNovoProduto = findViewById(R.id.btnNovoProdutoPersonalizado);
        boolean isPersonalizados = (slugCategoria != null && slugCategoria.toLowerCase().contains("personalizado"))
                || (nomeCategoria != null && nomeCategoria.toLowerCase().contains("personalizado"))
                || idCategoria == 9L;

        if (btnNovoProduto != null) {
            if (isPersonalizados) {
                btnNovoProduto.setVisibility(View.VISIBLE);
                btnNovoProduto.setOnClickListener(v -> exibirDialogNovoProdutoPersonalizado());
            } else {
                btnNovoProduto.setVisibility(View.GONE);
            }
        }

        NavigationRailView navRail = findViewById(R.id.navRail);
        if (navRail != null) {
            navRail.setSelectedItemId(R.id.nav_categorias);
            navRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        startActivity(new Intent(CategoryProductsActivity.this, HomeActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_listas) {
                        startActivity(new Intent(CategoryProductsActivity.this, MinhasListasActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_compras) {
                        Toast.makeText(CategoryProductsActivity.this, "Histórico de compras — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_categorias) {
                        startActivity(new Intent(CategoryProductsActivity.this, CategoriesActivity.class));
                        finish();
                        return true;
                    } else if (itemId == R.id.nav_escanear) {
                        Toast.makeText(CategoryProductsActivity.this, "Leitura de notas fiscais — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemId == R.id.nav_despesas) {
                        Toast.makeText(CategoryProductsActivity.this, "Relatórios de despesas — Em breve", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return false;
                }
            });
        }

        carregarProdutosDaCategoriaApi(idCategoria);
    }

    private void exibirDialogNovoProdutoPersonalizado() {
        selectedImageUri = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("+ Novo Produto Personalizado");

        LinearLayout layoutForm = new LinearLayout(this);
        layoutForm.setOrientation(LinearLayout.VERTICAL);
        layoutForm.setPadding(32, 24, 32, 16);

        final EditText editNome = new EditText(this);
        editNome.setHint("Nome do produto (obrigatório)");
        editNome.setInputType(InputType.TYPE_CLASS_TEXT);

        MaterialButton btnEscolherFoto = new MaterialButton(this);
        btnEscolherFoto.setText("Escolher Foto da Galeria");
        btnEscolherFoto.setIconResource(android.R.drawable.ic_menu_gallery);

        imgPreviewDialog = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(120), dpToPx(120));
        imgParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        imgParams.setMargins(0, 16, 0, 16);
        imgPreviewDialog.setLayoutParams(imgParams);
        imgPreviewDialog.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgPreviewDialog.setVisibility(View.GONE);

        btnEscolherFoto.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        layoutForm.addView(editNome);
        layoutForm.addView(btnEscolherFoto);
        layoutForm.addView(imgPreviewDialog);

        builder.setView(layoutForm);

        builder.setPositiveButton("Cadastrar", (dialog, which) -> {
            String nome = editNome.getText().toString().trim();
            if (nome.isEmpty()) {
                Toast.makeText(CategoryProductsActivity.this, "O nome do produto é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadFotoEEfetuarCadastro(nome, selectedImageUri);
            } else {
                cadastrarProdutoPersonalizadoSemFoto(nome, null, null);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void uploadFotoEEfetuarCadastro(String nome, Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                cadastrarProdutoPersonalizadoSemFoto(nome, null, null);
                return;
            }

            byte[] bytes = getBytesFromInputStream(inputStream);
            if (bytes.length > 5 * 1024 * 1024) { // Limite de 5 MB
                Toast.makeText(this, "A imagem selecionada excede o limite de 5 MB", Toast.LENGTH_LONG).show();
                return;
            }

            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse(mimeType));
            MultipartBody.Part partBody = MultipartBody.Part.createFormData("arquivo", "foto_produto.jpg", requestFile);

            ApiService api = ApiClient.getApiService(this);
            Log.d("API_LISTA", "POST /imagens/produtos/upload -> Enviando imagem multipart (" + bytes.length + " bytes)...");

            api.uploadImagemProduto(partBody).enqueue(new Callback<UploadImagemResponse>() {
                @Override
                public void onResponse(@NonNull Call<UploadImagemResponse> call, @NonNull Response<UploadImagemResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UploadImagemResponse uploadRes = response.body();
                        Log.d("API_LISTA", "POST /imagens/produtos/upload -> Sucesso HTTP " + response.code() + ". URL Cloudinary: " + uploadRes.getImagemUrl());
                        cadastrarProdutoPersonalizadoSemFoto(nome, uploadRes.getImagemUrl(), uploadRes.getCloudinaryPublicId());
                    } else {
                        tratarErroHttp(response, "POST /imagens/produtos/upload");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UploadImagemResponse> call, @NonNull Throwable t) {
                    Log.e("API_LISTA", "POST /imagens/produtos/upload -> Falha de upload", t);
                    Toast.makeText(CategoryProductsActivity.this, "Falha ao enviar imagem para o Cloudinary", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("API_LISTA", "Erro ao processar imagem da galeria", e);
            Toast.makeText(this, "Erro ao processar imagem da galeria", Toast.LENGTH_SHORT).show();
        }
    }

    private void cadastrarProdutoPersonalizadoSemFoto(String nome, String urlImagem, String cloudinaryPublicId) {
        ApiService api = ApiClient.getApiService(this);
        ProdutoPersonalizadoRequest request = new ProdutoPersonalizadoRequest(nome, urlImagem, cloudinaryPublicId);

        Log.d("API_LISTA", "POST /produtos/personalizados -> Cadastrando produto '" + nome + "' no Supabase...");

        api.criarProdutoPersonalizado(request).enqueue(new Callback<ProdutoCatalog>() {
            @Override
            public void onResponse(@NonNull Call<ProdutoCatalog> call, @NonNull Response<ProdutoCatalog> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProdutoCatalog prodCriado = response.body();
                    Log.d("API_LISTA", "POST /produtos/personalizados -> Sucesso HTTP " + response.code() + ". ID do produto: " + prodCriado.getId());
                    Toast.makeText(CategoryProductsActivity.this, "Produto '" + prodCriado.getNome() + "' cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    carregarProdutosDaCategoriaApi(idCategoria);
                } else {
                    tratarErroHttp(response, "POST /produtos/personalizados");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProdutoCatalog> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "POST /produtos/personalizados -> Falha ao cadastrar produto personalizado", t);
                Toast.makeText(CategoryProductsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void carregarProdutosDaCategoriaApi(long idCategoria) {
        LinearLayout container = findViewById(R.id.containerProdutosCategoria);
        if (container == null) return;

        container.removeAllViews();

        ApiService api = ApiClient.getApiService(this);
        Log.d("API_LISTA", "GET /categorias/" + idCategoria + "/produtos -> Solicitando produtos da categoria...");

        api.obterProdutosPorCategoria(idCategoria).enqueue(new Callback<List<ProdutoCatalog>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProdutoCatalog>> call, @NonNull Response<List<ProdutoCatalog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProdutoCatalog> produtos = response.body();
                    Log.d("API_LISTA", "GET /categorias/" + idCategoria + "/produtos -> Sucesso HTTP " + response.code() + ". Total de produtos: " + produtos.size());
                    exibirProdutosNaTela(produtos);
                } else {
                    tratarErroHttp(response, "GET /categorias/" + idCategoria + "/produtos");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProdutoCatalog>> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "GET /categorias/" + idCategoria + "/produtos -> Falha de comunicação/servidor offline", t);
                Toast.makeText(CategoryProductsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exibirProdutosNaTela(List<ProdutoCatalog> produtos) {
        LinearLayout container = findViewById(R.id.containerProdutosCategoria);
        if (container == null) return;

        container.removeAllViews();

        if (produtos.isEmpty()) {
            TextView txtVazio = new TextView(this);
            txtVazio.setText("Nenhum produto cadastrado nesta categoria.");
            txtVazio.setPadding(16, 32, 16, 16);
            txtVazio.setTextSize(16);
            txtVazio.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            container.addView(txtVazio);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (ProdutoCatalog prod : produtos) {
            View itemView = inflater.inflate(R.layout.item_produto, container, false);

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
                String urlFoto = prod.getImagemUrl();
                Glide.with(this)
                        .load(urlFoto)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(img);
            }

            if (btnAdd != null) {
                btnAdd.setOnClickListener(v -> solicitarAdicaoAoCarrinho(prod));
            }

            container.addView(itemView);
        }
    }

    // PASSO 1: Busca as listas reais do usuário via GET /listas antes de solicitar preço/quantidade
    private void solicitarAdicaoAoCarrinho(ProdutoCatalog produto) {
        ApiService api = ApiClient.getApiService(this);
        Log.d("API_LISTA", "GET /listas -> Buscando listas ativas do usuário para seleção de destino...");

        api.obterListas().enqueue(new Callback<List<ListaCompra>>() {
            @Override
            public void onResponse(@NonNull Call<List<ListaCompra>> call, @NonNull Response<List<ListaCompra>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ListaCompra> listas = response.body();
                    Log.d("API_LISTA", "GET /listas -> Sucesso HTTP " + response.code() + ". Total de listas para seleção: " + listas.size());

                    if (listas.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryProductsActivity.this);
                        builder.setTitle("Nenhuma Lista Encontrada");
                        builder.setMessage("Você não possui nenhuma lista de compras criada no Supabase. Crie uma lista antes de adicionar produtos.");
                        builder.setPositiveButton("Criar Lista", (dialog, which) -> {
                            Intent intent = new Intent(CategoryProductsActivity.this, MinhasListasActivity.class);
                            startActivity(intent);
                        });
                        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
                        builder.show();
                    } else {
                        exibirDialogSelecaoDeLista(produto, listas);
                    }
                } else {
                    tratarErroHttp(response, "GET /listas");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ListaCompra>> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "GET /listas -> Falha ao buscar listas para seleção", t);
                Toast.makeText(CategoryProductsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exibirDialogSelecaoDeLista(ProdutoCatalog produto, List<ListaCompra> listas) {
        String[] nomesListas = new String[listas.size()];
        for (int i = 0; i < listas.size(); i++) {
            nomesListas[i] = listas.get(i).getNome();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha a lista para '" + produto.getNome() + "'");
        builder.setItems(nomesListas, (dialog, which) -> {
            ListaCompra listaSelecionada = listas.get(which);
            long realListaId = listaSelecionada.getId();
            String realListaNome = listaSelecionada.getNome();

            Log.d("API_LISTA", "Lista selecionada pelo usuário: ID " + realListaId + " | Nome: " + realListaNome);
            exibirFormularioPrecoQuantidade(produto, realListaId, realListaNome);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // PASSO 2: Formulário com Nome, Preço R$ (opcional) e Quantidade (mínimo 1)
    private void exibirFormularioPrecoQuantidade(ProdutoCatalog produto, long realListaId, String realListaNome) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar à Lista - " + produto.getNome());

        LinearLayout layoutForm = new LinearLayout(this);
        layoutForm.setOrientation(LinearLayout.VERTICAL);
        layoutForm.setPadding(32, 24, 32, 16);

        final EditText editPreco = new EditText(this);
        editPreco.setHint(String.format(Locale.getDefault(), "Preço R$ (opcional, atual: %.2f)", produto.getPreco()));
        editPreco.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        final EditText editQtde = new EditText(this);
        editQtde.setHint("Quantidade (mínimo 1, padrão: 1)");
        editQtde.setText("1");
        editQtde.setInputType(InputType.TYPE_CLASS_NUMBER);

        layoutForm.addView(editPreco);
        layoutForm.addView(editQtde);

        builder.setView(layoutForm);

        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String precoStr = editPreco.getText().toString().trim();
            String qtdeStr = editQtde.getText().toString().trim();

            int qtde = 1;
            if (!qtdeStr.isEmpty()) {
                try {
                    qtde = Integer.parseInt(qtdeStr);
                } catch (NumberFormatException ignored) {
                }
            }

            if (qtde < 1) {
                Toast.makeText(CategoryProductsActivity.this, "A quantidade mínima é 1", Toast.LENGTH_SHORT).show();
                return;
            }

            BigDecimal precoDecimal = null;
            if (!precoStr.isEmpty()) {
                try {
                    precoDecimal = new BigDecimal(precoStr.replace(",", "."));
                    if (precoDecimal.compareTo(BigDecimal.ZERO) < 0) {
                        Toast.makeText(CategoryProductsActivity.this, "O preço não pode ser negativo", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(CategoryProductsActivity.this, "Preço inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            adicionarProdutoDoCatalogoApi(realListaId, produto.getId(), qtde, precoDecimal, produto.getNome(), realListaNome);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // PASSO 3: Executa POST /listas/{lista_id}/produtos com o ID real da lista e do produto
    private void adicionarProdutoDoCatalogoApi(long realListaId, long produtoId, int quantidade, BigDecimal precoUsuario, String nomeProduto, String nomeLista) {
        ApiService api = ApiClient.getApiService(this);
        ItemAddFromCatalogRequest req = new ItemAddFromCatalogRequest(produtoId, quantidade, precoUsuario);

        Log.d("API_LISTA", "POST /listas/" + realListaId + "/produtos -> Adicionando produto ID " + produtoId + " à lista " + realListaId + "...");

        api.adicionarProdutoDoCatalogo(realListaId, req).enqueue(new Callback<ItemLista>() {
            @Override
            public void onResponse(@NonNull Call<ItemLista> call, @NonNull Response<ItemLista> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ItemLista itemAdicionado = response.body();
                    Log.d("API_LISTA", "POST /listas/" + realListaId + "/produtos -> Sucesso HTTP " + response.code() + ". Item na lista ID: " + itemAdicionado.getId());
                    Toast.makeText(CategoryProductsActivity.this, "'" + nomeProduto + "' adicionado à lista '" + nomeLista + "' com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    tratarErroHttp(response, "POST /listas/" + realListaId + "/produtos");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemLista> call, @NonNull Throwable t) {
                Log.e("API_LISTA", "POST /listas/" + realListaId + "/produtos -> Falha de comunicação/servidor", t);
                Toast.makeText(CategoryProductsActivity.this, "Servidor indisponível ou falha de conexão", Toast.LENGTH_SHORT).show();
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
            Log.e("API_LISTA", "Erro ao ler resposta de erro HTTP", e);
        }

        if (code == 401) {
            mensagemErro = "Sessão expirada. Faça login novamente.";
            Log.e("API_LISTA", endpointInfo + " -> HTTP 401 Unauthorized. Redirecionando para login...");
            SessionManager.getInstance(this).clearSession();
            Intent intent = new Intent(CategoryProductsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        } else if (code == 404) {
            mensagemErro = "Categoria ou lista não encontrada no servidor.";
        } else if (code == 413) {
            mensagemErro = "A imagem excede o tamanho máximo de 5 MB.";
        } else if (code == 415) {
            mensagemErro = "Formato de imagem não suportado. Use JPEG, PNG ou WebP.";
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