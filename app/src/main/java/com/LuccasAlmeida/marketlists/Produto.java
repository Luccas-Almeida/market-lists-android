package com.LuccasAlmeida.marketlists;

public class Produto {
    private final int id;
    private String nome;
    private double preco;
    private String urlImagem;
    private int idCategoria;
    private boolean travarPrecoNota;

    public Produto(int id, String nome, double preco, String urlImagem) {
        this(id, nome, preco, urlImagem, 0, false);
    }

    public Produto(int id, String nome, double preco, String urlImagem, int idCategoria) {
        this(id, nome, preco, urlImagem, idCategoria, false);
    }

    public Produto(int id, String nome, double preco, String urlImagem, int idCategoria, boolean travarPrecoNota) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.urlImagem = urlImagem;
        this.idCategoria = idCategoria;
        this.travarPrecoNota = travarPrecoNota;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public boolean isTravarPrecoNota() {
        return travarPrecoNota;
    }

    public void setTravarPrecoNota(boolean travarPrecoNota) {
        this.travarPrecoNota = travarPrecoNota;
    }
}