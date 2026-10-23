package com.example.pimobile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ParticipacaoRepository {

    private static final String COLECAO = "participacoes";

    // Status do pedido do usuário logado neste pod (null = ainda não pediu)
    public static void getStatus(String podId, Callback<String> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance().collection(COLECAO)
                .document(podId + "_" + user.getUid())
                .get()
                .addOnSuccessListener(doc ->
                        callback.onSucesso(doc.exists() ? doc.getString("status") : null))
                .addOnFailureListener(callback::onErro);
    }

    // statusAtual: null se nunca pediu, "recusada" se está pedindo de novo
    public static void pedirEntrada(String podId, String podNome, String nome,
                                    String statusAtual, Callback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        DocumentReference ref = FirebaseFirestore.getInstance().collection(COLECAO)
                .document(podId + "_" + user.getUid());

        if ("recusada".equals(statusAtual)) {
            ref.update("status", "pendente", "criadoEm", FieldValue.serverTimestamp())
                    .addOnSuccessListener(x -> callback.onSucesso(null))
                    .addOnFailureListener(callback::onErro);
            return;
        }

        Map<String, Object> pedido = new HashMap<>();
        pedido.put("podId", podId);
        pedido.put("podNome", podNome);
        pedido.put("usuarioId", user.getUid());
        pedido.put("nome", nome);
        pedido.put("status", "pendente");
        pedido.put("criadoEm", FieldValue.serverTimestamp());

        ref.set(pedido)
                .addOnSuccessListener(x -> callback.onSucesso(null))
                .addOnFailureListener(callback::onErro);
    }
}