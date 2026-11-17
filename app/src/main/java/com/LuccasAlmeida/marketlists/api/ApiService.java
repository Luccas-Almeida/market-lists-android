package com.LuccasAlmeida.marketlists.api;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

import com.LuccasAlmeida.marketlists.Categoria;
import com.LuccasAlmeida.marketlists.model.DeleteItemResponse;
import com.LuccasAlmeida.marketlists.model.HealthResponse;
import com.LuccasAlmeida.marketlists.model.ItemAddFromCatalogRequest;
import com.LuccasAlmeida.marketlists.model.ItemCreateRequest;
import com.LuccasAlmeida.marketlists.model.ItemLista;
import com.LuccasAlmeida.marketlists.model.ItemUpdateRequest;
import com.LuccasAlmeida.marketlists.model.ListaCompra;
import com.LuccasAlmeida.marketlists.model.ListaCreateRequest;
import com.LuccasAlmeida.marketlists.model.LoginRequest;
import com.LuccasAlmeida.marketlists.model.LoginResponse;
import com.LuccasAlmeida.marketlists.model.ProdutoCatalog;
import com.LuccasAlmeida.marketlists.model.ProdutoPersonalizadoRequest;
import com.LuccasAlmeida.marketlists.model.RegisterRequest;
import com.LuccasAlmeida.marketlists.model.ResumoListaResponse;
import com.LuccasAlmeida.marketlists.model.UploadImagemResponse;
import com.LuccasAlmeida.marketlists.model.UserResponse;

public interface ApiService {

    // --- HEALTH CHECK ---
    @GET("health")
    Call<HealthResponse> testarHealth();

    // --- AUTENTICAÇÃO ---
    @POST("auth/login")
    Call<LoginResponse> login(
        @Body LoginRequest request
    );

    @POST("auth/register")
    Call<LoginResponse> register(
        @Body RegisterRequest request
    );

    @GET("auth/me")
    Call<UserResponse> getMe();

    // --- GERENCIAMENTO DE LISTAS DO USUÁRIO ---
    @GET("listas")
    Call<List<ListaCompra>> obterListas();

    @POST("listas")
    Call<ListaCompra> criarLista(
        @Body ListaCreateRequest request
    );

    // --- CATÁLOGO DE CATEGORIAS E PRODUTOS ---
    @GET("categorias")
    Call<List<Categoria>> obterCategorias();

    @GET("categorias/{categoria_id}/produtos")
    Call<List<ProdutoCatalog>> obterProdutosPorCategoria(
        @Path("categoria_id") long categoriaId
    );

    @POST("produtos/personalizados")
    Call<ProdutoCatalog> criarProdutoPersonalizado(
        @Body ProdutoPersonalizadoRequest request
    );

    @Multipart
    @POST("imagens/produtos/upload")
    Call<UploadImagemResponse> uploadImagemProduto(
        @Part MultipartBody.Part arquivo
    );

    @POST("listas/{lista_id}/produtos")
    Call<ItemLista> adicionarProdutoDoCatalogo(
        @Path("lista_id") long listaId,
        @Body ItemAddFromCatalogRequest request
    );

    // --- ITENS DA LISTA ---
    @GET("listas/{lista_id}/itens")
    Call<List<ItemLista>> listarItens(
        @Path("lista_id") long listaId
    );

    @POST("listas/{lista_id}/itens")
    Call<ItemLista> criarItem(
        @Path("lista_id") long listaId,
        @Body ItemCreateRequest item
    );

    @PATCH("listas/{lista_id}/itens/{item_id}")
    Call<ItemLista> atualizarItem(
        @Path("lista_id") long listaId,
        @Path("item_id") long itemId,
        @Body ItemUpdateRequest item
    );

    @DELETE("listas/{lista_id}/itens/{item_id}")
    Call<DeleteItemResponse> excluirItem(
        @Path("lista_id") long listaId,
        @Path("item_id") long itemId
    );

    @GET("listas/{lista_id}/resumo")
    Call<ResumoListaResponse> obterResumoLista(
        @Path("lista_id") long listaId
    );

}