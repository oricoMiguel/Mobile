package com.example.pimobile;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeedDados {

    public static void skillPods() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        pod(batch, db, "desenvolvimento-web", "Pod Desenvolvimento web",
                "Time completo para criar e publicar sites e sistemas web.", true,
                Arrays.asList("Front-end", "Back-end", "QA"),
                Arrays.asList("AS", "RP", "TC"), 3, "code", 0, 1);
        pod(batch, db, "design-ux", "Pod Design e UX",
                "Dupla para desenhar telas, fluxos e protótipos testáveis.", true,
                Arrays.asList("UI", "UX", "Protótipo"),
                Arrays.asList("CL", "MB"), 2, "palette", 1, 2);
        pod(batch, db, "marketing-digital", "Pod Marketing digital",
                "Equipe para lançar e divulgar sua marca nas redes.", true,
                Arrays.asList("Conteúdo", "Tráfego", "Social"),
                Arrays.asList("JM", "LF"), 4, "megaphone", 2, 3);
        pod(batch, db, "workshops", "Pod Workshops",
                "Equipe no seu endereço para oficinas e prototipação.", false,
                Arrays.asList("Facilitação", "Protótipo"),
                Arrays.asList("PS", "AM", "RT"), 3, "palette", 1, 4);

        batch.commit()
                .addOnSuccessListener(x -> Log.d("Seed", "skillPods gravados"))
                .addOnFailureListener(e -> Log.e("Seed", "falha ao gravar skillPods", e));
    }

    public static void freelancers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        freelancer(batch, db, "exemplo-ana-souza", "Ana Souza", "Desenvolvedora front-end",
                "Desenvolvimento", 4.9, Arrays.asList("React", "JavaScript", "CSS"),
                true, "Disponível agora");
        freelancer(batch, db, "exemplo-carlos-lima", "Carlos Lima", "Designer UI/UX",
                "Design", 4.7, Arrays.asList("Figma", "Protótipo", "Pesquisa"),
                true, "Disponível agora");
        freelancer(batch, db, "exemplo-julia-martins", "Julia Martins", "Marketing digital",
                "Marketing", 4.8, Arrays.asList("Conteúdo", "Tráfego pago"),
                false, "Livre a partir de segunda");

        batch.commit()
                .addOnSuccessListener(x -> Log.d("Seed", "freelancers gravados"))
                .addOnFailureListener(e -> Log.e("Seed", "falha ao gravar freelancers", e));
    }

    private static void pod(WriteBatch batch, FirebaseFirestore db, String id, String nome,
                            String descricao, boolean digital, List<String> tags,
                            List<String> membros, int totalPessoas, String icone,
                            int cor, int ordem) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome);
        dados.put("descricao", descricao);
        dados.put("digital", digital);
        dados.put("tags", tags);
        dados.put("membros", membros);
        dados.put("totalPessoas", totalPessoas);
        dados.put("icone", icone);
        dados.put("cor", cor);
        dados.put("ordem", ordem);
        batch.set(db.collection("skillPods").document(id), dados);
    }

    private static void freelancer(WriteBatch batch, FirebaseFirestore db, String id,
                                   String nome, String area, String categoria, double nota,
                                   List<String> habilidades, boolean disponivel,
                                   String disponibilidade) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome);
        dados.put("area", area);
        dados.put("categoria", categoria);
        dados.put("nota", nota);
        dados.put("habilidades", habilidades);
        dados.put("disponivel", disponivel);
        dados.put("disponibilidade", disponibilidade);
        batch.set(db.collection("freelancers").document(id), dados);
    }
}