package com.example.pimobile;

public class Notificacao {

    private final String id;
    private final String titulo;
    private final String mensagem;
    private boolean lida;
    private final long criadoEm; // milissegundos; 0 se ainda sem data

    public Notificacao(String id, String titulo, String mensagem, boolean lida, long criadoEm) {
        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.lida = lida;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
    public long getCriadoEm() { return criadoEm; }
}