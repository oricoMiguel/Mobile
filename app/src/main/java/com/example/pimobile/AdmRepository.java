package com.example.pimobile;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class AdmRepository {

    public static void getAreas(Callback<List<AreaAdm>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<AggregateQuerySnapshot> usuarios =
                db.collection("usuarios").count().get(AggregateSource.SERVER);
        Task<AggregateQuerySnapshot> freelancers =
                db.collection("freelancers").count().get(AggregateSource.SERVER);
        Task<AggregateQuerySnapshot> freelancersPendentes =
                db.collection("freelancers").whereEqualTo("status", "pendente")
                        .count().get(AggregateSource.SERVER);
        Task<AggregateQuerySnapshot> pods =
                db.collection("skillPods").count().get(AggregateSource.SERVER);
        Task<AggregateQuerySnapshot> equipesPendentes =
                db.collection("equipes").whereEqualTo("status", "pendente")
                        .count().get(AggregateSource.SERVER);
        Task<AggregateQuerySnapshot> participacoesPendentes =
                db.collection("participacoes").whereEqualTo("status", "pendente")
                        .count().get(AggregateSource.SERVER);

        Tasks.whenAllSuccess(usuarios, freelancers, freelancersPendentes, pods,
                        equipesPendentes, participacoesPendentes)
                .addOnSuccessListener(x -> {
                    long nUsuarios = usuarios.getResult().getCount();
                    long nFreelancers = freelancers.getResult().getCount();
                    long nFreelancersPendentes = freelancersPendentes.getResult().getCount();
                    long nPods = pods.getResult().getCount();
                    long nEquipesPendentes = equipesPendentes.getResult().getCount();
                    long nParticipacoesPendentes = participacoesPendentes.getResult().getCount();

                    List<AreaAdm> lista = new ArrayList<>();
                    lista.add(new AreaAdm("Pedidos de equipes",
                            "Aprove ou recuse equipes montadas",
                            nEquipesPendentes + " pendentes",
                            R.drawable.ic_people, R.drawable.bg_icone_roxo,
                            AreaEquipesActivity.class, (int) nEquipesPendentes));
                    lista.add(new AreaAdm("Pedidos de pods",
                            "Freelancers que querem entrar em um skill pod",
                            nParticipacoesPendentes + " pendentes",
                            R.drawable.ic_pods_laranja, R.drawable.bg_icone_laranja,
                            AreaParticipacoesActivity.class, (int) nParticipacoesPendentes));
                    lista.add(new AreaAdm("Área do usuário", "Gerencie contas de usuários",
                            nUsuarios + " usuários",
                            R.drawable.ic_users_purple, R.drawable.bg_icone_roxo,
                            AreaUsuarioActivity.class, 0));
                    lista.add(new AreaAdm("Área dos freelancers",
                            "Aprove perfis e acompanhe a disponibilidade",
                            nFreelancers + " freelancers · " + nFreelancersPendentes + " pendentes",
                            R.drawable.ic_briefcase, R.drawable.bg_icone_verde,
                            AreaFreelancerActivity.class, (int) nFreelancersPendentes));
                    lista.add(new AreaAdm("Gestão de pods", "Organize e monitore os skill pods",
                            nPods + " pods",
                            R.drawable.ic_pods_laranja, R.drawable.bg_icone_laranja,
                            AreaPodsActivity.class, 0));
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    // true quando as regras do Firestore recusaram a leitura (usuário não é admin)
    public static boolean permissaoNegada(Exception e) {
        return e instanceof FirebaseFirestoreException
                && ((FirebaseFirestoreException) e).getCode()
                == FirebaseFirestoreException.Code.PERMISSION_DENIED;
    }
}