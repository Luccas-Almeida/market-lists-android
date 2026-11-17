package com.LuccasAlmeida.marketlists;

import java.util.ArrayList;
import java.util.List;

public class ListaDeCompras {
    private final String id;
    private String nome;
    private final List<ItemListaCompras> itens;

    public ListaDeCompras(String id, String nome) {
        this.id = id;
        this.nome = nome;
        this.itens = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<ItemListaCompras> getItens() {
        return itens;
    }

    public void adicionarProduto(Produto produto) {
        if (produto == null) return;
        for (ItemListaCompras item : itens) {
            if (item.getProduto().getId() == produto.getId()) {
                item.setQuantidade(item.getQuantidade() + 1);
                return;
            }
        }
        itens.add(new ItemListaCompras(produto, 1));
    }

    public void decrementarProduto(Produto produto) {
        if (produto == null) return;
        for (int i = 0; i < itens.size(); i++) {
            ItemListaCompras item = itens.get(i);
            if (item.getProduto().getId() == produto.getId()) {
                if (item.getQuantidade() > 1) {
                    item.setQuantidade(item.getQuantidade() - 1);
                } else {
                    itens.remove(i); // Quando estiver em 1 e decrementar, remove o item
                }
                return;
            }
        }
    }

    public double getValorTotalLista() {
        double total = 0.0;
        for (ItemListaCompras item : itens) {
            total += item.getValorTotalItem();
        }
        return total;
    }

    public int getQuantidadeTotalItens() {
        int total = 0;
        for (ItemListaCompras item : itens) {
            total += item.getQuantidade();
        }
        return total;
    }
}