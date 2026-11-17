package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class ListaCompra {

    @SerializedName("id")
    private long id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("criado_em")
    private String criadoEm;

    @SerializedName("usuario_id")
    private String usuarioId;

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public String getUsuarioId() {
        return usuarioId;
    }
}