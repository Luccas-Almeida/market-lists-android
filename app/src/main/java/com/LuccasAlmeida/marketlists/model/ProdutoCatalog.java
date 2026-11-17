package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class ProdutoCatalog {

    @SerializedName("id")
    private long id;

    @SerializedName("categoria_id")
    private Long categoriaId;

    @SerializedName("nome")
    private String nome;

    @SerializedName("imagem_url")
    private String imagemUrl;

    @SerializedName("cloudinary_public_id")
    private String cloudinaryPublicId;

    @SerializedName("imagem_enviada_pelo_usuario")
    private Boolean imagemEnviadaPeloUsuario;

    @SerializedName("preco")
    private double preco;

    @SerializedName("criado_em")
    private String criadoEm;

    public long getId() {
        return id;
    }

    public Long getCategoriaId() {
        return categoriaId;
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

    public Boolean getImagemEnviadaPeloUsuario() {
        return imagemEnviadaPeloUsuario;
    }

    public double getPreco() {
        return preco;
    }

    public String getCriadoEm() {
        return criadoEm;
    }
}