package com.example.pimobile;

public class SkillPodResumo {

    private final String nome;
    private final int quantidadeEspecialistas;
    private final int iconeRes;
    private final int fundoIconeRes;

    public SkillPodResumo(String nome, int quantidadeEspecialistas, int iconeRes, int fundoIconeRes) {
        this.nome = nome;
        this.quantidadeEspecialistas = quantidadeEspecialistas;
        this.iconeRes = iconeRes;
        this.fundoIconeRes = fundoIconeRes;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidadeEspecialistas() {
        return quantidadeEspecialistas;
    }

    public int getIconeRes() {
        return iconeRes;
    }

    public int getFundoIconeRes() {
        return fundoIconeRes;
    }
}