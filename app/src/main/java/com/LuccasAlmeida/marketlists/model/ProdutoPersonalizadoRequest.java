package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class ProdutoPersonalizadoRequest {

    @SerializedName("nome")
    private String nome;

    @SerializedName("imagem_url")
    private String imagemUrl;

    @SerializedName("cloudinary_public_id")
    private String cloudinaryPublicId;

    public ProdutoPersonalizadoRequest(String nome, String imagemUrl, String cloudinaryPublicId) {
        this.nome = nome;
        this.imagemUrl = imagemUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
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
}