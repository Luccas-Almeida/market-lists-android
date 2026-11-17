package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class DeleteItemResponse {

    @SerializedName("mensagem")
    private String mensagem;

    @SerializedName("id")
    private long id;

    @SerializedName("imagem_excluida")
    private boolean imagemExcluida;

    @SerializedName("observacao")
    private String observacao;

    public String getMensagem() {
        return mensagem;
    }

    public long getId() {
        return id;
    }

    public boolean isImagemExcluida() {
        return imagemExcluida;
    }

    public String getObservacao() {
        return observacao;
    }
}