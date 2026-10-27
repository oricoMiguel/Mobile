package com.example.pimobile;

// Um RegistroAdm que também carrega a descrição da proposta, para o admin ler antes de decidir
public class PropostaPodRegistro extends RegistroAdm {

    private final String descricao;

    public PropostaPodRegistro(String id, String nome, String subtitulo, String detalhe,
                               String badge, int badgeTipo, int cor, String descricao) {
        super(id, nome, subtitulo, detalhe, badge, badgeTipo, cor, 0);
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}
