package com.example.pimobile;

public class RegistroAdm {

    // badgeTipo: 0 = verde (ativo/disponível), 1 = laranja (pendente/ocupado/em projeto), 2 = cinza (inativo)
    // cor do avatar: 0 = roxo, 1 = verde, 2 = laranja
    private final String nome;
    private final String subtitulo;
    private final String detalhe;
    private final String badge;
    private final int badgeTipo;
    private final int cor;
    private final double valor; // usado para somar o faturamento

    public RegistroAdm(String nome, String subtitulo, String detalhe, String badge,
                       int badgeTipo, int cor, double valor) {
        this.nome = nome;
        this.subtitulo = subtitulo;
        this.detalhe = detalhe;
        this.badge = badge;
        this.badgeTipo = badgeTipo;
        this.cor = cor;
        this.valor = valor;
    }

    public String getNome() { return nome; }
    public String getSubtitulo() { return subtitulo; }
    public String getDetalhe() { return detalhe; }
    public String getBadge() { return badge; }
    public int getBadgeTipo() { return badgeTipo; }
    public int getCor() { return cor; }
    public double getValor() { return valor; }

    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}