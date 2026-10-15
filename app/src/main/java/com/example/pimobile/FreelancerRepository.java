package com.example.pimobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FreelancerRepository {

    // TODO: trocar por dados reais (banco de dados ou API).
    // A tela ServicoActivity só usa este método, então nada mais precisa mudar.
    public static List<Freelancer> getTodos() {
        List<Freelancer> lista = new ArrayList<>();
        lista.add(new Freelancer("Ana Souza", "Desenvolvedora front-end", "Desenvolvimento", 4.9,
                Arrays.asList("React", "JavaScript", "CSS"), true, "Disponível agora", 0));
        lista.add(new Freelancer("Carlos Lima", "Designer UI/UX", "Design", 4.7,
                Arrays.asList("Figma", "Protótipo", "Pesquisa"), true, "Disponível agora", 1));
        lista.add(new Freelancer("Julia Martins", "Marketing digital", "Marketing", 4.8,
                Arrays.asList("Conteúdo", "Tráfego pago"), false, "Livre a partir de segunda", 2));
        return lista;
    }
}