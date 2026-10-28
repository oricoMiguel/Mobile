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

import com.google.firebase.firestore.FirebaseFirestoreException;


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
    // ---------- Freelancers ----------
    // subtitulo = área | detalhe = nota, categoria e disponibilidade | badge = status do perfil
    public static void getFreelancers(Callback<List<RegistroAdm>> callback) {
        FirebaseFirestore.getInstance().collection("freelancers")
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentSnapshot> docs = new ArrayList<>(query.getDocuments());

                    // Pendentes primeiro; dentro de cada grupo, ordem alfabética
                    Collections.sort(docs, (a, b) -> {
                        boolean pa = "pendente".equals(a.getString("status"));
                        boolean pb = "pendente".equals(b.getString("status"));
                        if (pa != pb) return pa ? -1 : 1;
                        String na = a.getString("nome") == null ? "" : a.getString("nome");
                        String nb = b.getString("nome") == null ? "" : b.getString("nome");
                        return na.compareToIgnoreCase(nb);
                    });

                    List<RegistroAdm> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : docs) {
                        String nome = doc.getString("nome");
                        String area = doc.getString("area");
                        String categoria = doc.getString("categoria");
                        String status = doc.getString("status");
                        Double nota = doc.getDouble("nota");
                        boolean disponivel = Boolean.TRUE.equals(doc.getBoolean("disponivel"));

                        double notaValor = nota == null ? 0 : nota;
                        String detalhe = (notaValor > 0
                                ? "★ " + String.format(BR, "%.1f", notaValor) : "Novo")
                                + " · " + (categoria == null ? "Outros" : categoria)
                                + " · " + (disponivel ? "Disponível" : "Indisponível");

                        String badge;
                        int badgeTipo;
                        if ("aprovado".equals(status)) {
                            badge = "Aprovado";
                            badgeTipo = 0;
                        } else if ("recusado".equals(status)) {
                            badge = "Recusado";
                            badgeTipo = 2;
                        } else {
                            badge = "Pendente";
                            badgeTipo = 1;
                        }

                        lista.add(new RegistroAdm(
                                doc.getId(),
                                nome == null ? "Sem nome" : nome,
                                area == null ? "" : area,
                                detalhe,
                                badge,
                                badgeTipo,
                                corPorCategoria(categoria == null ? "Outros" : categoria),
                                notaValor));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // ---------- Pedidos para entrar em skill pods ----------
    // nome = freelancer | subtitulo = pod pedido | detalhe = data
    public static void getParticipacoes(Callback<List<RegistroAdm>> callback) {
        FirebaseFirestore.getInstance().collection("participacoes")
                .get()
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
                        String nome = doc.getString("nome");
                        String podNome = doc.getString("podNome");
                        String status = doc.getString("status");
                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

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

                        lista.add(new RegistroAdm(
                                doc.getId(),
                                nome == null ? "Sem nome" : nome,
                                "Quer entrar em: " + (podNome == null ? "pod" : podNome),
                                criadoEm == null ? "Sem data"
                                        : "Pedido em " + formato.format(criadoEm.toDate()),
                                badge,
                                badgeTipo,
                                corPorId(doc.getId()),
                                0));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // status: "aprovada" ou "recusada".
    // Ao aprovar, numa única transação: o pedido muda de status, o freelancer entra na
    // lista de membros do pod, o total de pessoas sobe 1 e ele recebe um aviso.
    public static void atualizarStatusParticipacao(String participacaoId, String status,
                                                   Callback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("participacoes").document(participacaoId);
        boolean aprovada = "aprovada".equals(status);

        db.runTransaction(transacao -> {
                    DocumentSnapshot pedido = transacao.get(ref);

                    // Evita contar duas vezes se outro admin já decidiu
                    if (!"pendente".equals(pedido.getString("status"))) {
                        throw new FirebaseFirestoreException("Este pedido já foi decidido",
                                FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                    }

                    String podId = pedido.getString("podId");
                    String podNome = pedido.getString("podNome");
                    String usuarioId = pedido.getString("usuarioId");
                    String nome = pedido.getString("nome");

                    if (aprovada) {
                        Map<String, Object> membro = new HashMap<>();
                        membro.put("uid", usuarioId);
                        membro.put("nome", nome);

                        transacao.update(db.collection("skillPods").document(podId),
                                "membros", FieldValue.arrayUnion(membro),
                                "totalPessoas", FieldValue.increment(1));
                    }

                    transacao.update(ref, "status", status);

                    Map<String, Object> aviso = new HashMap<>();
                    aviso.put("usuarioId", usuarioId);
                    aviso.put("tipo", "pod");
                    aviso.put("titulo", aprovada ? "Entrada no pod aprovada" : "Entrada no pod recusada");
                    aviso.put("mensagem", aprovada
                            ? "Você agora faz parte do " + (podNome == null ? "pod" : podNome) + "."
                            : "Seu pedido para entrar no " + (podNome == null ? "pod" : podNome)
                            + " não foi aprovado desta vez.");
                    aviso.put("lida", false);
                    aviso.put("criadoEm", FieldValue.serverTimestamp());
                    transacao.set(db.collection("notificacoes").document(), aviso);

                    return null;
                })
                .addOnSuccessListener(x -> callback.onSucesso(null))
                .addOnFailureListener(callback::onErro);
    }

    // status: "aprovado" ou "recusado". O ID do perfil é o mesmo UID da conta do freelancer.
    public static void atualizarStatusFreelancer(String freelancerId, String status,
                                                 Callback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        boolean aprovado = "aprovado".equals(status);

        WriteBatch batch = db.batch();
        batch.update(db.collection("freelancers").document(freelancerId), "status", status);

        Map<String, Object> aviso = new HashMap<>();
        aviso.put("usuarioId", freelancerId);
        aviso.put("tipo", "perfil");
        aviso.put("titulo", aprovado ? "Perfil aprovado" : "Perfil recusado");
        aviso.put("mensagem", aprovado
                ? "Seu perfil profissional foi aprovado e já aparece em Serviços."
                : "Seu perfil profissional não foi aprovado. Ajuste as informações e envie de novo.");
        aviso.put("lida", false);
        aviso.put("criadoEm", FieldValue.serverTimestamp());
        batch.set(db.collection("notificacoes").document(), aviso);

        batch.commit()
                .addOnSuccessListener(x -> callback.onSucesso(null))
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

    // ---------- Propostas de novos skill pods ----------
    // nome = pod proposto | subtitulo = autor | detalhe = formato, tags e data
    public static void getPropostasPod(Callback<List<RegistroAdm>> callback) {
        FirebaseFirestore.getInstance().collection("propostasPod")
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentSnapshot> docs = new ArrayList<>(query.getDocuments());

                    // Pendentes primeiro; dentro de cada grupo, as mais recentes
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
                        String nome = doc.getString("nome");
                        String descricao = doc.getString("descricao");
                        String autor = doc.getString("usuarioNome");
                        String status = doc.getString("status");
                        boolean digital = Boolean.TRUE.equals(doc.getBoolean("digital"));
                        String tags = TextUtils.join(", ", listaDeTexto(doc.get("tags")));
                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

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

                        String detalhe = (digital ? "Digital" : "Presencial")
                                + (tags.isEmpty() ? "" : " · " + tags)
                                + (criadoEm == null ? "" : " · " + formato.format(criadoEm.toDate()));

                        lista.add(new PropostaPodRegistro(
                                doc.getId(),
                                nome == null ? "Sem nome" : nome,
                                "Proposto por " + (autor == null ? "usuário" : autor),
                                detalhe,
                                badge,
                                badgeTipo,
                                corPorId(doc.getId()),
                                descricao == null ? "" : descricao));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // status: "aprovada" ou "recusada".
    // Ao aprovar, numa única transação: o pod nasce no catálogo, a proposta guarda o podId,
    // e quem propôs recebe o aviso. Se quem propôs é freelancer com perfil aprovado, ele
    // entra como primeiro membro; se não, o pod nasce vazio ("em formação").
    public static void atualizarStatusPropostaPod(String propostaId, String status,
                                                  Callback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("propostasPod").document(propostaId);
        boolean aprovada = "aprovada".equals(status);

        db.runTransaction(transacao -> {
                    DocumentSnapshot proposta = transacao.get(ref);

                    // Evita criar o pod duas vezes se outro admin já decidiu
                    if (!"pendente".equals(proposta.getString("status"))) {
                        throw new FirebaseFirestoreException("Esta proposta já foi decidida",
                                FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                    }

                    String usuarioId = proposta.getString("usuarioId");
                    String nome = proposta.getString("nome");
                    String nomePod = nome == null ? "pod" : nome;

                    // Todas as leituras antes das escritas
                    String freelancerNome = null;
                    if (aprovada && usuarioId != null) {
                        DocumentSnapshot conta = transacao.get(
                                db.collection("usuarios").document(usuarioId));
                        if ("Freelancer".equals(conta.getString("tipoConta"))) {
                            DocumentSnapshot perfil = transacao.get(
                                    db.collection("freelancers").document(usuarioId));
                            if (perfil.exists() && "aprovado".equals(perfil.getString("status"))) {
                                freelancerNome = perfil.getString("nome");
                            }
                        }
                    }

                    boolean primeiroMembro = freelancerNome != null;

                    if (aprovada) {
                        IconesPod.Item icone = IconesPod.porChave(proposta.getString("icone"));
                        Boolean digital = proposta.getBoolean("digital");
                        DocumentReference podRef = db.collection("skillPods").document();

                        List<Object> membros = new ArrayList<>();
                        if (primeiroMembro) {
                            Map<String, Object> membro = new HashMap<>();
                            membro.put("uid", usuarioId);
                            membro.put("nome", freelancerNome);
                            membros.add(membro);
                        }

                        Map<String, Object> pod = new HashMap<>();
                        pod.put("nome", nomePod);
                        pod.put("descricao", proposta.getString("descricao"));
                        pod.put("digital", Boolean.TRUE.equals(digital));
                        pod.put("tags", listaDeTexto(proposta.get("tags")));
                        pod.put("membros", membros);
                        pod.put("totalPessoas", membros.size());
                        pod.put("icone", icone.chave);
                        pod.put("cor", icone.cor);
                        // Segundos desde 1970: sempre depois dos pods antigos (1 a 4)
                        pod.put("ordem", System.currentTimeMillis() / 1000);
                        transacao.set(podRef, pod);

                        transacao.update(ref, "status", status, "podId", podRef.getId());
                    } else {
                        transacao.update(ref, "status", status);
                    }

                    if (usuarioId != null) {
                        Map<String, Object> aviso = new HashMap<>();
                        aviso.put("usuarioId", usuarioId);
                        aviso.put("tipo", "pod");
                        aviso.put("titulo", aprovada ? "Seu pod foi aprovado" : "Proposta de pod recusada");
                        aviso.put("mensagem", aprovada
                                ? "O " + nomePod + " já está no catálogo."
                                + (primeiroMembro
                                ? " Você é o primeiro membro."
                                : " Ele aparece como \"Em formação\" até um freelancer entrar.")
                                : "Sua proposta do " + nomePod + " não foi aprovada desta vez.");
                        aviso.put("lida", false);
                        aviso.put("criadoEm", FieldValue.serverTimestamp());
                        transacao.set(db.collection("notificacoes").document(), aviso);
                    }

                    return null;
                })
                .addOnSuccessListener(x -> callback.onSucesso(null))
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
                                    boolean freelancer = "freelancer".equals(doc.getString("formato"));
                                    if (freelancer) continue; // o freelancer decide o próprio pedido
                                    String podNome = freelancer
                                            ? doc.getString("freelancerNome")
                                            : doc.getString("podNome");
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

                                    int nPessoas = pessoas == null ? 0 : pessoas.intValue();
                                    String detalhe = (freelancer ? "Freelancer · "
                                            : (tipo == null ? "" : tipo + " · "))
                                            + (nPessoas == 1 ? "1 pessoa" : nPessoas + " pessoas")
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
                    String podNome = "freelancer".equals(doc.getString("formato"))
                            ? doc.getString("freelancerNome")
                            : doc.getString("podNome");
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