package com.example.pimobile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilRepository {

    public static void getPerfilAtual(Callback<Perfil> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onErro(new IllegalStateException("Perfil não encontrado"));
                        return;
                    }

                    String nome = doc.getString("nome");
                    if (nome == null || nome.trim().isEmpty()) nome = "Sem nome";

                    String email = doc.getString("email");
                    if (email == null) email = user.getEmail();

                    String tipoConta = doc.getString("tipoConta");
                    if (tipoConta == null) tipoConta = "Usuário";

                    boolean verificado = Boolean.TRUE.equals(doc.getBoolean("verificado"));

                    Long naoLidas = doc.getLong("mensagensNaoLidas");
                    int mensagens = naoLidas == null ? 0 : naoLidas.intValue();

                    callback.onSucesso(new Perfil(nome, email, tipoConta, verificado, mensagens));
                })
                .addOnFailureListener(callback::onErro);
    }

    public static void atualizarNome(String nome, Callback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .update("nome", nome)
                .addOnSuccessListener(x -> callback.onSucesso(null))
                .addOnFailureListener(callback::onErro);
    }
}