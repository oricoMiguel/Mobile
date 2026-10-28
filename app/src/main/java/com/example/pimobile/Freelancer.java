package com.example.pimobile;

import java.util.List;

public class Freelancer {

    // Cores do card: 0 = roxo, 1 = verde, 2 = laranja
    private final String id;
    private final String nome;
    private final String area;
    private final String categoria;
    private final double nota;
    private final List<String> habilidades;
    private final boolean disponivel;
    private final String disponibilidade;
    private final int cor;
    private final long criadoEm; // milissegundos; 0 se não houver data
    private final String whatsapp;
    private final String instagram;
    private final String telefone;
    private final String emailContato;

    public Freelancer(String id, String nome, String area, String categoria, double nota,
                      List<String> habilidades, boolean disponivel,
                      String disponibilidade, int cor, long criadoEm,
                      String whatsapp, String instagram, String telefone, String emailContato) {
        this.id = id;
        this.nome = nome;
        this.area = area;
        this.categoria = categoria;
        this.nota = nota;
        this.habilidades = habilidades;
        this.disponivel = disponivel;
        this.disponibilidade = disponibilidade;
        this.cor = cor;
        this.criadoEm = criadoEm;
        this.whatsapp = whatsapp == null ? "" : whatsapp;
        this.instagram = instagram == null ? "" : instagram;
        this.telefone = telefone == null ? "" : telefone;
        this.emailContato = emailContato == null ? "" : emailContato;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getArea() { return area; }
    public String getCategoria() { return categoria; }
    public double getNota() { return nota; }
    public List<String> getHabilidades() { return habilidades; }
    public boolean isDisponivel() { return disponivel; }
    public String getDisponibilidade() { return disponibilidade; }
    public int getCor() { return cor; }
    public long getCriadoEm() { return criadoEm; }
    public String getWhatsapp() { return whatsapp; }
    public String getInstagram() { return instagram; }
    public String getTelefone() { return telefone; }
    public String getEmailContato() { return emailContato; }

    public String getIniciais() {
        String limpo = nome == null ? "" : nome.trim();
        if (limpo.isEmpty()) return "?";
        String[] partes = limpo.split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}