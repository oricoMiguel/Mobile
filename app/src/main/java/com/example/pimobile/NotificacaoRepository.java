package com.example.pimobile;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificacaoRepository {

    private static final String COLECAO = "notificacoes";

    // Avisos do usuário logado, do mais recente para o mais antigo
    public static void getMinhas(Callback<List<Notificacao>> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance().collection(COLECAO)
                .whereEqualTo("usuarioId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    List<Notificacao> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String titulo = doc.getString("titulo");
                        String mensagem = doc.getString("mensagem");
                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

                        String tipo = doc.getString("tipo");

                        lista.add(new Notificacao(
                                doc.getId(),
                                titulo == null ? "Aviso" : titulo,
                                mensagem == null ? "" : mensagem,
                                Boolean.TRUE.equals(doc.getBoolean("lida")),
                                criadoEm == null ? 0 : criadoEm.toDate().getTime(),
                                tipo == null ? "equipe" : tipo));
                    }
                    Collections.sort(lista, (a, b) -> Long.compare(b.getCriadoEm(), a.getCriadoEm()));
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public static void contarNaoLidas(Callback<Integer> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onSucesso(0);
            return;
        }

        FirebaseFirestore.getInstance().collection(COLECAO)
                .whereEqualTo("usuarioId", user.getUid())
                .whereEqualTo("lida", false)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(resultado -> callback.onSucesso((int) resultado.getCount()))
                .addOnFailureListener(callback::onErro);
    }

    public static void marcarComoLida(String notificacaoId) {
        FirebaseFirestore.getInstance().collection(COLECAO)
                .document(notificacaoId)
                .update("lida", true);
    }
}