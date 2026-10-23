package com.example.pimobile;

public class Membro {

    private final String uid; // null só em dados antigos
    private final String nome;

    public Membro(String uid, String nome) {
        this.uid = uid;
        this.nome = nome;
    }

    public String getUid() { return uid; }
    public String getNome() { return nome; }

    public String getIniciais() {
        String limpo = nome == null ? "" : nome.trim();
        if (limpo.isEmpty()) return "?";
        String[] partes = limpo.split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }
}