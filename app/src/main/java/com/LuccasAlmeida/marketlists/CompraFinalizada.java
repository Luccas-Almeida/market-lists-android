package com.LuccasAlmeida.marketlists;

import java.util.ArrayList;
import java.util.List;

public class CompraFinalizada {
    private final String id;
    private final String nomeLista;
    private final String data;
    private final double valorTotal;
    private final String estabelecimento;
    private final List<ItemListaCompras> itensComprados;

    public CompraFinalizada(String id, String nomeLista, String data, double valorTotal, String estabelecimento, List<ItemListaCompras> itensComprados) {
        this.id = id;
        this.nomeLista = nomeLista;
        this.data = data;
        this.valorTotal = valorTotal;
        this.estabelecimento = estabelecimento;
        this.itensComprados = itensComprados != null ? itensComprados : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getNomeLista() {
        return nomeLista;
    }

    public String getData() {
        return data;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public String getEstabelecimento() {
        return estabelecimento;
    }

    public List<ItemListaCompras> getItensComprados() {
        return itensComprados;
    }
}