package com.example.pimobile;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AreaFreelancerActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de freelancers"; }
    @Override protected String dicaBusca() { return "Buscar por nome ou especialidade"; }

    @Override
    protected List<RegistroAdm> carregarRegistros() {
        return AreaAdmRepository.getFreelancers();
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de freelancers", "Disponíveis", "Em projeto", "Faturamento"};
    }

    @Override
    protected String[] valoresCards(List<RegistroAdm> lista) {
        double faturamento = 0;
        for (RegistroAdm r : lista) {
            faturamento += r.getValor();
        }
        NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        moeda.setMaximumFractionDigits(0);

        return new String[]{
                String.valueOf(lista.size()),
                String.valueOf(contar(lista, 0)),
                String.valueOf(contar(lista, 1)),
                moeda.format(faturamento)
        };
    }
}