package com.example.pimobile;

import java.util.List;

public class Freelancer {

    // Cores do card: 0 = roxo, 1 = verde, 2 = laranja
    private final String nome;
    private final String area;
    private final String categoria;
    private final double nota;
    private final List<String> habilidades;
    private final boolean disponivel;
    private final String disponibilidade;
    private final int cor;

    public Freelancer(String nome, String area, String categoria, double nota,
                      List<String> habilidades, boolean disponivel,
                      String disponibilidade, int cor) {
        this.nome = nome;
        this.area = area;
        this.categoria = categoria;
        this.nota = nota;
        this.habilidades = habilidades;
        this.disponivel = disponivel;
        this.disponibilidade = disponibilidade;
        this.cor = cor;
    }

    public String getNome() { return nome; }
    public String getArea() { return area; }
    public String getCategoria() { return categoria; }
    public double getNota() { return nota; }
    public List<String> getHabilidades() { return habilidades; }
    public boolean isDisponivel() { return disponivel; }
    public String getDisponibilidade() { return disponibilidade; }
    public int getCor() { return cor; }

    public String getIniciais() {
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}