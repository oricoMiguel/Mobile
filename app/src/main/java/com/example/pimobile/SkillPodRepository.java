package com.example.pimobile;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SkillPodRepository {

    private static final String COLECAO = "skillPods";

    private static final int[] FUNDOS = {
            R.drawable.bg_icone_roxo, R.drawable.bg_icone_verde, R.drawable.bg_icone_laranja};

    public static void getTodos(Callback<List<SkillPod>> callback) {
        FirebaseFirestore.getInstance().collection(COLECAO)
                .orderBy("ordem")
                .get()
                .addOnSuccessListener(query -> {
                    List<SkillPod> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        lista.add(paraSkillPod(doc));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    public static void getPopulares(Callback<List<SkillPodResumo>> callback) {
        FirebaseFirestore.getInstance().collection(COLECAO)
                .orderBy("totalPessoas", Query.Direction.DESCENDING)
                .limit(2)
                .get()
                .addOnSuccessListener(query -> {
                    List<SkillPodResumo> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        SkillPod pod = paraSkillPod(doc);
                        lista.add(new SkillPodResumo(pod.getNome(), pod.getTotalPessoas(),
                                pod.getIconeRes(), FUNDOS[pod.getCor()]));
                    }
                    callback.onSucesso(lista);
                })
                .addOnFailureListener(callback::onErro);
    }

    private static SkillPod paraSkillPod(DocumentSnapshot doc) {
        String nome = doc.getString("nome");
        String descricao = doc.getString("descricao");
        boolean digital = Boolean.TRUE.equals(doc.getBoolean("digital"));

        Long total = doc.getLong("totalPessoas");
        Long cor = doc.getLong("cor");
        int corIndice = cor == null ? 0 : Math.max(0, Math.min(2, cor.intValue()));

        return new SkillPod(
                doc.getId(),
                nome == null ? "Sem nome" : nome,
                descricao == null ? "" : descricao,
                digital,
                listaDeTexto(doc.get("tags")),
                listaDeTexto(doc.get("membros")),
                total == null ? 0 : total.intValue(),
                iconeParaRes(doc.getString("icone")),
                corIndice);
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

    private static int iconeParaRes(String chave) {
        if (chave == null) return R.drawable.ic_code;
        switch (chave) {
            case "palette":   return R.drawable.ic_palette;
            case "megaphone": return R.drawable.ic_megaphone;
            case "code":
            default:          return R.drawable.ic_code;
        }
    }
}