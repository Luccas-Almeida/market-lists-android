package com.LuccasAlmeida.marketlists;

import com.google.gson.annotations.SerializedName;

public class Categoria {

    @SerializedName("id")
    private long id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("slug")
    private String slug;

    @SerializedName("ordem")
    private int ordem;

    @SerializedName("url")
    private String url;

    public Categoria() {
    }

    public Categoria(long id, String nome, String url) {
        this.id = id;
        this.nome = nome;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
    }

    public int getOrdem() {
        return ordem;
    }

    public String getUrl() {
        return url;
    }
}