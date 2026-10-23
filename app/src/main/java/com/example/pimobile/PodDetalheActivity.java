package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class PodDetalheActivity extends AppCompatActivity {

    private final int[] fundoIcone = {R.drawable.bg_icone_roxo, R.drawable.bg_icone_verde, R.drawable.bg_icone_laranja};
    private final int[] fundoTag = {R.drawable.bg_tag_roxa, R.drawable.bg_tag_verde, R.drawable.bg_tag_laranja};
    private final int[] fundoAvatar = {R.drawable.bg_avatar_roxo, R.drawable.bg_avatar_verde, R.drawable.bg_avatar_laranja};
    private final int[] corTexto = {R.color.roxo_icone, R.color.texto_verde, R.color.laranja_icone};

    private SkillPod pod;
    private PerfilProfissional perfilAtual;
    private String statusParticipacao; // null = nunca pediu

    private Button btnParticipar;
    private TextView txtParticipacaoInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pod_detalhe);
        InsetsUtil.aplicar(this);

        btnParticipar = findViewById(R.id.btnParticipar);
        txtParticipacaoInfo = findViewById(R.id.txtParticipacaoInfo);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        btnParticipar.setOnClickListener(v -> pedirEntrada());

        String podId = getIntent().getStringExtra("pod_id");
        if (podId == null) {
            Toast.makeText(this, "Skill pod inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SkillPodRepository.getPorId(podId, new Callback<SkillPod>() {
            @Override
            public void onSucesso(SkillPod resultado) {
                if (isFinishing() || isDestroyed()) return;
                pod = resultado;
                preencher(resultado);
                prepararAcao();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                ((TextView) findViewById(R.id.txtCarregando))
                        .setText("Não foi possível carregar este skill pod");
                Toast.makeText(PodDetalheActivity.this,
                        "Não foi possível carregar o skill pod", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void preencher(SkillPod p) {
        int cor = p.getCor();

        findViewById(R.id.iconeContainer).setBackgroundResource(fundoIcone[cor]);
        ((ImageView) findViewById(R.id.imgIcone)).setImageResource(p.getIconeRes());

        ((TextView) findViewById(R.id.txtNome)).setText(p.getNome());
        ((TextView) findViewById(R.id.txtFormato)).setText(p.isDigital() ? "Digital" : "Presencial");
        int total = p.getTotalPessoas();
        ((TextView) findViewById(R.id.txtPessoas))
                .setText("· " + total + (total == 1 ? " pessoa" : " pessoas"));
        ((TextView) findViewById(R.id.txtDescricao)).setText(p.getDescricao());

        LinearLayout listaTags = findViewById(R.id.listaTags);
        listaTags.removeAllViews();
        for (String tagTexto : p.getTags()) {
            TextView tag = new TextView(this);
            tag.setText(tagTexto);
            tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tag.setTextColor(ContextCompat.getColor(this, corTexto[cor]));
            tag.setBackgroundResource(fundoTag[cor]);
            tag.setPadding(dp(12), dp(6), dp(12), dp(6));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            tag.setLayoutParams(lp);
            listaTags.addView(tag);
        }

        montarMembros(p.getIntegrantes());

        findViewById(R.id.txtCarregando).setVisibility(View.GONE);
        findViewById(R.id.conteudoPod).setVisibility(View.VISIBLE);
    }

    private void montarMembros(List<Membro> integrantes) {
        LinearLayout lista = findViewById(R.id.listaMembros);
        lista.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        findViewById(R.id.txtSemMembros).setVisibility(
                integrantes.isEmpty() ? View.VISIBLE : View.GONE);

        for (int i = 0; i < integrantes.size(); i++) {
            Membro membro = integrantes.get(i);
            int cor = i % 3;

            View item = inflater.inflate(R.layout.item_membro_pod, lista, false);

            TextView txtIniciais = item.findViewById(R.id.txtIniciais);
            txtIniciais.setText(membro.getIniciais());
            txtIniciais.setBackgroundResource(fundoAvatar[cor]);
            txtIniciais.setTextColor(ContextCompat.getColor(this, corTexto[cor]));

            ((TextView) item.findViewById(R.id.txtNome)).setText(membro.getNome());

            item.setOnClickListener(v -> {
                if (membro.getUid() == null) return;
                Intent intent = new Intent(this, PerfilFreelancerActivity.class);
                intent.putExtra("freelancer_id", membro.getUid());
                startActivity(intent);
            });

            lista.addView(item);
        }
    }

    // Só freelancers com perfil aprovado podem pedir para entrar
    private void prepararAcao() {
        PerfilRepository.getPerfilAtual(new Callback<Perfil>() {
            @Override
            public void onSucesso(Perfil perfil) {
                if (isFinishing() || isDestroyed()) return;
                if (!"Freelancer".equals(perfil.getTipoConta())) return;

                FreelancerRepository.getMeuPerfil(new Callback<PerfilProfissional>() {
                    @Override
                    public void onSucesso(PerfilProfissional profissional) {
                        if (isFinishing() || isDestroyed()) return;

                        if (profissional == null) {
                            mostrarInfo("Complete seu perfil profissional (Perfil → Editar) "
                                    + "para pedir entrada em um pod.");
                            return;
                        }
                        if (!"aprovado".equals(profissional.getStatus())) {
                            mostrarInfo("Seu perfil profissional precisa ser aprovado "
                                    + "para pedir entrada em um pod.");
                            return;
                        }

                        perfilAtual = profissional;
                        carregarParticipacao();
                    }

                    @Override
                    public void onErro(Exception e) {
                        // Sem o botão, a tela continua funcionando normalmente
                    }
                });
            }

            @Override
            public void onErro(Exception e) {
                // Sem o botão, a tela continua funcionando normalmente
            }
        });
    }

    private void carregarParticipacao() {
        ParticipacaoRepository.getStatus(pod.getId(), new Callback<String>() {
            @Override
            public void onSucesso(String status) {
                if (isFinishing() || isDestroyed()) return;
                statusParticipacao = status;
                configurarBotao();
            }

            @Override
            public void onErro(Exception e) {
                // Sem o botão, a tela continua funcionando normalmente
            }
        });
    }

    private void configurarBotao() {
        btnParticipar.setVisibility(View.VISIBLE);
        txtParticipacaoInfo.setVisibility(View.GONE);

        boolean habilitado = true;
        String texto = "Pedir para entrar";

        if ("pendente".equals(statusParticipacao)) {
            texto = "Pedido enviado";
            habilitado = false;
            mostrarInfo("Aguardando aprovação de um administrador.");
        } else if ("aprovada".equals(statusParticipacao)) {
            texto = "Você faz parte deste pod";
            habilitado = false;
        } else if ("recusada".equals(statusParticipacao)) {
            texto = "Pedir novamente";
            mostrarInfo("Seu pedido anterior foi recusado.");
        }

        btnParticipar.setText(texto);
        btnParticipar.setEnabled(habilitado);
        btnParticipar.setAlpha(habilitado ? 1f : 0.5f);
    }

    private void pedirEntrada() {
        btnParticipar.setEnabled(false);

        ParticipacaoRepository.pedirEntrada(pod.getId(), pod.getNome(), perfilAtual.getNome(),
                statusParticipacao, new Callback<Void>() {
                    @Override
                    public void onSucesso(Void resultado) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(PodDetalheActivity.this,
                                "Pedido enviado", Toast.LENGTH_SHORT).show();
                        statusParticipacao = "pendente";
                        configurarBotao();
                    }

                    @Override
                    public void onErro(Exception e) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(PodDetalheActivity.this,
                                "Não foi possível enviar o pedido", Toast.LENGTH_LONG).show();
                        configurarBotao();
                    }
                });
    }

    private void mostrarInfo(String mensagem) {
        txtParticipacaoInfo.setText(mensagem);
        txtParticipacaoInfo.setVisibility(View.VISIBLE);
    }

    private int dp(int valor) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics()));
    }
}