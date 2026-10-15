package com.example.pimobile;

public class AreaAdm {

    private final String titulo;
    private final String descricao;
    private final String resumo;
    private final int iconeRes;
    private final int fundoIconeRes;
    private final Class<?> destino;
    private final int pendentes;

    public AreaAdm(String titulo, String descricao, String resumo, int iconeRes,
                   int fundoIconeRes, Class<?> destino, int pendentes) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.resumo = resumo;
        this.iconeRes = iconeRes;
        this.fundoIconeRes = fundoIconeRes;
        this.destino = destino;
        this.pendentes = pendentes;
    }

    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getResumo() { return resumo; }
    public int getIconeRes() { return iconeRes; }
    public int getFundoIconeRes() { return fundoIconeRes; }
    public Class<?> getDestino() { return destino; }
    public int getPendentes() { return pendentes; }
}