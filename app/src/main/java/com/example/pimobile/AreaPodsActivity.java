package com.example.pimobile;

import java.util.List;

public class AreaPodsActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de skill pods"; }
    @Override protected String dicaBusca() { return "Buscar skill pod"; }

    @Override
    protected List<RegistroAdm> carregarRegistros() {
        return AreaAdmRepository.getPods();
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de pods", "Ativos", "Ocupados", "Inativos"};
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