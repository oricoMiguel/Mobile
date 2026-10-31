package com.example.pimobile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

                    callback.onSucesso(new Perfil(
                            nome, email, tipoConta, verificado, mensagens,
                            doc.getString("telefone"), doc.getString("empresa"),
                            doc.getString("cidade"), doc.getString("estado"),
                            doc.getString("fotoUrl"), doc.getString("fotoPublicId")));
                })
                .addOnFailureListener(callback::onErro);
    }

    public static void atualizarFotoUsuario(String fotoUrl, String fotoPublicId, Callback<Void> callback) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("fotoUrl", fotoUrl == null ? "" : fotoUrl);
        campos.put("fotoPublicId", fotoPublicId == null ? "" : fotoPublicId);
        atualizarCampos(campos, callback);
    }

    public static void atualizarNome(String nome, Callback<Void> callback) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("nome", nome);
        atualizarCampos(campos, callback);
    }

    public static void atualizarPerfilUsuario(String nome, String telefone, String empresa,
                                               String cidade, String estado, Callback<Void> callback) {
        Map<String, Object> campos = new HashMap<>();
        campos.put("nome", nome.trim());
        campos.put("telefone", telefone == null ? "" : telefone.trim());
        campos.put("empresa", empresa == null ? "" : empresa.trim());
        campos.put("cidade", cidade == null ? "" : cidade.trim());
        campos.put("estado", estado == null ? "" : estado.trim());
        atualizarCampos(campos, callback);
    }

    private static void atualizarCampos(Map<String, Object> campos, Callback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }
        FirebaseFirestore.getInstance().collection("usuarios")
                .document(user.getUid())
                .update(campos)
                .addOnSuccessListener(x -> callback.onSucesso(null))
                .addOnFailureListener(callback::onErro);
    }
}
