
package com.example.pimobile;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EquipeRepository {

    private static final String COLECAO = "equipes";

    public static final int MAX_FREELANCERS_EQUIPE = 5;


    // ==========================================
    // SALVAR EQUIPE DE SKILL POD
    // ==========================================

    public static void salvarEquipePod(
            String podId,
            String podNome,
            int pessoas,
            String tipo,
            Callback<String> callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        if (podId == null || podId.trim().isEmpty()) {
            callback.onErro(new IllegalArgumentException("Pod inválido"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SkillPodRepository.getPorId(podId, new Callback<SkillPod>() {

            @Override
            public void onSucesso(SkillPod pod) {

                if (pod == null) {
                    callback.onErro(
                            new IllegalStateException("Skill Pod não encontrado."));
                    return;
                }

                List<Membro> integrantes = pod.getIntegrantes();

                if (integrantes == null || integrantes.isEmpty()) {
                    callback.onErro(
                            new IllegalStateException(
                                    "Este Skill Pod ainda não possui integrantes."));
                    return;
                }

                if (integrantes.size() > MAX_FREELANCERS_EQUIPE) {
                    callback.onErro(
                            new IllegalArgumentException(
                                    "Este Skill Pod possui mais de "
                                            + MAX_FREELANCERS_EQUIPE
                                            + " integrantes. Não é possível enviar o pedido."
                            ));
                    return;
                }

                List<Membro> membrosValidos = new ArrayList<>();
                Set<String> idsVerificados = new HashSet<>();

                for (Membro membro : integrantes) {

                    if (membro == null
                            || membro.getUid() == null
                            || membro.getUid().trim().isEmpty()) {

                        callback.onErro(
                                new IllegalStateException(
                                        "Existe um integrante sem UID cadastrado neste Skill Pod."));
                        return;
                    }

                    if (membro.getUid().equals(user.getUid())) {
                        callback.onErro(
                                new IllegalStateException(
                                        "O contratante não pode receber um pedido do próprio pod."));
                        return;
                    }

                    if (!idsVerificados.add(membro.getUid())) {
                        callback.onErro(
                                new IllegalStateException(
                                        "Existem integrantes duplicados neste Skill Pod."));
                        return;
                    }

                    if (membro.getNome() == null
                            || membro.getNome().trim().isEmpty()) {

                        callback.onErro(
                                new IllegalStateException(
                                        "Existe um integrante sem nome cadastrado."));
                        return;
                    }

                    membrosValidos.add(membro);
                }

                if (membrosValidos.isEmpty()) {
                    callback.onErro(
                            new IllegalStateException(
                                    "Nenhum integrante válido foi encontrado."));
                    return;
                }

                db.collection("usuarios")
                        .document(user.getUid())
                        .get()
                        .addOnSuccessListener(usuario -> {

                            String nomeUsuario = usuario.getString("nome");

                            if (nomeUsuario == null
                                    || nomeUsuario.trim().isEmpty()) {

                                callback.onErro(
                                        new IllegalStateException(
                                                "Perfil do contratante sem nome."));
                                return;
                            }

                            String grupoEquipeId =
                                    db.collection(COLECAO).document().getId();

                            WriteBatch batch = db.batch();

                            for (Membro membro : membrosValidos) {

                                String freelancerId = membro.getUid();
                                String freelancerNome = membro.getNome();

                                DocumentReference equipeRef =
                                        db.collection(COLECAO).document();

                                DocumentReference notificacaoRef =
                                        db.collection("notificacoes").document();

                                Map<String, Object> equipe = new HashMap<>();

                                equipe.put("usuarioId", user.getUid());
                                equipe.put("usuarioNome", nomeUsuario);
                                equipe.put("formato", "skillPod");
                                equipe.put("podId", podId);
                                equipe.put("podNome", podNome);
                                equipe.put("freelancerId", freelancerId);
                                equipe.put("freelancerNome", freelancerNome);
                                equipe.put("pessoas", pessoas);
                                equipe.put("tipo", tipo);
                                equipe.put("status", "pendente");
                                equipe.put("grupoEquipeId", grupoEquipeId);
                                equipe.put("criadoEm", FieldValue.serverTimestamp());

                                Map<String, Object> aviso = new HashMap<>();

                                aviso.put("usuarioId", freelancerId);
                                aviso.put("equipeId", equipeRef.getId());
                                aviso.put("titulo", "Novo pedido de Skill Pod");
                                aviso.put(
                                        "mensagem",
                                        nomeUsuario
                                                + " solicitou sua participação no Skill Pod "
                                                + podNome + "."
                                );
                                aviso.put("lida", false);
                                aviso.put("tipo", "pedido_contratacao");
                                aviso.put("criadoEm", FieldValue.serverTimestamp());

                                batch.set(equipeRef, equipe);
                                batch.set(notificacaoRef, aviso);
                            }

                            batch.commit()
                                    .addOnSuccessListener(resultado ->
                                            callback.onSucesso(grupoEquipeId))
                                    .addOnFailureListener(callback::onErro);

                        })
                        .addOnFailureListener(callback::onErro);
            }

            @Override
            public void onErro(Exception e) {
                callback.onErro(e);
            }
        });
    }


    // ==========================================
    // CONTRATAÇÃO INDIVIDUAL
    // ==========================================

    public static void salvarEquipeFreelancer(
            String freelancerId,
            String freelancerNome,
            Callback<String> callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        if (freelancerId == null || freelancerNome == null) {
            callback.onErro(new IllegalArgumentException("Freelancer inválido"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(usuario -> {

                    String nome = usuario.getString("nome");

                    if (nome == null || nome.trim().isEmpty()) {
                        callback.onErro(
                                new IllegalStateException("Perfil sem nome"));
                        return;
                    }

                    Map<String, Object> equipe = new HashMap<>();

                    equipe.put("usuarioId", user.getUid());
                    equipe.put("usuarioNome", nome);
                    equipe.put("formato", "freelancer");
                    equipe.put("freelancerId", freelancerId);
                    equipe.put("freelancerNome", freelancerNome);
                    equipe.put("pessoas", 1);
                    equipe.put("tipo", "Individual");
                    equipe.put("status", "pendente");
                    equipe.put("criadoEm", FieldValue.serverTimestamp());

                    DocumentReference equipeRef =
                            db.collection(COLECAO).document();

                    DocumentReference notificacaoRef =
                            db.collection("notificacoes").document();

                    Map<String, Object> aviso = new HashMap<>();

                    aviso.put("usuarioId", freelancerId);
                    aviso.put("equipeId", equipeRef.getId());
                    aviso.put("titulo", "Novo pedido de contratação");
                    aviso.put("mensagem", nome + " quer contratar você.");
                    aviso.put("lida", false);
                    aviso.put("tipo", "pedido_contratacao");
                    aviso.put("criadoEm", FieldValue.serverTimestamp());

                    WriteBatch batch = db.batch();

                    batch.set(equipeRef, equipe);
                    batch.set(notificacaoRef, aviso);

                    batch.commit()
                            .addOnSuccessListener(resultado ->
                                    callback.onSucesso(equipeRef.getId()))
                            .addOnFailureListener(callback::onErro);

                })
                .addOnFailureListener(callback::onErro);
    }


    // ==========================================
    // CONTRATAÇÃO DE VÁRIOS FREELANCERS
    // ==========================================

    public static void salvarEquipeFreelancers(
            List<String> freelancerIds,
            List<String> freelancerNomes,
            Callback<String> callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onErro(
                    new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        if (freelancerIds == null
                || freelancerNomes == null
                || freelancerIds.isEmpty()
                || freelancerIds.size() != freelancerNomes.size()) {

            callback.onErro(
                    new IllegalArgumentException("Lista de freelancers inválida"));
            return;
        }

        if (freelancerIds.size() > MAX_FREELANCERS_EQUIPE) {
            callback.onErro(
                    new IllegalArgumentException(
                            "Selecione no máximo "
                                    + MAX_FREELANCERS_EQUIPE
                                    + " freelancers por equipe."
                    )
            );
            return;
        }

        Set<String> idsVerificados = new HashSet<>();

        for (int i = 0; i < freelancerIds.size(); i++) {

            String id = freelancerIds.get(i);
            String nome = freelancerNomes.get(i);

            if (id == null || id.trim().isEmpty()
                    || nome == null || nome.trim().isEmpty()) {

                callback.onErro(
                        new IllegalArgumentException(
                                "Existe um freelancer com dados inválidos."));
                return;
            }

            if (id.equals(user.getUid())) {
                callback.onErro(
                        new IllegalArgumentException(
                                "Você não pode contratar seu próprio perfil."));
                return;
            }

            if (!idsVerificados.add(id)) {
                callback.onErro(
                        new IllegalArgumentException(
                                "A lista contém freelancers repetidos."));
                return;
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(usuario -> {

                    String nomeUsuario = usuario.getString("nome");

                    if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
                        callback.onErro(
                                new IllegalStateException("Perfil sem nome"));
                        return;
                    }

                    String grupoEquipeId =
                            db.collection(COLECAO).document().getId();

                    WriteBatch batch = db.batch();

                    for (int i = 0; i < freelancerIds.size(); i++) {

                        String freelancerId = freelancerIds.get(i);
                        String freelancerNome = freelancerNomes.get(i);

                        DocumentReference equipeRef =
                                db.collection(COLECAO).document();

                        DocumentReference notificacaoRef =
                                db.collection("notificacoes").document();

                        Map<String, Object> equipe = new HashMap<>();

                        equipe.put("usuarioId", user.getUid());
                        equipe.put("usuarioNome", nomeUsuario);
                        equipe.put("formato", "freelancer");
                        equipe.put("freelancerId", freelancerId);
                        equipe.put("freelancerNome", freelancerNome);
                        equipe.put("pessoas", 1);
                        equipe.put("tipo", "Individual");
                        equipe.put("status", "pendente");
                        equipe.put("criadoEm", FieldValue.serverTimestamp());
                        equipe.put("grupoEquipeId", grupoEquipeId);

                        Map<String, Object> aviso = new HashMap<>();

                        aviso.put("usuarioId", freelancerId);
                        aviso.put("equipeId", equipeRef.getId());
                        aviso.put("titulo", "Novo pedido de contratação");
                        aviso.put(
                                "mensagem",
                                nomeUsuario + " quer contratar você para uma equipe."
                        );
                        aviso.put("lida", false);
                        aviso.put("tipo", "pedido_contratacao");
                        aviso.put("criadoEm", FieldValue.serverTimestamp());

                        batch.set(equipeRef, equipe);
                        batch.set(notificacaoRef, aviso);
                    }

                    batch.commit()
                            .addOnSuccessListener(resultado ->
                                    callback.onSucesso(grupoEquipeId))
                            .addOnFailureListener(callback::onErro);

                })
                .addOnFailureListener(callback::onErro);
    }


    // ==========================================
    // EQUIPES DO USUÁRIO LOGADO
    // ==========================================

    public static void getMinhasEquipes(
            Callback<List<Equipe>> callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onErro(
                    new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(COLECAO)
                .whereEqualTo("usuarioId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {

                    List<Equipe> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        String formato = doc.getString("formato");

                        String podNome = "freelancer".equals(formato)
                                ? doc.getString("freelancerNome")
                                : doc.getString("podNome");

                        String tipo = doc.getString("tipo");
                        String status = doc.getString("status");

                        Long pessoas = doc.getLong("pessoas");

                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

                        String grupoEquipeId = doc.getString("grupoEquipeId");

                        lista.add(new Equipe(
                                doc.getId(),
                                formato == null ? "skillPod" : formato,
                                podNome == null ? "Equipe" : podNome,
                                pessoas == null ? 0 : pessoas.intValue(),
                                tipo == null ? "" : tipo,
                                status == null ? "pendente" : status,
                                criadoEm == null
                                        ? 0
                                        : criadoEm.toDate().getTime(),
                                grupoEquipeId
                        ));
                    }

                    Collections.sort(
                            lista,
                            (a, b) -> Long.compare(
                                    b.getCriadoEm(),
                                    a.getCriadoEm()
                            )
                    );

                    callback.onSucesso(lista);

                })
                .addOnFailureListener(callback::onErro);
    }


    // ==========================================
    // LADO DO FREELANCER
    // ==========================================

    public static void getPedidosRecebidos(
            Callback<List<RegistroAdm>> callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onErro(
                    new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLECAO)
                .whereEqualTo("freelancerId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {

                    List<DocumentSnapshot> docs =
                            new ArrayList<>(query.getDocuments());

                    Collections.sort(docs, (a, b) -> {

                        boolean pa = "pendente".equals(a.getString("status"));
                        boolean pb = "pendente".equals(b.getString("status"));

                        if (pa != pb) {
                            return pa ? -1 : 1;
                        }

                        return Long.compare(
                                millis(b.getTimestamp("criadoEm")),
                                millis(a.getTimestamp("criadoEm"))
                        );
                    });

                    Set<String> idsContratantes = new HashSet<>();

                    for (DocumentSnapshot doc : docs) {

                        String usuarioId = doc.getString("usuarioId");

                        Log.d("PEDIDOS_DEBUG",
                                "Pedido: " + doc.getId()
                                        + " | usuarioId: " + usuarioId
                                        + " | usuarioNome: " + doc.getString("usuarioNome")
                                        + " | freelancerId: " + doc.getString("freelancerId")
                                        + " | status: " + doc.getString("status"));

                        if (usuarioId != null && !usuarioId.trim().isEmpty()) {
                            idsContratantes.add(usuarioId);
                        }
                    }

                    List<String> ids = new ArrayList<>(idsContratantes);

                    List<Task<DocumentSnapshot>> tarefas = new ArrayList<>();

                    for (String id : ids) {
                        tarefas.add(
                                db.collection("usuarios")
                                        .document(id)
                                        .get()
                        );
                    }

                    /*
                     * IMPORTANTE:
                     * whenAllComplete permite continuar mesmo que
                     * alguma leitura de perfil seja negada.
                     */
                    Tasks.whenAllComplete(tarefas)
                            .addOnSuccessListener(resultados -> {

                                Map<String, DocumentSnapshot> perfis =
                                        new HashMap<>();

                                for (int i = 0; i < ids.size(); i++) {

                                    Task<DocumentSnapshot> tarefa =
                                            tarefas.get(i);

                                    if (tarefa.isSuccessful()
                                            && tarefa.getResult() != null) {

                                        perfis.put(
                                                ids.get(i),
                                                tarefa.getResult()
                                        );

                                    } else {

                                        Log.w(
                                                "EquipeRepository",
                                                "Não foi possível carregar o perfil: "
                                                        + ids.get(i),
                                                tarefa.getException()
                                        );
                                    }
                                }

                                SimpleDateFormat formato =
                                        new SimpleDateFormat(
                                                "dd/MM/yyyy",
                                                new Locale("pt", "BR")
                                        );

                                List<RegistroAdm> lista = new ArrayList<>();

                                for (DocumentSnapshot doc : docs) {

                                    String usuarioId = doc.getString("usuarioId");

                                    // Nome salvo no próprio pedido.
                                    String cliente = doc.getString("usuarioNome");

                                    String status = doc.getString("status");

                                    DocumentSnapshot perfil =
                                            perfis.get(usuarioId);

                                    String fotoUrl = null;
                                    String email = null;
                                    String telefone = null;
                                    String empresa = null;
                                    String cidade = null;
                                    String estado = null;

                                    // Dados complementares, se houver permissão.
                                    if (perfil != null && perfil.exists()) {

                                        String nomePerfil = perfil.getString("nome");

                                        if (nomePerfil != null
                                                && !nomePerfil.trim().isEmpty()) {
                                            cliente = nomePerfil;
                                        }

                                        fotoUrl = perfil.getString("fotoUrl");
                                        email = perfil.getString("email");
                                        telefone = perfil.getString("telefone");
                                        empresa = perfil.getString("empresa");
                                        cidade = perfil.getString("cidade");
                                        estado = perfil.getString("estado");
                                    }

                                    Timestamp criadoEm =
                                            doc.getTimestamp("criadoEm");

                                    String badge;
                                    int badgeTipo;

                                    if ("concluida".equals(status)) {
                                        badge = "Concluído";
                                        badgeTipo = 0;

                                    } else if ("aprovada".equals(status)) {
                                        badge = "Aceito";
                                        badgeTipo = 0;

                                    } else if ("recusada".equals(status)) {
                                        badge = "Recusado";
                                        badgeTipo = 2;

                                    } else {
                                        badge = "Pendente";
                                        badgeTipo = 1;
                                    }

                                    lista.add(new RegistroAdm(
                                            doc.getId(),
                                            cliente == null ? "Cliente" : cliente,
                                            "Quer contratar você",
                                            criadoEm == null
                                                    ? ""
                                                    : "Pedido em " + formato.format(
                                                    criadoEm.toDate()
                                            ),
                                            badge,
                                            badgeTipo,
                                            0,
                                            0,
                                            usuarioId,
                                            fotoUrl,
                                            email,
                                            telefone,
                                            empresa,
                                            cidade,
                                            estado
                                    ));
                                }

                                callback.onSucesso(lista);
                            });
                })
                .addOnFailureListener(callback::onErro);
    }


    // ==========================================
    // CONTAR PEDIDOS PENDENTES
    // ==========================================

    public static void contarPedidosPendentes(
            Callback<Integer> callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onSucesso(0);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(COLECAO)
                .whereEqualTo("freelancerId", user.getUid())
                .whereEqualTo("status", "pendente")
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(r ->
                        callback.onSucesso((int) r.getCount()))
                .addOnFailureListener(callback::onErro);
    }


    // ==========================================
    // RESPONDER PEDIDO
    // ==========================================

    public static void responderPedido(
            String equipeId,
            boolean aceitar,
            Callback<Void> callback) {

        if (equipeId == null || equipeId.trim().isEmpty()) {
            callback.onErro(
                    new IllegalArgumentException("Pedido inválido"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference equipeRef =
                db.collection(COLECAO).document(equipeId);

        equipeRef.get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        callback.onErro(
                                new IllegalStateException("Pedido não encontrado"));
                        return;
                    }

                    String usuarioId = doc.getString("usuarioId");
                    String freelancerNome = doc.getString("freelancerNome");

                    WriteBatch batch = db.batch();

                    batch.update(
                            equipeRef,
                            "status",
                            aceitar ? "aprovada" : "recusada"
                    );

                    if (usuarioId != null) {

                        String quem = freelancerNome == null
                                ? "O freelancer"
                                : freelancerNome;

                        Map<String, Object> aviso = new HashMap<>();

                        aviso.put("usuarioId", usuarioId);
                        aviso.put("equipeId", equipeId);
                        aviso.put(
                                "titulo",
                                aceitar ? "Pedido aceito" : "Pedido recusado"
                        );

                        aviso.put(
                                "mensagem",
                                aceitar
                                        ? quem + " aceitou seu pedido de contratação."
                                        : quem + " não pôde aceitar seu pedido desta vez."
                        );

                        aviso.put("lida", false);
                        aviso.put("criadoEm", FieldValue.serverTimestamp());

                        batch.set(
                                db.collection("notificacoes").document(),
                                aviso
                        );
                    }

                    batch.commit()
                            .addOnSuccessListener(x -> callback.onSucesso(null))
                            .addOnFailureListener(callback::onErro);

                })
                .addOnFailureListener(callback::onErro);
    }


    // ==========================================
    // AUXILIAR
    // ==========================================

    private static long millis(Timestamp t) {
        return t == null ? 0 : t.toDate().getTime();
    }
}
