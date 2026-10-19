package com.example.pimobile;

public class Perfil {

    private final String nome;
    private final String email;
    private final String tipoConta; // "Freelancer" ou "Usuário"
    private final boolean verificado;
    private final int mensagensNaoLidas;

    public Perfil(String nome, String email, String tipoConta,
                  boolean verificado, int mensagensNaoLidas) {
        this.nome = nome;
        this.email = email;
        this.tipoConta = tipoConta;
        this.verificado = verificado;
        this.mensagensNaoLidas = mensagensNaoLidas;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTipoConta() { return tipoConta; }
    public boolean isVerificado() { return verificado; }
    public int getMensagensNaoLidas() { return mensagensNaoLidas; }

    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}