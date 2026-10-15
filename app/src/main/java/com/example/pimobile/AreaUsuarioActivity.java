package com.example.pimobile;

import java.util.List;

public class AreaUsuarioActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de usuários"; }
    @Override protected String dicaBusca() { return "Buscar por nome ou email"; }

    @Override
    protected List<RegistroAdm> carregarRegistros() {
        return AreaAdmRepository.getUsuarios();
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de usuários", "Ativos", "Pendentes", "Inativos"};
    }

    @Override
    protected String[] valoresCards(List<RegistroAdm> lista) {
        return new String[]{
                String.valueOf(lista.size()),
                String.valueOf(contar(lista, 0)),
                String.valueOf(contar(lista, 1)),
                String.valueOf(contar(lista, 2))
        };
    }
}