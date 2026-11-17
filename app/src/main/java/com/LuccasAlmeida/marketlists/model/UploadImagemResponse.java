package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class UploadImagemResponse {

    @SerializedName("imagem_url")
    private String imagemUrl;

    @SerializedName("cloudinary_public_id")
    private String cloudinaryPublicId;

    @SerializedName("formato")
    private String formato;

    @SerializedName("tamanho_bytes")
    private long tamanhoBytes;

    @SerializedName("largura")
    private int largura;

    @SerializedName("altura")
    private int altura;

    public String getImagemUrl() {
        return imagemUrl;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public String getFormato() {
        return formato;
    }

    public long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }
}