package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class ListaCreateRequest {

    @SerializedName("nome")
    private String nome;

    public ListaCreateRequest(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}