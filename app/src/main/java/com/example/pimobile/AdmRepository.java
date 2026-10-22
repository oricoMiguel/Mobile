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
        Task<AggregateQuerySnapshot> pods =
                db.collection("skillPods").count().get(AggregateSource.SERVER);
        Task<AggregateQuerySnapshot> pendentes =
                db.collection("equipes").whereEqualTo("status", "pendente")
                        .count().get(AggregateSource.SERVER);

        Tasks.whenAllSuccess(usuarios, freelancers, pods, pendentes)
                .addOnSuccessListener(x -> {
                    long nUsuarios = usuarios.getResult().getCount();
                    long nFreelancers = freelancers.getResult().getCount();
                    long nPods = pods.getResult().getCount();
                    long nPendentes = pendentes.getResult().getCount();

                    List<AreaAdm> lista = new ArrayList<>();
                    lista.add(new AreaAdm("Pedidos de equipes",
                            "Aprove ou recuse equipes montadas",
                            nPendentes + " pendentes",
                            R.drawable.ic_people, R.drawable.bg_icone_roxo,
                            AreaEquipesActivity.class, (int) nPendentes));
                    lista.add(new AreaAdm("Área do usuário", "Gerencie contas de usuários",
                            nUsuarios + " usuários",
                            R.drawable.ic_users_purple, R.drawable.bg_icone_roxo,
                            AreaUsuarioActivity.class, 0));
                    lista.add(new AreaAdm("Área dos freelancers",
                            "Freelancers e disponibilidade",
                            nFreelancers + " freelancers",
                            R.drawable.ic_briefcase, R.drawable.bg_icone_verde,
                            AreaFreelancerActivity.class, 0));
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