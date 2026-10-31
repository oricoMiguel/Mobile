package com.example.pimobile;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Repositório de avaliações reais de freelancers. */
public class AvaliacaoRepository {
    private static final String AVALIACOES = "avaliacoes";
    private static final String EQUIPES = "equipes";

    public static void aplicarMedias(List<Freelancer> freelancers, Callback<Void> callback) {
        FirebaseFirestore.getInstance().collection(AVALIACOES).get()
                .addOnSuccessListener(query -> {
                    Map<String, Double> somas = new HashMap<>();
                    Map<String, Integer> quantidades = new HashMap<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getString("freelancerId");
                        Object valorNota = doc.get("nota");
                        Number nota = valorNota instanceof Number ? (Number) valorNota : null;
                        if (id == null || nota == null) continue;
                        somas.put(id, somas.getOrDefault(id, 0.0) + nota.doubleValue());
                        quantidades.put(id, quantidades.getOrDefault(id, 0) + 1);
                    }
                    for (Freelancer freelancer : freelancers) {
                        int quantidade = quantidades.getOrDefault(freelancer.getId(), 0);
                        freelancer.setNota(quantidade == 0 ? 0.0
                                : somas.get(freelancer.getId()) / quantidade);
                    }
                    callback.onSucesso(null);
                })
                .addOnFailureListener(callback::onErro);
    }

    /** Retorna um pedido concluído pelo cliente atual, necessário para liberar a avaliação. */
    public static void getPedidoConcluido(String freelancerId, Callback<String> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Entre na sua conta para avaliar."));
            return;
        }
        FirebaseFirestore.getInstance().collection(EQUIPES)
                .whereEqualTo("usuarioId", user.getUid())
                .get()
                .addOnSuccessListener(q -> {
                    String encontrado = null;
                    for (DocumentSnapshot doc : q.getDocuments()) {
                        if ("freelancer".equals(doc.getString("formato"))
                                && freelancerId.equals(doc.getString("freelancerId"))
                                && "concluida".equals(doc.getString("status"))) {
                            encontrado = doc.getId();
                            break;
                        }
                    }
                    callback.onSucesso(encontrado);
                })
                .addOnFailureListener(callback::onErro);
    }

    public static void getMinhaNota(String freelancerId, Callback<Integer> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { callback.onSucesso(0); return; }
        String id = user.getUid() + "_" + freelancerId;
        FirebaseFirestore.getInstance().collection(AVALIACOES).document(id).get()
                .addOnSuccessListener(doc -> {
                    Object valorNota = doc.get("nota");
                    Number nota = valorNota instanceof Number ? (Number) valorNota : null;
                    callback.onSucesso(nota == null ? 0 : nota.intValue());
                })
                .addOnFailureListener(callback::onErro);
    }

    public static void salvar(String freelancerId, String freelancerNome, String equipeId,
                              int nota, Callback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Entre na sua conta para avaliar."));
            return;
        }
        if (nota < 1 || nota > 5) {
            callback.onErro(new IllegalArgumentException("Escolha de 1 a 5 estrelas."));
            return;
        }
        Map<String, Object> dados = new HashMap<>();
        dados.put("clienteId", user.getUid());
        dados.put("freelancerId", freelancerId);
        dados.put("freelancerNome", freelancerNome);
        dados.put("equipeId", equipeId);
        dados.put("nota", nota);
        dados.put("criadoEm", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection(AVALIACOES)
                .document(user.getUid() + "_" + freelancerId)
                .set(dados)
                .addOnSuccessListener(unused -> callback.onSucesso(null))
                .addOnFailureListener(callback::onErro);
    }

    public static void concluirPedido(String equipeId, Callback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado."));
            return;
        }
        FirebaseFirestore.getInstance().collection(EQUIPES).document(equipeId)
                .update("status", "concluida")
                .addOnSuccessListener(unused -> callback.onSucesso(null))
                .addOnFailureListener(callback::onErro);
    }
}
