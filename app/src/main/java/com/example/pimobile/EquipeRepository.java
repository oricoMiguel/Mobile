package com.example.pimobile;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipeRepository {

    private static final String COLECAO = "equipes";

    // Devolve o ID da equipe criada
    public static void salvarEquipePod(String podId, String podNome, int pessoas,
                                       String tipo, Callback<String> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }
        if (podId == null) {
            callback.onErro(new IllegalArgumentException("Pod inválido"));
            return;
        }

        Map<String, Object> equipe = new HashMap<>();
        equipe.put("usuarioId", user.getUid());
        equipe.put("formato", "skillPod");
        equipe.put("podId", podId);
        equipe.put("podNome", podNome);
        equipe.put("pessoas", pessoas);
        equipe.put("tipo", tipo);
        equipe.put("status", "pendente");
        equipe.put("criadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection(COLECAO)
                .add(equipe)
                .addOnSuccessListener(ref -> callback.onSucesso(ref.getId()))
                .addOnFailureListener(callback::onErro);
    }

    // Equipes do usuário logado, da mais recente para a mais antiga
    public static void getMinhasEquipes(Callback<List<Equipe>> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance().collection(COLECAO)
                .whereEqualTo("usuarioId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    List<Equipe> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String podNome = doc.getString("podNome");
                        String tipo = doc.getString("tipo");
                        String status = doc.getString("status");
                        Long pessoas = doc.getLong("pessoas");
                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

                        lista.add(new Equipe(
                                doc.getId(),
                                podNome == null ? "Equipe" : podNome,
                                pessoas == null ? 0 : pessoas.intValue(),
                                tipo == null ? "" : tipo,
                                status == null ? "pendente" : status,
                                criadoEm == null ? 0 : criadoEm.toDate().getTime()));
                    }
                    Collections.sort(lista, (a, b) -> Long.compare(b.getCriadoEm(), a.getCriadoEm()));
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }
}