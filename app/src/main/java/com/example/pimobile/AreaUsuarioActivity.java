package com.example.pimobile;

import java.util.List;

public class AreaUsuarioActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de usuários"; }
    @Override protected String dicaBusca() { return "Buscar por nome ou email"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getUsuarios(callback);
    }
}