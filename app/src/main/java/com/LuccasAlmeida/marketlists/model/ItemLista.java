package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class ItemLista {

    @SerializedName("id")
    private long id;

    @SerializedName("lista_id")
    private long listaId;

    @SerializedName("produto_id")
    private Long produtoId;

    @SerializedName("nome")
    private String nome;

    @SerializedName("imagem_url")
    private String imagemUrl;

    @SerializedName("cloudinary_public_id")
    private String cloudinaryPublicId;

    @SerializedName("preco_usuario")
    private BigDecimal precoUsuario;

    @SerializedName("quantidade")
    private int quantidade;

    @SerializedName("comprado")
    private boolean comprado;

    public long getId() {
        return id;
    }

    public long getListaId() {
        return listaId;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getNome() {
        return nome;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public BigDecimal getPrecoUsuario() {
        return precoUsuario;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public boolean isComprado() {
        return comprado;
    }
}