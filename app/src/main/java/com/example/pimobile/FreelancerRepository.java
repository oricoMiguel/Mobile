package com.example.pimobile;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FreelancerRepository {

    private static final String COLECAO = "freelancers";

    public static void getTodos(Callback<List<Freelancer>> callback) {
        FirebaseFirestore.getInstance().collection(COLECAO)
                .orderBy("nome")
                .get()
                .addOnSuccessListener(query -> {
                    List<Freelancer> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        lista.add(paraFreelancer(doc));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    private static Freelancer paraFreelancer(DocumentSnapshot doc) {
        String nome = doc.getString("nome");
        if (nome == null || nome.trim().isEmpty()) nome = "Sem nome";

        String area = doc.getString("area");
        if (area == null) area = "";

        String categoria = doc.getString("categoria");
        if (categoria == null || categoria.trim().isEmpty()) categoria = "Outros";

        Double nota = doc.getDouble("nota");
        boolean disponivel = Boolean.TRUE.equals(doc.getBoolean("disponivel"));

        String disponibilidade = doc.getString("disponibilidade");
        if (disponibilidade == null) {
            disponibilidade = disponivel ? "Disponível agora" : "Indisponível";
        }

        return new Freelancer(
                nome,
                area,
                categoria,
                nota == null ? 0.0 : nota,
                listaDeTexto(doc.get("habilidades")),
                disponivel,
                disponibilidade,
                corPorCategoria(categoria));
    }

    private static List<String> listaDeTexto(Object valor) {
        List<String> resultado = new ArrayList<>();
        if (valor instanceof List<?>) {
            for (Object item : (List<?>) valor) {
                if (item != null) resultado.add(item.toString());
            }
        }
        return resultado;
    }

    private static int corPorCategoria(String categoria) {
        switch (categoria) {
            case "Desenvolvimento": return 0;
            case "Design":          return 1;
            case "Marketing":       return 2;
            default:                return Math.abs(categoria.hashCode()) % 3;
        }
    }
}