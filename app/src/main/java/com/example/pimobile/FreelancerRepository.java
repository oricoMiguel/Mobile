package com.example.pimobile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreelancerRepository {

    private static final String COLECAO = "freelancers";

    // Só os perfis aprovados aparecem na lista de Serviços
    public static void getTodos(Callback<List<Freelancer>> callback) {
        FirebaseFirestore.getInstance().collection(COLECAO)
                .whereEqualTo("status", "aprovado")
                .get()
                .addOnSuccessListener(query -> {
                    List<Freelancer> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        lista.add(paraFreelancer(doc));
                    }
                    Collections.sort(lista, (a, b) -> a.getNome().compareToIgnoreCase(b.getNome()));
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // Perfil profissional do usuário logado (null se ele ainda não criou)
    public static void getMeuPerfil(Callback<PerfilProfissional> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance().collection(COLECAO)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onSucesso(null);
                        return;
                    }

                    String nome = doc.getString("nome");
                    String area = doc.getString("area");
                    String categoria = doc.getString("categoria");
                    String disponibilidade = doc.getString("disponibilidade");
                    String status = doc.getString("status");

                    callback.onSucesso(new PerfilProfissional(
                            nome == null ? "" : nome,
                            area == null ? "" : area,
                            categoria == null ? "Outros" : categoria,
                            listaDeTexto(doc.get("habilidades")),
                            Boolean.TRUE.equals(doc.getBoolean("disponivel")),
                            disponibilidade == null ? "" : disponibilidade,
                            status == null ? "pendente" : status));
                })
                .addOnFailureListener(callback::onErro);
    }

    // Cria ou atualiza o perfil. O callback devolve true quando o perfil precisa
    // (de novo) da aprovação de um admin.
    public static void salvarMeuPerfil(String nome, boolean nomeMudou, String area, String categoria,
                                       List<String> habilidades, boolean disponivel,
                                       String disponibilidade, PerfilProfissional atual,
                                       Callback<Boolean> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(COLECAO).document(user.getUid());
        WriteBatch batch = db.batch();

        final boolean precisaAprovacao = atual == null
                || !nome.equals(atual.getNome())
                || !area.equals(atual.getArea())
                || !categoria.equals(atual.getCategoria())
                || !habilidades.equals(atual.getHabilidades());

        Map<String, Object> campos = new HashMap<>();
        campos.put("nome", nome);
        campos.put("area", area);
        campos.put("categoria", categoria);
        campos.put("habilidades", habilidades);
        campos.put("disponivel", disponivel);
        campos.put("disponibilidade", disponibilidade);

        if (atual == null) {
            campos.put("status", "pendente");
            campos.put("criadoEm", FieldValue.serverTimestamp());
            batch.set(ref, campos);
        } else {
            if (precisaAprovacao) {
                campos.put("status", "pendente");
            }
            batch.update(ref, campos);
        }

        if (nomeMudou) {
            batch.update(db.collection("usuarios").document(user.getUid()), "nome", nome);
        }

        batch.commit()
                .addOnSuccessListener(x -> callback.onSucesso(precisaAprovacao))
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
            default:                return (categoria.hashCode() & 0x7fffffff) % 3;
        }
    }
}