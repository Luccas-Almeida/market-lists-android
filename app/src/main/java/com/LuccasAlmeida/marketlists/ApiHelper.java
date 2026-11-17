package com.LuccasAlmeida.marketlists;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiHelper {

    public interface ApiCallback<T> {
        void onSuccess(List<T> result);
        void onError(String errorMessage);
    }

    public static void buscarProdutos(Context context, String urlApi, ApiCallback<Produto> callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                urlApi,
                null,
                response -> {
                    List<Produto> lista = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.getInt("id");
                            String nome = obj.getString("nome");
                            double preco = obj.optDouble("preco", 0.0);
                            String urlImagem = obj.optString("url_imagem", "");

                            lista.add(new Produto(id, nome, preco, urlImagem));
                        }
                        callback.onSuccess(lista);
                    } catch (JSONException e) {
                        callback.onError("Erro ao ler dados do JSON: " + e.getMessage());
                    }
                },
                error -> callback.onError("Erro na requisição: " + (error.getMessage() != null ? error.getMessage() : "Erro de rede"))
        );

        queue.add(request);
    }

    public static void buscarCategorias(Context context, String urlApi, ApiCallback<Categoria> callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                urlApi,
                null,
                response -> {
                    List<Categoria> lista = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.getInt("id");
                            String nome = obj.getString("nome");
                            String urlImagem = obj.optString("url_imagem", "");

                            lista.add(new Categoria(id, nome, urlImagem));
                        }
                        callback.onSuccess(lista);
                    } catch (JSONException e) {
                        callback.onError("Erro ao ler JSON: " + e.getMessage());
                    }
                },
                error -> callback.onError("Erro de conexão: " + (error.getMessage() != null ? error.getMessage() : "Erro de rede"))
        );

        queue.add(request);
    }
}