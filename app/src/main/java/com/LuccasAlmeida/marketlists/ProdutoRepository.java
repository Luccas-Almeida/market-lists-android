package com.LuccasAlmeida.marketlists;

import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {

    private static ProdutoRepository instance;
    private final List<NotaFiscal> notasSalvas = new ArrayList<>();
    private final List<Produto> catalogoProdutos = new ArrayList<>();
    private final List<ListaDeCompras> listasDeCompras = new ArrayList<>();
    private final List<CompraFinalizada> comprasFinalizadas = new ArrayList<>();

    private ProdutoRepository() {
        // Inicialização limpa: catálogo, notas e compras pertencem exclusivamente ao usuário
    }

    public static synchronized ProdutoRepository getInstance() {
        if (instance == null) {
            instance = new ProdutoRepository();
        }
        return instance;
    }

    public List<NotaFiscal> getNotasSalvas() {
        return notasSalvas;
    }

    public List<Produto> getCatalogoProdutos() {
        return catalogoProdutos;
    }

    public List<ListaDeCompras> getListasDeCompras() {
        return listasDeCompras;
    }

    public List<CompraFinalizada> getComprasFinalizadas() {
        return comprasFinalizadas;
    }

    public boolean removerCompraFinalizada(String idCompra) {
        if (idCompra == null) return false;
        for (int i = 0; i < comprasFinalizadas.size(); i++) {
            if (comprasFinalizadas.get(i).getId().equalsIgnoreCase(idCompra)) {
                comprasFinalizadas.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removerProduto(int produtoId) {
        for (int i = 0; i < catalogoProdutos.size(); i++) {
            if (catalogoProdutos.get(i).getId() == produtoId) {
                catalogoProdutos.remove(i);
                return true;
            }
        }
        return false;
    }

    public void editarProduto(int produtoId, String novoNome, double novoPreco, String novaUrl, boolean travarPreco) {
        for (Produto p : catalogoProdutos) {
            if (p.getId() == produtoId) {
                if (novoNome != null && !novoNome.trim().isEmpty()) {
                    p.setNome(novoNome.trim());
                }
                if (novoPreco >= 0) {
                    p.setPreco(novoPreco);
                }
                if (novaUrl != null && !novaUrl.trim().isEmpty()) {
                    p.setUrlImagem(novaUrl.trim());
                }
                p.setTravarPrecoNota(travarPreco);
                break;
            }
        }
    }

    public CompraFinalizada finalizarCompraDaLista(ListaDeCompras lista, String estabelecimento) {
        if (lista == null) return null;

        List<ItemListaCompras> itensPegos = new ArrayList<>();
        double valorTotalPego = 0.0;

        for (ItemListaCompras item : lista.getItens()) {
            if (item.isPego()) {
                itensPegos.add(item);
                valorTotalPego += item.getValorTotalItem();
            }
        }

        if (itensPegos.isEmpty()) {
            return null;
        }

        String idCompra = "compra-" + System.currentTimeMillis();
        String nomeMercado = (estabelecimento != null && !estabelecimento.trim().isEmpty()) ? estabelecimento : "Mercado / Feira";
        CompraFinalizada novaCompra = new CompraFinalizada(
                idCompra,
                lista.getNome(),
                "26/10/2026",
                valorTotalPego,
                nomeMercado,
                itensPegos
        );

        comprasFinalizadas.add(0, novaCompra);
        return novaCompra;
    }

    public ListaDeCompras criarListaDeCompras(String nome) {
        String id = "lista-" + System.currentTimeMillis();
        ListaDeCompras novaLista = new ListaDeCompras(id, nome);
        listasDeCompras.add(0, novaLista);
        return novaLista;
    }

    public ListaDeCompras buscarListaPorId(String id) {
        for (ListaDeCompras lista : listasDeCompras) {
            if (lista.getId().equalsIgnoreCase(id)) {
                return lista;
            }
        }
        return null;
    }

    public void adicionarNota(NotaFiscal nota) {
        if (nota != null) {
            notasSalvas.add(0, nota);
            if (nota.getItens() != null) {
                for (Produto p : nota.getItens()) {
                    adicionarOuAtualizarProduto(p);
                }
            }
        }
    }

    public void adicionarOuAtualizarProduto(Produto novoProduto) {
        if (novoProduto == null) return;

        for (Produto p : catalogoProdutos) {
            if (p.getId() == novoProduto.getId() || p.getNome().equalsIgnoreCase(novoProduto.getNome())) {
                if (!p.isTravarPrecoNota()) {
                    p.setPreco(novoProduto.getPreco());
                }
                return;
            }
        }
        catalogoProdutos.add(novoProduto);
    }

    public List<Produto> getProdutosPorCategoria(int idCategoria) {
        List<Produto> listaFiltrada = new ArrayList<>();
        for (Produto p : catalogoProdutos) {
            if (p.getIdCategoria() == idCategoria || idCategoria == 0) {
                listaFiltrada.add(p);
            }
        }
        return listaFiltrada;
    }

    public List<Produto> buscarProdutosPorNome(String query) {
        List<Produto> listaFiltrada = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(catalogoProdutos);
        }
        String busca = query.toLowerCase().trim();
        for (Produto p : catalogoProdutos) {
            if (p.getNome().toLowerCase().contains(busca)) {
                listaFiltrada.add(p);
            }
        }
        return listaFiltrada;
    }

    public NotaFiscal buscarNotaPorId(String id) {
        for (NotaFiscal nota : notasSalvas) {
            if (nota.getId().equalsIgnoreCase(id)) {
                return nota;
            }
        }
        return null;
    }

    public void setTravarPrecoProduto(int produtoId, boolean travar) {
        for (Produto p : catalogoProdutos) {
            if (p.getId() == produtoId) {
                p.setTravarPrecoNota(travar);
                break;
            }
        }
    }
}