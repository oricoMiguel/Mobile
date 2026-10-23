package com.example.pimobile;

import java.util.List;

public class PerfilProfissional {

    private final String nome;
    private final String area;
    private final String categoria;
    private final List<String> habilidades;
    private final boolean disponivel;
    private final String disponibilidade;
    private final String status; // pendente, aprovado ou recusado

    public PerfilProfissional(String nome, String area, String categoria, List<String> habilidades,
                              boolean disponivel, String disponibilidade, String status) {
        this.nome = nome;
        this.area = area;
        this.categoria = categoria;
        this.habilidades = habilidades;
        this.disponivel = disponivel;
        this.disponibilidade = disponibilidade;
        this.status = status;
    }

    public String getNome() { return nome; }
    public String getArea() { return area; }
    public String getCategoria() { return categoria; }
    public List<String> getHabilidades() { return habilidades; }
    public boolean isDisponivel() { return disponivel; }
    public String getDisponibilidade() { return disponibilidade; }
    public String getStatus() { return status; }
}