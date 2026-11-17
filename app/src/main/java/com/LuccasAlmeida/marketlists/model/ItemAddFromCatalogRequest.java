package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class ItemAddFromCatalogRequest {

    @SerializedName("produto_id")
    private long produtoId;

    @SerializedName("quantidade")
    private int quantidade;

    @SerializedName("preco_usuario")
    private BigDecimal precoUsuario;

    public ItemAddFromCatalogRequest(long produtoId, int quantidade, BigDecimal precoUsuario) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUsuario = precoUsuario;
    }

    public long getProdutoId() {
        return produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUsuario() {
        return precoUsuario;
    }
}