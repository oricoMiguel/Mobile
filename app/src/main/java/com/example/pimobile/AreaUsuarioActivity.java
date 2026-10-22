package com.example.pimobile;

import java.util.List;

public class AreaUsuarioActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de usuários"; }
    @Override protected String dicaBusca() { return "Buscar por nome ou email"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getUsuarios(callback);
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de usuários", "Freelancers", "Usuários", "Admins"};
    }

    @Override
    protected String[] valoresCards(List<RegistroAdm> lista) {
        return new String[]{
                String.valueOf(lista.size()),
                String.valueOf(contar(lista, 0)),
                String.valueOf(contar(lista, 2)),
                String.valueOf(contar(lista, 1))
        };
    }
}