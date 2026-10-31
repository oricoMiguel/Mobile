package com.example.pimobile;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    // Pedido para contratar um freelancer individual. Quem decide é o próprio freelancer.
    // Devolve o ID da equipe criada
    public static void salvarEquipeFreelancer(String freelancerId, String freelancerNome,
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

        // O nome de quem pede vai junto, pois o freelancer não pode ler a coleção "usuarios"
        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(usuario -> {
                    String nome = usuario.getString("nome");
                    if (nome == null) {
                        callback.onErro(new IllegalStateException("Perfil sem nome"));
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

                    // Salva o pedido e cria a notificação do freelancer na mesma operação.
                    // Assim, não fica pedido sem aviso se uma das gravações falhar.
                    DocumentReference equipeRef = db.collection(COLECAO).document();
                    DocumentReference notificacaoRef = db.collection("notificacoes").document();

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
                            .addOnSuccessListener(resultado -> callback.onSucesso(equipeRef.getId()))
                            .addOnFailureListener(callback::onErro);
                })
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
                        String formato = doc.getString("formato");
                        // Pedido de freelancer guarda o nome em freelancerNome; o de pod, em podNome
                        String podNome = "freelancer".equals(formato)
                                ? doc.getString("freelancerNome")
                                : doc.getString("podNome");
                        String tipo = doc.getString("tipo");
                        String status = doc.getString("status");
                        Long pessoas = doc.getLong("pessoas");
                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

                        lista.add(new Equipe(
                                doc.getId(),
                                formato == null ? "skillPod" : formato,
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

    // ---------- Lado do freelancer ----------

    // Pedidos de contratação recebidos pelo freelancer logado (pendentes primeiro).
    // Reaproveita o RegistroAdm: nome = cliente | subtitulo = texto fixo | detalhe = data
    public static void getPedidosRecebidos(Callback<List<RegistroAdm>> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onErro(new IllegalStateException("Nenhum usuário logado"));
            return;
        }

        FirebaseFirestore.getInstance().collection(COLECAO)
                .whereEqualTo("freelancerId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentSnapshot> docs = new ArrayList<>(query.getDocuments());

                    Collections.sort(docs, (a, b) -> {
                        boolean pa = "pendente".equals(a.getString("status"));
                        boolean pb = "pendente".equals(b.getString("status"));
                        if (pa != pb) return pa ? -1 : 1;
                        return Long.compare(millis(b.getTimestamp("criadoEm")),
                                millis(a.getTimestamp("criadoEm")));
                    });

                    SimpleDateFormat formato =
                            new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
                    List<RegistroAdm> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : docs) {
                        String cliente = doc.getString("usuarioNome");
                        String status = doc.getString("status");
                        Timestamp criadoEm = doc.getTimestamp("criadoEm");

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
                                criadoEm == null ? "" : "Pedido em " + formato.format(criadoEm.toDate()),
                                badge,
                                badgeTipo,
                                0,
                                0));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public static void contarPedidosPendentes(Callback<Integer> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onSucesso(0);
            return;
        }

        FirebaseFirestore.getInstance().collection(COLECAO)
                .whereEqualTo("freelancerId", user.getUid())
                .whereEqualTo("status", "pendente")
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(r -> callback.onSucesso((int) r.getCount()))
                .addOnFailureListener(callback::onErro);
    }

    // O freelancer aceita ou recusa. Muda o status e avisa quem pediu, na mesma operação.
    public static void responderPedido(String equipeId, boolean aceitar, Callback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference equipeRef = db.collection(COLECAO).document(equipeId);

        equipeRef.get()
                .addOnSuccessListener(doc -> {
                    String usuarioId = doc.getString("usuarioId");
                    String freelancerNome = doc.getString("freelancerNome");

                    WriteBatch batch = db.batch();
                    batch.update(equipeRef, "status", aceitar ? "aprovada" : "recusada");

                    if (usuarioId != null) {
                        String quem = freelancerNome == null ? "O freelancer" : freelancerNome;
                        Map<String, Object> aviso = new HashMap<>();
                        aviso.put("usuarioId", usuarioId);
                        aviso.put("equipeId", equipeId);
                        aviso.put("titulo", aceitar ? "Pedido aceito" : "Pedido recusado");
                        aviso.put("mensagem", aceitar
                                ? quem + " aceitou seu pedido de contratação."
                                : quem + " não pôde aceitar seu pedido desta vez.");
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

    private static long millis(Timestamp t) {
        return t == null ? 0 : t.toDate().getTime();
    }
}