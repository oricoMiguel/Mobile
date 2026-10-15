package com.example.pimobile;

import java.util.List;

public class SkillPod {

    // Cor do card: 0 = roxo, 1 = verde, 2 = laranja
    private final String nome;
    private final String descricao;
    private final boolean digital;
    private final List<String> tags;
    private final List<String> membros; // iniciais dos membros
    private final int totalPessoas;
    private final int iconeRes;
    private final int cor;

    public SkillPod(String nome, String descricao, boolean digital, List<String> tags,
                    List<String> membros, int totalPessoas, int iconeRes, int cor) {
        this.nome = nome;
        this.descricao = descricao;
        this.digital = digital;
        this.tags = tags;
        this.membros = membros;
        this.totalPessoas = totalPessoas;
        this.iconeRes = iconeRes;
        this.cor = cor;
    }

    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public boolean isDigital() { return digital; }
    public List<String> getTags() { return tags; }
    public List<String> getMembros() { return membros; }
    public int getTotalPessoas() { return totalPessoas; }
    public int getIconeRes() { return iconeRes; }
    public int getCor() { return cor; }
}