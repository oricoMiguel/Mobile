package com.example.pimobile;

import java.util.List;
import java.util.Locale;

public class AreaFreelancerActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de freelancers"; }
    @Override protected String dicaBusca() { return "Buscar por nome ou especialidade"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getFreelancers(callback);
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de freelancers", "Disponíveis", "Indisponíveis", "Nota média"};
    }

    @Override
    protected String[] valoresCards(List<RegistroAdm> lista) {
        String media = "–";
        if (!lista.isEmpty()) {
            double soma = 0;
            for (RegistroAdm r : lista) {
                soma += r.getValor();
            }
            media = String.format(new Locale("pt", "BR"), "%.1f", soma / lista.size());
        }

        return new String[]{
                String.valueOf(lista.size()),
                String.valueOf(contar(lista, 0)),
                String.valueOf(contar(lista, 1)),
                media
        };
    }
}