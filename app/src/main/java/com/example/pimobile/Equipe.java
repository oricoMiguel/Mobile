package com.example.pimobile;

public class Equipe {

    private final String id;
    private final String formato; // "skillPod" ou "freelancer"
    private final String podNome; // nome do pod ou freelancer contratado
    private final int pessoas;
    private final String tipo;
    private final String status;
    private final long criadoEm; // milissegundos; 0 se ainda sem data
    private final String grupoEquipeId; // Identifica pedidos que pertencem à mesma equipe

    // Construtor antigo: mantém compatibilidade com o restante do aplicativo
    public Equipe(String id, String formato, String podNome, int pessoas, String tipo,
                  String status, long criadoEm) {

        this(id, formato, podNome, pessoas, tipo, status, criadoEm, null);
    }

    // Novo construtor: recebe também o grupoEquipeId
    public Equipe(String id, String formato, String podNome, int pessoas, String tipo,
                  String status, long criadoEm, String grupoEquipeId) {

        this.id = id;
        this.formato = formato;
        this.podNome = podNome;
        this.pessoas = pessoas;
        this.tipo = tipo;
        this.status = status;
        this.criadoEm = criadoEm;
        this.grupoEquipeId = grupoEquipeId;
    }

    public String getId() {
        return id;
    }

    public String getFormato() {
        return formato;
    }

    public boolean isFreelancer() {
        return "freelancer".equals(formato);
    }

    public String getPodNome() {
        return podNome;
    }

    public int getPessoas() {
        return pessoas;
    }

    public String getTipo() {
        return tipo;
    }

    public String getStatus() {
        return status;
    }

    public long getCriadoEm() {
        return criadoEm;
    }

    public String getGrupoEquipeId() {
        return grupoEquipeId;
    }

    public boolean pertenceAGrupo() {
        return grupoEquipeId != null && !grupoEquipeId.isEmpty();
    }
}