package com.example.pimobile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropostaPodRepository {

    private static final String COLECAO = "propostasPod";

    // Cria uma proposta pendente. Quem decide é o admin (AreaAdmRepository).
    public static void enviar(String nome, String descricao, boolean digital,
                              List<String> tags, String icone, Callback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        PerfilRepository.getPerfilAtual(new Callback<Perfil>() {
            @Override
            public void onSucesso(Perfil perfil) {
                Map<String, Object> proposta = new HashMap<>();
                proposta.put("usuarioId", user.getUid());
                proposta.put("usuarioNome", perfil.getNome());
                proposta.put("nome", nome);
                proposta.put("descricao", descricao);
                proposta.put("digital", digital);
                proposta.put("tags", tags);
                proposta.put("icone", icone);
                proposta.put("status", "pendente");
                proposta.put("criadoEm", FieldValue.serverTimestamp());

                FirebaseFirestore.getInstance().collection(COLECAO)
                        .add(proposta)
                        .addOnSuccessListener(ref -> callback.onSucesso(null))
                        .addOnFailureListener(callback::onErro);
            }

            @Override
            public void onErro(Exception e) {
                callback.onErro(e);
            }
        });
    }
}
