
package com.example.pimobile;

public class RegistroAdm {

    private final String id;
    private final String nome;
    private final String subtitulo;
    private final String detalhe;
    private final String badge;
    private final int badgeTipo;
    private final int cor;
    private final double valor;

    // Dados do contratante
    private final String contratanteId;
    private final String fotoUrl;
    private final String email;
    private final String telefone;
    private final String empresa;
    private final String cidade;
    private final String estado;

    // Construtor antigo: preserva as outras telas
    public RegistroAdm(
            String id,
            String nome,
            String subtitulo,
            String detalhe,
            String badge,
            int badgeTipo,
            int cor,
            double valor) {

        this(id, nome, subtitulo, detalhe, badge,
                badgeTipo, cor, valor,
                null, null, null, null, null, null, null);
    }

    // Novo construtor com dados do contratante
    public RegistroAdm(
            String id,
            String nome,
            String subtitulo,
            String detalhe,
            String badge,
            int badgeTipo,
            int cor,
            double valor,
            String contratanteId,
            String fotoUrl,
            String email,
            String telefone,
            String empresa,
            String cidade,
            String estado) {

        this.id = id;
        this.nome = nome;
        this.subtitulo = subtitulo;
        this.detalhe = detalhe;
        this.badge = badge;
        this.badgeTipo = badgeTipo;
        this.cor = cor;
        this.valor = valor;

        this.contratanteId = contratanteId;
        this.fotoUrl = fotoUrl;
        this.email = email;
        this.telefone = telefone;
        this.empresa = empresa;
        this.cidade = cidade;
        this.estado = estado;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getSubtitulo() { return subtitulo; }
    public String getDetalhe() { return detalhe; }
    public String getBadge() { return badge; }
    public int getBadgeTipo() { return badgeTipo; }
    public int getCor() { return cor; }
    public double getValor() { return valor; }

    public String getContratanteId() { return contratanteId; }
    public String getFotoUrl() { return fotoUrl; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public String getEmpresa() { return empresa; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }

    public String getIniciais() {
        String limpo = nome == null ? "" : nome.trim();

        if (limpo.isEmpty()) return "?";

        String[] partes = limpo.split("\\s+");

        String primeira = partes[0].substring(0, 1);

        String ultima = partes.length > 1
                ? partes[partes.length - 1].substring(0, 1)
                : "";

        return (primeira + ultima).toUpperCase();
    }
}
