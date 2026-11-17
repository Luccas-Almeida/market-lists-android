package com.LuccasAlmeida.marketlists.model;

import com.google.gson.annotations.SerializedName;

public class ResumoListaResponse {

    @SerializedName("total_itens")
    private Integer totalItens;

    @SerializedName("unidades_totais")
    private Integer unidadesTotais;

    @SerializedName("itens_comprados")
    private Integer itensComprados;

    @SerializedName("itens_pendentes")
    private Integer itensPendentes;

    @SerializedName("unidades_compradas")
    private Integer unidadesCompradas;

    @SerializedName("unidades_pendentes")
    private Integer unidadesPendentes;

    @SerializedName("itens_sem_preco")
    private Integer itensSemPreco;

    @SerializedName("total_estimado")
    private String totalEstimado;

    @SerializedName("total_comprado")
    private String totalComprado;

    @SerializedName("total_pendente")
    private String totalPendente;

    @SerializedName("lista_id")
    private Long listaId;

    @SerializedName("total_completo")
    private Boolean totalCompleto;

    public Integer getTotalItens() {
        return totalItens;
    }

    public Integer getUnidadesTotais() {
        return unidadesTotais;
    }

    public Integer getItensComprados() {
        return itensComprados;
    }

    public Integer getItensPendentes() {
        return itensPendentes;
    }

    public Integer getUnidadesCompradas() {
        return unidadesCompradas;
    }

    public Integer getUnidadesPendentes() {
        return unidadesPendentes;
    }

    public Integer getItensSemPreco() {
        return itensSemPreco;
    }

    public String getTotalEstimado() {
        return totalEstimado;
    }

    public String getTotalComprado() {
        return totalComprado;
    }

    public String getTotalPendente() {
        return totalPendente;
    }

    public Long getListaId() {
        return listaId;
    }

    public Boolean getTotalCompleto() {
        return totalCompleto;
    }
}