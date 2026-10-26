package com.example.pimobile;

import java.util.List;

public class AreaPodsActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de skill pods"; }
    @Override protected String dicaBusca() { return "Buscar skill pod"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getPods(callback);
    }
}