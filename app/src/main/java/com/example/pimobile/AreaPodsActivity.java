package com.example.pimobile;

import java.util.List;

public class AreaPodsActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de skill pods"; }
    @Override protected String dicaBusca() { return "Buscar skill pod"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getPods(callback);
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de pods", "Digitais", "Presenciais", "Pessoas"};
    }

    @Override
    protected String[] valoresCards(List<RegistroAdm> lista) {
        int pessoas = 0;
        for (RegistroAdm r : lista) {
            pessoas += (int) r.getValor();
        }
        return new String[]{
                String.valueOf(lista.size()),
                String.valueOf(contar(lista, 0)),
                String.valueOf(contar(lista, 1)),
                String.valueOf(pessoas)
        };
    }
}