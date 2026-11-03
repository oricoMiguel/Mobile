
package com.example.pimobile;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MinhasEquipesActivity extends AppCompatActivity {

    private LinearLayout listaEquipes;
    private TextView txtVazio, btnMontarEquipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_equipes);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        listaEquipes = findViewById(R.id.listaEquipes);
        txtVazio = findViewById(R.id.txtVazio);
        btnMontarEquipe = findViewById(R.id.btnMontarEquipe);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        btnMontarEquipe.setOnClickListener(v ->
                startActivity(new Intent(this, MontarEquipeActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEquipes();
    }

    private void carregarEquipes() {

        EquipeRepository.getMinhasEquipes(new Callback<List<Equipe>>() {

            @Override
            public void onSucesso(List<Equipe> equipes) {
                if (isFinishing() || isDestroyed()) return;
                renderizar(equipes);
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;

                txtVazio.setText("Não foi possível carregar suas equipes");
                txtVazio.setVisibility(View.VISIBLE);

                Toast.makeText(
                        MinhasEquipesActivity.this,
                        "Não foi possível carregar suas equipes",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void renderizar(List<Equipe> equipes) {

        listaEquipes.removeAllViews();

        if (equipes.isEmpty()) {

            txtVazio.setText("Você ainda não montou nenhuma equipe");
            txtVazio.setVisibility(View.VISIBLE);
            btnMontarEquipe.setVisibility(View.VISIBLE);

            return;
        }

        txtVazio.setVisibility(View.GONE);
        btnMontarEquipe.setVisibility(View.GONE);

        /*
         * Agrupa somente os pedidos que possuem grupoEquipeId.
         *
         * Pedidos individuais antigos e Skill Pods continuam
         * sendo exibidos separadamente.
         */
        Map<String, List<Equipe>> grupos = new LinkedHashMap<>();

        for (Equipe equipe : equipes) {

            if (equipe.pertenceAGrupo()) {

                String grupoId = equipe.getGrupoEquipeId();

                if (!grupos.containsKey(grupoId)) {
                    grupos.put(grupoId, new ArrayList<>());
                }

                grupos.get(grupoId).add(equipe);

            } else {

                // Pedido individual ou Skill Pod.
                renderizarPedido(equipe, listaEquipes);
            }
        }

        // Exibe os grupos de freelancers.
        for (Map.Entry<String, List<Equipe>> entrada : grupos.entrySet()) {

            List<Equipe> integrantes = entrada.getValue();

            renderizarGrupo(integrantes);
        }
    }


    private void renderizarGrupo(List<Equipe> integrantes) {

        if (integrantes == null || integrantes.isEmpty()) return;

        // Skill Pod: mostrar apenas um card para o grupo inteiro.
        if ("skillPod".equals(integrantes.get(0).getFormato())) {
            renderizarGrupoSkillPod(integrantes);
            return;
        }

        // Equipe de freelancers: mantém os pedidos individuais.
        LinearLayout grupo = new LinearLayout(this);
        grupo.setOrientation(LinearLayout.VERTICAL);
        grupo.setPadding(dp(12), dp(12), dp(12), dp(8));

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.bottomMargin = dp(16);
        grupo.setLayoutParams(parametros);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(android.graphics.Color.WHITE);
        fundo.setCornerRadius(dp(14));
        fundo.setStroke(
                dp(1),
                ContextCompat.getColor(this, R.color.eq_texto_claro)
        );

        grupo.setBackground(fundo);

        TextView titulo = new TextView(this);
        titulo.setText("Equipe com " + integrantes.size() + " freelancers");
        titulo.setTextSize(17);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setTextColor(android.graphics.Color.BLACK);

        LinearLayout.LayoutParams parametrosTitulo =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosTitulo.bottomMargin = dp(12);
        titulo.setLayoutParams(parametrosTitulo);

        grupo.addView(titulo);

        for (Equipe integrante : integrantes) {
            renderizarPedido(integrante, grupo);
        }

        listaEquipes.addView(grupo);
    }


// ==========================================
// EXIBIR SKILL POD AGRUPADO
// ==========================================


    private void renderizarGrupoSkillPod(List<Equipe> integrantes) {

        if (integrantes == null || integrantes.isEmpty()) {
            return;
        }

        Equipe principal = integrantes.get(0);

        int aprovados = 0;
        int pendentes = 0;
        int recusados = 0;
        int concluidos = 0;

        for (Equipe equipe : integrantes) {

            String statusAtual = equipe.getStatus();

            if (statusAtual == null) continue;

            switch (statusAtual) {
                case "aprovada":
                    aprovados++;
                    break;

                case "pendente":
                    pendentes++;
                    break;

                case "recusada":
                    recusados++;
                    break;

                case "concluida":
                case "concluído":
                    concluidos++;
                    break;
            }
        }

        View card = LayoutInflater.from(this).inflate(
                R.layout.item_equipe,
                listaEquipes,
                false
        );

        TextView txtNome = card.findViewById(R.id.txtNome);
        TextView txtDetalhe = card.findViewById(R.id.txtDetalhe);
        TextView txtData = card.findViewById(R.id.txtData);
        TextView status = card.findViewById(R.id.txtStatus);

        // Nome do Skill Pod
        txtNome.setText(principal.getPodNome());

        // Quantidade de integrantes
        txtDetalhe.setText(
                "Skill Pod · " + integrantes.size() + " integrantes"
        );

        // Data do pedido
        if (principal.getCriadoEm() > 0) {

            SimpleDateFormat formato = new SimpleDateFormat(
                    "dd/MM/yyyy",
                    new Locale("pt", "BR")
            );

            txtData.setVisibility(View.VISIBLE);
            txtData.setText(
                    "Pedido em " +
                            formato.format(new Date(principal.getCriadoEm()))
            );

        } else {
            txtData.setVisibility(View.GONE);
        }

        // Resumo curto para não ocupar espaço demais
        String resumo;

        if (pendentes > 0) {
            resumo = aprovados + "/" + integrantes.size() + " aceitos";

            status.setBackgroundResource(R.drawable.bg_tag_laranja);
            status.setTextColor(
                    ContextCompat.getColor(this, R.color.laranja_status)
            );

        } else if (recusados > 0) {
            resumo = recusados + " recusados";

            status.setBackgroundResource(R.drawable.bg_tag_cinza);
            status.setTextColor(
                    ContextCompat.getColor(this, R.color.eq_texto_claro)
            );

        } else if (concluidos == integrantes.size()) {
            resumo = "Concluído";

            status.setBackgroundResource(R.drawable.bg_tag_verde);
            status.setTextColor(
                    ContextCompat.getColor(this, R.color.texto_verde)
            );

        } else {
            resumo = aprovados + "/" + integrantes.size() + " aceitos";

            status.setBackgroundResource(R.drawable.bg_tag_verde);
            status.setTextColor(
                    ContextCompat.getColor(this, R.color.texto_verde)
            );
        }

        status.setText(resumo);
        status.setMaxLines(1);
        status.setEllipsize(android.text.TextUtils.TruncateAt.END);

        listaEquipes.addView(card);
    }



    private void renderizarPedido(Equipe equipe, LinearLayout destino) {

        LayoutInflater inflater = LayoutInflater.from(this);

        View card = inflater.inflate(
                R.layout.item_equipe,
                destino,
                false
        );

        ((TextView) card.findViewById(R.id.txtNome))
                .setText(equipe.getPodNome());

        String detalhe = equipe.getPessoas() == 1
                ? "1 pessoa"
                : equipe.getPessoas() + " pessoas";

        if (equipe.isFreelancer()) {
            detalhe = "Freelancer · " + detalhe;

        } else if (!equipe.getTipo().isEmpty()) {
            detalhe = equipe.getTipo() + " · " + detalhe;
        }

        ((TextView) card.findViewById(R.id.txtDetalhe))
                .setText(detalhe);

        TextView txtData = card.findViewById(R.id.txtData);

        SimpleDateFormat formato = new SimpleDateFormat(
                "dd/MM/yyyy",
                new Locale("pt", "BR")
        );

        if (equipe.getCriadoEm() > 0) {

            txtData.setText(
                    "Pedido em " +
                            formato.format(new Date(equipe.getCriadoEm()))
            );

        } else {
            txtData.setVisibility(View.GONE);
        }

        configurarStatus(
                card.findViewById(R.id.txtStatus),
                equipe.getStatus(),
                equipe.isFreelancer()
        );

        destino.addView(card);

        // Conclusão individual de cada freelancer aprovado.
        if (equipe.isFreelancer()
                && "aprovada".equals(equipe.getStatus())) {

            Button btnConcluir = new Button(this);

            btnConcluir.setText("Marcar serviço como concluído");
            btnConcluir.setAllCaps(false);

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            lp.bottomMargin = dp(12);

            btnConcluir.setLayoutParams(lp);

            btnConcluir.setOnClickListener(v ->

                    new androidx.appcompat.app.AlertDialog.Builder(this)

                            .setTitle("Concluir serviço")

                            .setMessage(
                                    "Confirma que o serviço com " +
                                            equipe.getPodNome() +
                                            " foi concluído? Depois disso, " +
                                            "você poderá avaliar o freelancer."
                            )

                            .setNegativeButton("Cancelar", null)

                            .setPositiveButton("Confirmar", (dialog, which) -> {

                                btnConcluir.setEnabled(false);

                                AvaliacaoRepository.concluirPedido(
                                        equipe.getId(),
                                        new Callback<Void>() {

                                            @Override
                                            public void onSucesso(Void ignorado) {

                                                Toast.makeText(
                                                        MinhasEquipesActivity.this,
                                                        "Serviço concluído. Você já pode avaliar o freelancer.",
                                                        Toast.LENGTH_LONG
                                                ).show();

                                                carregarEquipes();
                                            }

                                            @Override
                                            public void onErro(Exception e) {

                                                btnConcluir.setEnabled(true);

                                                Toast.makeText(
                                                        MinhasEquipesActivity.this,
                                                        "Não foi possível concluir o serviço. Confira as regras do Firebase.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                        }
                                );
                            })

                            .show()
            );

            destino.addView(btnConcluir);
        }
    }

    private int dp(int valor) {
        return Math.round(
                valor * getResources().getDisplayMetrics().density
        );
    }

    private void configurarStatus(
            TextView chip,
            String status,
            boolean freelancer) {

        switch (status) {

            case "aprovada":

                chip.setText(freelancer ? "Aceita" : "Aprovada");

                chip.setBackgroundResource(R.drawable.bg_tag_verde);

                chip.setTextColor(
                        ContextCompat.getColor(this, R.color.texto_verde)
                );

                break;

            case "concluida":

                chip.setText("Concluída");

                chip.setBackgroundResource(R.drawable.bg_tag_verde);

                chip.setTextColor(
                        ContextCompat.getColor(this, R.color.texto_verde)
                );

                break;

            case "recusada":

                chip.setText("Recusada");

                chip.setBackgroundResource(R.drawable.bg_tag_cinza);

                chip.setTextColor(
                        ContextCompat.getColor(this, R.color.eq_texto_claro)
                );

                break;

            case "pendente":
            default:

                chip.setText(
                        freelancer
                                ? "Aguardando freelancer"
                                : "Pendente"
                );

                chip.setBackgroundResource(R.drawable.bg_tag_laranja);

                chip.setTextColor(
                        ContextCompat.getColor(this, R.color.laranja_status)
                );

                break;
        }
    }
}
