package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RevisaoEquipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revisao_equipe);
        InsetsUtil.aplicar(this);

        Intent dados = getIntent();
        boolean freelancer = "freelancer".equals(dados.getStringExtra("formato"));
        if (freelancer) {
            configurarFreelancer(dados);
            return;
        }

        String podId = dados.getStringExtra("pod_id");
        String podNome = dados.getStringExtra("pod_nome");
        String podTipo = dados.getStringExtra("pod_tipo");
        int podPessoas = dados.getIntExtra("pod_pessoas", 0);

        ((TextView) findViewById(R.id.txtTipo)).setText("Skill pods · " + podTipo);
        ((TextView) findViewById(R.id.txtPodNome)).setText(podNome);
        ((TextView) findViewById(R.id.txtPodDescricao))
                .setText(dados.getStringExtra("pod_descricao"));
        ((TextView) findViewById(R.id.txtPessoas)).setText(podPessoas + " pessoas");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        View btnConfirmar = findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(v -> {
            btnConfirmar.setEnabled(false);

            EquipeRepository.salvarEquipePod(podId, podNome, podPessoas, podTipo,
                    new Callback<String>() {
                        @Override
                        public void onSucesso(String equipeId) {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(RevisaoEquipeActivity.this,
                                    "Equipe confirmada!", Toast.LENGTH_SHORT).show();
                            Intent home = new Intent(RevisaoEquipeActivity.this, HomeActivity.class);
                            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(home);
                        }

                        @Override
                        public void onErro(Exception e) {
                            if (isFinishing() || isDestroyed()) return;
                            btnConfirmar.setEnabled(true);
                            Toast.makeText(RevisaoEquipeActivity.this,
                                    "Não foi possível confirmar a equipe. Tente novamente.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // Contratação de um freelancer individual: mesma revisão, outro pedido
    private void configurarFreelancer(Intent dados) {
        String freelancerId = dados.getStringExtra("freelancer_id");
        String freelancerNome = dados.getStringExtra("freelancer_nome");

        ((TextView) findViewById(R.id.txtTipo)).setText("Freelancers individuais");
        ((TextView) findViewById(R.id.lblItem)).setText("Freelancer");
        ((TextView) findViewById(R.id.txtPodNome)).setText(freelancerNome);
        ((TextView) findViewById(R.id.txtPodDescricao))
                .setText(dados.getStringExtra("freelancer_descricao"));
        ((TextView) findViewById(R.id.txtPessoas)).setText("1 pessoa");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        View btnConfirmar = findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(v -> {
            btnConfirmar.setEnabled(false);

            EquipeRepository.salvarEquipeFreelancer(freelancerId, freelancerNome,
                    new Callback<String>() {
                        @Override
                        public void onSucesso(String equipeId) {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(RevisaoEquipeActivity.this,
                                    "Pedido enviado!", Toast.LENGTH_SHORT).show();
                            Intent home = new Intent(RevisaoEquipeActivity.this, HomeActivity.class);
                            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(home);
                        }

                        @Override
                        public void onErro(Exception e) {
                            if (isFinishing() || isDestroyed()) return;
                            btnConfirmar.setEnabled(true);
                            Toast.makeText(RevisaoEquipeActivity.this,
                                    "Não foi possível enviar o pedido. Tente novamente.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}