package com.example.pimobile;

public class Perfil {

    private final String nome;
    private final String email;
    private final String tipoConta; // "Freelancer" ou "Usuário"
    private final boolean verificado;
    private final int mensagensNaoLidas;
    private final String telefone;
    private final String empresa;
    private final String cidade;
    private final String estado;

    public Perfil(String nome, String email, String tipoConta,
                  boolean verificado, int mensagensNaoLidas,
                  String telefone, String empresa, String cidade, String estado) {
        this.nome = nome;
        this.email = email;
        this.tipoConta = tipoConta;
        this.verificado = verificado;
        this.mensagensNaoLidas = mensagensNaoLidas;
        this.telefone = telefone == null ? "" : telefone;
        this.empresa = empresa == null ? "" : empresa;
        this.cidade = cidade == null ? "" : cidade;
        this.estado = estado == null ? "" : estado;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTipoConta() { return tipoConta; }
    public boolean isVerificado() { return verificado; }
    public int getMensagensNaoLidas() { return mensagensNaoLidas; }
    public String getTelefone() { return telefone; }
    public String getEmpresa() { return empresa; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }

    public String getIniciais() {
        String nomeSeguro = nome == null || nome.trim().isEmpty() ? "U" : nome.trim();
        String[] partes = nomeSeguro.split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}
