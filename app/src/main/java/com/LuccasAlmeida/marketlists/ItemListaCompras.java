package com.LuccasAlmeida.marketlists;

public class ItemListaCompras {
    private final Produto produto;
    private int quantidade;
    private boolean pego;

    public ItemListaCompras(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.pego = false;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public boolean isPego() {
        return pego;
    }

    public void setPego(boolean pego) {
        this.pego = pego;
    }

    public void togglePego() {
        this.pego = !this.pego;
    }

    public double getValorTotalItem() {
        return produto != null ? produto.getPreco() * quantidade : 0.0;
    }
}