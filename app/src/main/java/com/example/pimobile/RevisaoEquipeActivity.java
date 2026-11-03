
package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RevisaoEquipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revisao_equipe);
        InsetsUtil.aplicar(this);

        Intent dados = getIntent();
        String formato = dados.getStringExtra("formato");

        // Equipe com vários freelancers
        if ("freelancer_multiplo".equals(formato)) {
            configurarFreelancersMultiplos(dados);
            return;
        }

        // Contratação de um freelancer individual
        if ("freelancer".equals(formato)) {
            configurarFreelancer(dados);
            return;
        }

        // Skill Pods: fluxo original preservado
        configurarSkillPod(dados);
    }

    // =====================================================
    // SKILL POD
    // =====================================================

    private void configurarSkillPod(Intent dados) {

        String podId = dados.getStringExtra("pod_id");
        String podNome = dados.getStringExtra("pod_nome");
        String podTipo = dados.getStringExtra("pod_tipo");
        int podPessoas = dados.getIntExtra("pod_pessoas", 0);

        ((TextView) findViewById(R.id.txtTipo))
                .setText("Skill pods · " + podTipo);

        ((TextView) findViewById(R.id.txtPodNome))
                .setText(podNome);

        ((TextView) findViewById(R.id.txtPodDescricao))
                .setText(dados.getStringExtra("pod_descricao"));

        ((TextView) findViewById(R.id.txtPessoas))
                .setText(podPessoas + " pessoas");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        View btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {
            btnConfirmar.setEnabled(false);

            EquipeRepository.salvarEquipePod(
                    podId,
                    podNome,
                    podPessoas,
                    podTipo,
                    new Callback<String>() {
                        @Override
                        public void onSucesso(String equipeId) {
                            if (isFinishing() || isDestroyed()) return;

                            Toast.makeText(
                                    RevisaoEquipeActivity.this,
                                    "Equipe confirmada!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            voltarParaHome();
                        }

                        @Override
                        public void onErro(Exception e) {
                            if (isFinishing() || isDestroyed()) return;

                            btnConfirmar.setEnabled(true);

                            Toast.makeText(
                                    RevisaoEquipeActivity.this,
                                    "Não foi possível confirmar a equipe. Tente novamente.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });
    }

    // =====================================================
    // FREELANCER INDIVIDUAL
    // =====================================================

    private void configurarFreelancer(Intent dados) {

        String freelancerId = dados.getStringExtra("freelancer_id");
        String freelancerNome = dados.getStringExtra("freelancer_nome");

        ((TextView) findViewById(R.id.txtTipo))
                .setText("Freelancers individuais");

        ((TextView) findViewById(R.id.lblItem))
                .setText("Freelancer");

        ((TextView) findViewById(R.id.txtPodNome))
                .setText(freelancerNome);

        ((TextView) findViewById(R.id.txtPodDescricao))
                .setText(dados.getStringExtra("freelancer_descricao"));

        ((TextView) findViewById(R.id.txtPessoas))
                .setText("1 pessoa");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        View btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {
            btnConfirmar.setEnabled(false);

            EquipeRepository.salvarEquipeFreelancer(
                    freelancerId,
                    freelancerNome,
                    new Callback<String>() {
                        @Override
                        public void onSucesso(String equipeId) {
                            if (isFinishing() || isDestroyed()) return;

                            Toast.makeText(
                                    RevisaoEquipeActivity.this,
                                    "Pedido enviado!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            voltarParaHome();
                        }

                        @Override
                        public void onErro(Exception e) {
                            if (isFinishing() || isDestroyed()) return;

                            btnConfirmar.setEnabled(true);

                            Toast.makeText(
                                    RevisaoEquipeActivity.this,
                                    "Não foi possível enviar o pedido. Tente novamente.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });
    }

    // =====================================================
    // EQUIPE COM VÁRIOS FREELANCERS
    // =====================================================

    private void configurarFreelancersMultiplos(Intent dados) {

        ArrayList<String> freelancerIds =
                dados.getStringArrayListExtra("freelancer_ids");

        ArrayList<String> freelancerNomes =
                dados.getStringArrayListExtra("freelancer_nomes");

        // Verifica se os dados recebidos são válidos.
        if (freelancerIds == null || freelancerNomes == null
                || freelancerIds.isEmpty()
                || freelancerIds.size() != freelancerNomes.size()) {

            Toast.makeText(
                    this,
                    "Não foi possível carregar os freelancers selecionados.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        int quantidade = freelancerIds.size();

        // Monta a lista de nomes para a revisão.
        StringBuilder nomes = new StringBuilder();

        for (int i = 0; i < freelancerNomes.size(); i++) {
            nomes.append("• ")
                    .append(freelancerNomes.get(i));

            if (i < freelancerNomes.size() - 1) {
                nomes.append("\n\n");
            }
        }

        ((TextView) findViewById(R.id.txtTipo))
                .setText("Equipe de freelancers");

        ((TextView) findViewById(R.id.lblItem))
                .setText("Integrantes");

        ((TextView) findViewById(R.id.txtPodNome))
                .setText("Equipe com " + quantidade + " freelancers");

        ((TextView) findViewById(R.id.txtPodDescricao))
                .setText(nomes.toString());

        ((TextView) findViewById(R.id.txtPessoas))
                .setText(quantidade + (quantidade == 1
                        ? " pessoa"
                        : " pessoas"));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        View btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {

            btnConfirmar.setEnabled(false);

            EquipeRepository.salvarEquipeFreelancers(
                    freelancerIds,
                    freelancerNomes,
                    new Callback<String>() {

                        @Override
                        public void onSucesso(String grupoEquipeId) {
                            if (isFinishing() || isDestroyed()) return;

                            Toast.makeText(
                                    RevisaoEquipeActivity.this,
                                    "Pedidos enviados para os freelancers!",
                                    Toast.LENGTH_LONG
                            ).show();

                            voltarParaHome();
                        }

                        @Override
                        public void onErro(Exception e) {
                            if (isFinishing() || isDestroyed()) return;

                            btnConfirmar.setEnabled(true);

                            Toast.makeText(
                                    RevisaoEquipeActivity.this,
                                    "Não foi possível enviar os pedidos. Tente novamente.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });
    }

    // =====================================================
    // NAVEGAÇÃO
    // =====================================================

    private void voltarParaHome() {

        Intent home = new Intent(
                RevisaoEquipeActivity.this,
                HomeActivity.class
        );

        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(home);
        finish();
    }
}
