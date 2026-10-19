package com.example.pimobile;

import java.util.ArrayList;
import java.util.List;

public class AdmRepository {

    // TODO: trocar por dados reais (banco de dados ou API).
    // A AdmActivity só usa este método, então nada mais precisa mudar.
    // O total de "itens para revisar" é a soma dos pendentes de todas as áreas.
    public static List<AreaAdm> getAreas() {
        List<AreaAdm> lista = new ArrayList<>();
        lista.add(new AreaAdm("Área do usuário", "Gerencie contas de usuários",
                "128 usuários · 12 pendentes",
                R.drawable.ic_users_purple, R.drawable.bg_icone_roxo,
                AreaUsuarioActivity.class, 12));
        lista.add(new AreaAdm("Área dos freelancers",
                "Freelancers, projetos e pagamentos",
                "64 freelancers · 7 pendentes",
                R.drawable.ic_briefcase, R.drawable.bg_icone_verde,
                AreaFreelancerActivity.class, 7));
        lista.add(new AreaAdm("Gestão de pods", "Organize e monitore os skill pods",
                "24 pods · 5 em formação",
                R.drawable.ic_pods_laranja, R.drawable.bg_icone_laranja,
                AreaPodsActivity.class, 0));
        return lista;
    }
}