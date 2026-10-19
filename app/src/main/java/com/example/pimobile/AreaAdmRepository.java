package com.example.pimobile;

import java.util.ArrayList;
import java.util.List;

public class AreaAdmRepository {

    // TODO: trocar os três métodos por dados reais (banco de dados ou API).
    // As telas só usam estes métodos, então nada mais precisa mudar.

    // Usuários: subtitulo = email | detalhe = cadastro e projetos
    public static List<RegistroAdm> getUsuarios() {
        List<RegistroAdm> lista = new ArrayList<>();
        lista.add(new RegistroAdm("Ana Souza", "ana.souza@email.com",
                "Cadastro 28/09/2026 · 3 projetos", "Ativo", 0, 0, 0));
        lista.add(new RegistroAdm("Carlos Lima", "carlos.lima@email.com",
                "Cadastro 02/10/2026 · 0 projetos", "Pendente", 1, 1, 0));
        lista.add(new RegistroAdm("Marina Barros", "marina.b@email.com",
                "Cadastro 14/08/2026 · 1 projeto", "Inativo", 2, 2, 0));
        return lista;
    }

    // Freelancers: subtitulo = especialidade | detalhe = avaliação, data e faturamento
    public static List<RegistroAdm> getFreelancers() {
        List<RegistroAdm> lista = new ArrayList<>();
        lista.add(new RegistroAdm("Ana Souza", "Desenvolvedora front-end",
                "★ 4,9 · Desde 12/03/2026 · R$ 8.400", "Disponível", 0, 0, 8400));
        lista.add(new RegistroAdm("Carlos Lima", "Designer UI/UX",
                "★ 4,7 · Desde 05/05/2026 · R$ 5.200", "Em projeto", 1, 1, 5200));
        lista.add(new RegistroAdm("Julia Martins", "Marketing digital",
                "★ 4,8 · Desde 20/07/2026 · R$ 3.900", "Disponível", 0, 2, 3900));
        return lista;
    }

    // Pods: subtitulo = membros | detalhe = data e projetos
    public static List<RegistroAdm> getPods() {
        List<RegistroAdm> lista = new ArrayList<>();
        lista.add(new RegistroAdm("Pod Desenvolvimento web", "3 membros",
                "Criado 10/06/2026 · 2 projetos", "Ativo", 0, 0, 0));
        lista.add(new RegistroAdm("Pod Design e UX", "2 membros",
                "Criado 22/07/2026 · 1 projeto", "Ocupado", 1, 1, 0));
        lista.add(new RegistroAdm("Pod Marketing digital", "4 membros",
                "Criado 03/04/2026 · 0 projetos", "Inativo", 2, 2, 0));
        return lista;
    }
}