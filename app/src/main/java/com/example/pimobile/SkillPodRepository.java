package com.example.pimobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillPodRepository {

    // TODO: quando houver dados reais (banco de dados ou API),
    // buscar aqui os mesmos dados e ordenar pelos mais populares.
    // A Home e a SkillPodsActivity usam esta mesma fonte.
    public static List<SkillPodResumo> getPopulares() {
        List<SkillPodResumo> lista = new ArrayList<>();
        lista.add(new SkillPodResumo("Desenvolvimento", 4, R.drawable.ic_code, R.drawable.bg_icone_roxo));
        lista.add(new SkillPodResumo("Design e UX", 3, R.drawable.ic_palette, R.drawable.bg_icone_verde));
        return lista;
    }

    // TODO: trocar por dados reais (banco de dados ou API).
    // A SkillPodsActivity só usa este método, então nada mais precisa mudar.
    public static List<SkillPod> getTodos() {
        List<SkillPod> lista = new ArrayList<>();
        lista.add(new SkillPod("Pod Desenvolvimento web",
                "Time completo para criar e publicar sites e sistemas web.", true,
                Arrays.asList("Front-end", "Back-end", "QA"),
                Arrays.asList("AS", "RP", "TC"), 3, R.drawable.ic_code, 0));
        lista.add(new SkillPod("Pod Design e UX",
                "Dupla para desenhar telas, fluxos e protótipos testáveis.", true,
                Arrays.asList("UI", "UX", "Protótipo"),
                Arrays.asList("CL", "MB"), 2, R.drawable.ic_palette, 1));
        lista.add(new SkillPod("Pod Marketing digital",
                "Equipe para lançar e divulgar sua marca nas redes.", true,
                Arrays.asList("Conteúdo", "Tráfego", "Social"),
                Arrays.asList("JM", "LF"), 4, R.drawable.ic_megaphone, 2));
        lista.add(new SkillPod("Pod Workshops",
                "Equipe no seu endereço para oficinas e prototipação.", false,
                Arrays.asList("Facilitação", "Protótipo"),
                Arrays.asList("PS", "AM", "RT"), 3, R.drawable.ic_palette, 1));
        return lista;
    }
}