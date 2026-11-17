package com.LuccasAlmeida.marketlists;

import java.util.ArrayList;
import java.util.List;

public class NotaFiscal {
    private final String id;
    private final String estabelecimento;
    private final String data;
    private final double valorTotal;
    private final String urlSefaz;
    private final List<Produto> itens;

    public NotaFiscal(String id, String estabelecimento, String data, double valorTotal, String urlSefaz, List<Produto> itens) {
        this.id = id;
        this.estabelecimento = estabelecimento;
        this.data = data;
        this.valorTotal = valorTotal;
        this.urlSefaz = urlSefaz;
        this.itens = itens != null ? itens : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getEstabelecimento() {
        return estabelecimento;
    }

    public String getData() {
        return data;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public String getUrlSefaz() {
        return urlSefaz;
    }

    public List<Produto> getItens() {
        return itens;
    }
}