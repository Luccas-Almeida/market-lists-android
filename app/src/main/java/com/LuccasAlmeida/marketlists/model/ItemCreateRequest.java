package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class ItemCreateRequest {

    @SerializedName("nome")
    private String nome;

    @SerializedName("imagem_url")
    private String imagemUrl;

    @SerializedName("cloudinary_public_id")
    private String cloudinaryPublicId;

    @SerializedName("imagem_enviada_pelo_usuario")
    private boolean imagemEnviadaPeloUsuario;

    @SerializedName("preco_usuario")
    private BigDecimal precoUsuario;

    @SerializedName("quantidade")
    private int quantidade;

    public ItemCreateRequest(
            String nome,
            String imagemUrl,
            String cloudinaryPublicId,
            boolean imagemEnviadaPeloUsuario,
            BigDecimal precoUsuario,
            int quantidade
    ) {
        this.nome = nome;
        this.imagemUrl = imagemUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.imagemEnviadaPeloUsuario = imagemEnviadaPeloUsuario;
        this.precoUsuario = precoUsuario;
        this.quantidade = quantidade;
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

    public boolean isImagemEnviadaPeloUsuario() {
        return imagemEnviadaPeloUsuario;
    }

    public BigDecimal getPrecoUsuario() {
        return precoUsuario;
    }

    public int getQuantidade() {
        return quantidade;
    }
}