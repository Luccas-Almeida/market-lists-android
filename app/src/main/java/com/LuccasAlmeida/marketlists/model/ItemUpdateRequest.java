package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class ItemUpdateRequest {

    @SerializedName("nome")
    private String nome;

    @SerializedName("imagem_url")
    private String imagemUrl;

    @SerializedName("cloudinary_public_id")
    private String cloudinaryPublicId;

    @SerializedName("preco_usuario")
    private BigDecimal precoUsuario;

    @SerializedName("quantidade")
    private Integer quantidade;

    @SerializedName("comprado")
    private Boolean comprado;

    public ItemUpdateRequest() {
    }

    public static ItemUpdateRequest comPreco(BigDecimal precoUsuario) {
        ItemUpdateRequest req = new ItemUpdateRequest();
        req.precoUsuario = precoUsuario;
        return req;
    }

    public static ItemUpdateRequest comQuantidade(Integer quantidade) {
        ItemUpdateRequest req = new ItemUpdateRequest();
        req.quantidade = quantidade;
        return req;
    }

    public static ItemUpdateRequest comComprado(Boolean comprado) {
        ItemUpdateRequest req = new ItemUpdateRequest();
        req.comprado = comprado;
        return req;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }

    public BigDecimal getPrecoUsuario() {
        return precoUsuario;
    }

    public void setPrecoUsuario(BigDecimal precoUsuario) {
        this.precoUsuario = precoUsuario;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Boolean getComprado() {
        return comprado;
    }

    public void setComprado(Boolean comprado) {
        this.comprado = comprado;
    }
}