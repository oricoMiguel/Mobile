package com.example.pimobile;

import android.text.TextUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.WriteBatch;


public class AreaAdmRepository {

    private static final Locale BR = new Locale("pt", "BR");

    // ---------- Usuários ----------
    // subtitulo = email | detalhe = data de cadastro
    public static void getUsuarios(Callback<List<RegistroAdm>> callback) {
        FirebaseFirestore.getInstance().collection("usuarios")
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentSnapshot> docs = new ArrayList<>(query.getDocuments());
                    Collections.sort(docs, (a, b) -> Long.compare(
                            millis(b.getTimestamp("criadoEm")), millis(a.getTimestamp("criadoEm"))));

                    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", BR);
                    List<RegistroAdm> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : docs) {
                        String nome = doc.getString("nome");
                        String email = doc.getString("email");
                        String tipoConta = doc.getString("tipoConta");
                        boolean admin = "admin".equals(doc.getString("papel"));
                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

                        String badge = admin ? "Admin" : (tipoConta == null ? "Usuário" : tipoConta);
                        int badgeTipo = admin ? 1 : ("Freelancer".equals(tipoConta) ? 0 : 2);
                        String detalhe = criadoEm == null
                                ? "Cadastro sem data"
                                : "Cadastro " + formato.format(criadoEm.toDate());

                        lista.add(new RegistroAdm(
                                doc.getId(),
                                nome == null ? "Sem nome" : nome,
                                email == null ? "" : email,
                                detalhe,
                                badge,
                                badgeTipo,
                                corPorId(doc.getId()),
                                0));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // ---------- Freelancers ----------
    // subtitulo = área | detalhe = nota e disponibilidade | valor = nota
    public static void getFreelancers(Callback<List<RegistroAdm>> callback) {
        FirebaseFirestore.getInstance().collection("freelancers")
                .orderBy("nome")
                .get()
                .addOnSuccessListener(query -> {
                    List<RegistroAdm> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String nome = doc.getString("nome");
                        String area = doc.getString("area");
                        String categoria = doc.getString("categoria");
                        Double nota = doc.getDouble("nota");
                        boolean disponivel = Boolean.TRUE.equals(doc.getBoolean("disponivel"));
                        String disponibilidade = doc.getString("disponibilidade");

                        double notaValor = nota == null ? 0 : nota;
                        String detalhe = "★ " + String.format(BR, "%.1f", notaValor)
                                + (disponibilidade == null ? "" : " · " + disponibilidade);

                        lista.add(new RegistroAdm(
                                doc.getId(),
                                nome == null ? "Sem nome" : nome,
                                area == null ? "" : area,
                                detalhe,
                                disponivel ? "Disponível" : "Indisponível",
                                disponivel ? 0 : 1,
                                corPorCategoria(categoria == null ? "Outros" : categoria),
                                notaValor));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // ---------- Skill pods ----------
    // subtitulo = membros | detalhe = formato e tags | valor = total de pessoas
    public static void getPods(Callback<List<RegistroAdm>> callback) {
        FirebaseFirestore.getInstance().collection("skillPods")
                .orderBy("ordem")
                .get()
                .addOnSuccessListener(query -> {
                    List<RegistroAdm> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String nome = doc.getString("nome");
                        boolean digital = Boolean.TRUE.equals(doc.getBoolean("digital"));
                        Long total = doc.getLong("totalPessoas");
                        Long cor = doc.getLong("cor");
                        int pessoas = total == null ? 0 : total.intValue();
                        String tags = TextUtils.join(", ", listaDeTexto(doc.get("tags")));

                        String detalhe = (digital ? "Digital" : "Presencial")
                                + (tags.isEmpty() ? "" : " · " + tags);

                        lista.add(new RegistroAdm(
                                doc.getId(),
                                nome == null ? "Sem nome" : nome,
                                pessoas + " membros",
                                detalhe,
                                digital ? "Digital" : "Presencial",
                                digital ? 0 : 1,
                                cor == null ? 0 : Math.max(0, Math.min(2, cor.intValue())),
                                pessoas));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // ---------- Pedidos de equipes ----------
    // nome = pod | subtitulo = solicitante | detalhe = tipo, pessoas e data
    public static void getEquipes(Callback<List<RegistroAdm>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Primeiro os nomes dos usuários, para mostrar quem pediu cada equipe
        db.collection("usuarios").get()
                .addOnSuccessListener(usuarios -> {
                    Map<String, String> nomes = new HashMap<>();
                    for (DocumentSnapshot u : usuarios.getDocuments()) {
                        String n = u.getString("nome");
                        nomes.put(u.getId(), n == null ? "Sem nome" : n);
                    }

                    db.collection("equipes").get()
                            .addOnSuccessListener(query -> {
                                List<DocumentSnapshot> docs = new ArrayList<>(query.getDocuments());

                                // Pendentes primeiro; dentro de cada grupo, os mais recentes
                                Collections.sort(docs, (a, b) -> {
                                    boolean pa = "pendente".equals(a.getString("status"));
                                    boolean pb = "pendente".equals(b.getString("status"));
                                    if (pa != pb) return pa ? -1 : 1;
                                    return Long.compare(millis(b.getTimestamp("criadoEm")),
                                            millis(a.getTimestamp("criadoEm")));
                                });

                                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", BR);
                                List<RegistroAdm> lista = new ArrayList<>();

                                for (DocumentSnapshot doc : docs) {
                                    String podNome = doc.getString("podNome");
                                    String tipo = doc.getString("tipo");
                                    String status = doc.getString("status");
                                    Long pessoas = doc.getLong("pessoas");
                                    Timestamp criadoEm = doc.getTimestamp("criadoEm");
                                    String solicitante = nomes.get(doc.getString("usuarioId"));

                                    String badge;
                                    int badgeTipo;
                                    if ("aprovada".equals(status)) {
                                        badge = "Aprovada";
                                        badgeTipo = 0;
                                    } else if ("recusada".equals(status)) {
                                        badge = "Recusada";
                                        badgeTipo = 2;
                                    } else {
                                        badge = "Pendente";
                                        badgeTipo = 1;
                                    }

                                    String detalhe = (tipo == null ? "" : tipo + " · ")
                                            + (pessoas == null ? 0 : pessoas.intValue()) + " pessoas"
                                            + (criadoEm == null ? "" : " · " + formato.format(criadoEm.toDate()));

                                    lista.add(new RegistroAdm(
                                            doc.getId(),
                                            podNome == null ? "Equipe" : podNome,
                                            "Solicitante: " + (solicitante == null ? "Usuário removido" : solicitante),
                                            detalhe,
                                            badge,
                                            badgeTipo,
                                            0,
                                            0));
                                }
                                callback.onSucesso(lista);
                            })
                            .addOnFailureListener(callback::onErro);
                })
                .addOnFailureListener(callback::onErro);
    }

    // status: "aprovada" ou "recusada" (as regras só deixam o admin fazer isso)
    // status: "aprovada" ou "recusada" (as regras só deixam o admin fazer isso).
    // Muda o status e cria o aviso para quem pediu a equipe, tudo na mesma operação.
    public static void atualizarStatusEquipe(String equipeId, String status, Callback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference equipeRef = db.collection("equipes").document(equipeId);

        equipeRef.get()
                .addOnSuccessListener(doc -> {
                    String usuarioId = doc.getString("usuarioId");
                    String podNome = doc.getString("podNome");
                    boolean aprovada = "aprovada".equals(status);

                    WriteBatch batch = db.batch();
                    batch.update(equipeRef, "status", status);

                    if (usuarioId != null) {
                        Map<String, Object> aviso = new HashMap<>();
                        aviso.put("usuarioId", usuarioId);
                        aviso.put("equipeId", equipeId);
                        aviso.put("titulo", aprovada ? "Equipe aprovada" : "Equipe recusada");
                        aviso.put("mensagem", "O pedido da equipe "
                                + (podNome == null ? "" : podNome + " ")
                                + (aprovada ? "foi aprovado." : "não foi aprovado desta vez."));
                        aviso.put("lida", false);
                        aviso.put("criadoEm", FieldValue.serverTimestamp());
                        batch.set(db.collection("notificacoes").document(), aviso);
                    }

                    batch.commit()
                            .addOnSuccessListener(x -> callback.onSucesso(null))
                            .addOnFailureListener(callback::onErro);
                })
                .addOnFailureListener(callback::onErro);
    }

    // ---------- Auxiliares ----------
    private static long millis(Timestamp t) {
        return t == null ? 0 : t.toDate().getTime();
    }

    private static int corPorId(String id) {
        return (id.hashCode() & 0x7fffffff) % 3;
    }

    private static int corPorCategoria(String categoria) {
        switch (categoria) {
            case "Desenvolvimento": return 0;
            case "Design":          return 1;
            case "Marketing":       return 2;
            default:                return (categoria.hashCode() & 0x7fffffff) % 3;
        }
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
}