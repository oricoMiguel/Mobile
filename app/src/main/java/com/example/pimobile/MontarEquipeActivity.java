package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MontarEquipeActivity extends AppCompatActivity {

    private static class PodOpcao {
        final String nome;
        final String descricao;
        final int pessoas;
        final boolean digital;
        final int iconeRes;
        final int fundoRes;

        PodOpcao(String nome, String descricao, int pessoas, boolean digital, int iconeRes, int fundoRes) {
            this.nome = nome;
            this.descricao = descricao;
            this.pessoas = pessoas;
            this.digital = digital;
            this.iconeRes = iconeRes;
            this.fundoRes = fundoRes;
        }
    }

    private final List<PodOpcao> todosPods = new ArrayList<>();

    private boolean formatoEscolhido = false;
    private boolean formatoSkillPods = false;
    private boolean mostrandoDigitais = true;
    private PodOpcao podSelecionado = null;

    private TextView circ1, circ2, circ3, tabDigitais, tabPresenciais, txtResumo;
    private View cardFreelancers, cardSkillPods, secaoPods;
    private ImageView checkFreelancers, checkSkillPods;
    private LinearLayout listaPods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_montar_equipe);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        circ1 = findViewById(R.id.circ1);
        circ2 = findViewById(R.id.circ2);
        circ3 = findViewById(R.id.circ3);
        tabDigitais = findViewById(R.id.tabDigitais);
        tabPresenciais = findViewById(R.id.tabPresenciais);
        txtResumo = findViewById(R.id.txtResumo);
        cardFreelancers = findViewById(R.id.cardFreelancers);
        cardSkillPods = findViewById(R.id.cardSkillPods);
        secaoPods = findViewById(R.id.secaoPods);
        checkFreelancers = findViewById(R.id.checkFreelancers);
        checkSkillPods = findViewById(R.id.checkSkillPods);
        listaPods = findViewById(R.id.listaPods);

        carregarPods();

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        cardFreelancers.setOnClickListener(v -> escolherFormato(false));
        cardSkillPods.setOnClickListener(v -> escolherFormato(true));

        tabDigitais.setOnClickListener(v -> {
            mostrandoDigitais = true;
            podSelecionado = null;
            atualizarTela();
        });
        tabPresenciais.setOnClickListener(v -> {
            mostrandoDigitais = false;
            podSelecionado = null;
            atualizarTela();
        });

        findViewById(R.id.btnContinuar).setOnClickListener(v -> continuar());

        atualizarTela();
    }

    private void carregarPods() {
        // TODO: buscar estes pods do mesmo lugar que a tela Skill Pods (banco de dados ou API)
        todosPods.add(new PodOpcao("Desenvolvimento web", "Front-end, back-end e QA · 3 pessoas",
                3, true, R.drawable.ic_code, R.drawable.bg_icone_roxo));
        todosPods.add(new PodOpcao("Design e UX", "UI, pesquisa e protótipo · 2 pessoas",
                2, true, R.drawable.ic_palette, R.drawable.bg_icone_verde));
        todosPods.add(new PodOpcao("Marketing digital", "Conteúdo, tráfego e social · 3 pessoas",
                3, true, R.drawable.ic_megaphone, R.drawable.bg_icone_laranja));
        todosPods.add(new PodOpcao("Desenvolvimento local", "Equipe no seu endereço · 3 pessoas",
                3, false, R.drawable.ic_code, R.drawable.bg_icone_roxo));
        todosPods.add(new PodOpcao("Design presencial", "Workshops e prototipação · 2 pessoas",
                2, false, R.drawable.ic_palette, R.drawable.bg_icone_verde));
    }

    private void escolherFormato(boolean skillPods) {
        formatoEscolhido = true;
        formatoSkillPods = skillPods;
        if (!skillPods) {
            podSelecionado = null;
        }
        atualizarTela();
    }

    private void atualizarTela() {
        // Bolinhas do stepper
        setCirculo(circ1, formatoEscolhido);
        setCirculo(circ2, formatoEscolhido && formatoSkillPods && podSelecionado != null);
        setCirculo(circ3, false);

        // Cards de formato
        boolean freeSel = formatoEscolhido && !formatoSkillPods;
        boolean podsSel = formatoEscolhido && formatoSkillPods;
        cardFreelancers.setBackgroundResource(freeSel
                ? R.drawable.bg_card_opcao_selecionado : R.drawable.bg_card_opcao);
        cardSkillPods.setBackgroundResource(podsSel
                ? R.drawable.bg_card_opcao_selecionado : R.drawable.bg_card_opcao);
        checkFreelancers.setVisibility(freeSel ? View.VISIBLE : View.GONE);
        checkSkillPods.setVisibility(podsSel ? View.VISIBLE : View.GONE);

        // Seção de pods
        secaoPods.setVisibility(podsSel ? View.VISIBLE : View.GONE);
        tabDigitais.setBackgroundResource(mostrandoDigitais ? R.drawable.bg_segmento_ativo : 0);
        tabDigitais.setTextColor(ContextCompat.getColor(this,
                mostrandoDigitais ? R.color.branco : R.color.eq_texto_claro));
        tabPresenciais.setBackgroundResource(!mostrandoDigitais ? R.drawable.bg_segmento_ativo : 0);
        tabPresenciais.setTextColor(ContextCompat.getColor(this,
                !mostrandoDigitais ? R.color.branco : R.color.eq_texto_claro));

        renderizarLista();

        // Resumo da barra inferior
        if (freeSel) {
            txtResumo.setText("Freelancers individuais");
        } else if (podSelecionado != null) {
            txtResumo.setText("1 pod · " + podSelecionado.pessoas + " pessoas");
        } else {
            txtResumo.setText("Nada selecionado");
        }
    }

    private void renderizarLista() {
        listaPods.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PodOpcao pod : todosPods) {
            if (pod.digital != mostrandoDigitais) continue;

            View item = inflater.inflate(R.layout.item_pod_opcao, listaPods, false);
            FrameLayout iconeContainer = item.findViewById(R.id.iconeContainer);
            ImageView imgIcone = item.findViewById(R.id.imgIcone);
            ImageView imgRadio = item.findViewById(R.id.imgRadio);
            TextView txtNome = item.findViewById(R.id.txtNome);
            TextView txtDescricao = item.findViewById(R.id.txtDescricao);

            iconeContainer.setBackgroundResource(pod.fundoRes);
            imgIcone.setImageResource(pod.iconeRes);
            txtNome.setText(pod.nome);
            txtDescricao.setText(pod.descricao);

            boolean selecionado = pod == podSelecionado;
            item.setBackgroundResource(selecionado
                    ? R.drawable.bg_card_opcao_selecionado : R.drawable.bg_card_opcao);
            imgRadio.setImageResource(selecionado ? R.drawable.ic_radio_on : R.drawable.ic_radio_off);

            item.setOnClickListener(v -> {
                podSelecionado = (podSelecionado == pod) ? null : pod;
                atualizarTela();
            });

            listaPods.addView(item);
        }
    }

    private void setCirculo(TextView circulo, boolean cheio) {
        circulo.setBackgroundResource(cheio ? R.drawable.bg_circulo_ativo : R.drawable.bg_circulo_inativo);
        circulo.setTextColor(ContextCompat.getColor(this,
                cheio ? R.color.branco : R.color.eq_texto_claro));
    }

    private void continuar() {
        if (!formatoEscolhido) {
            Toast.makeText(this, "Escolha como você quer trabalhar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!formatoSkillPods) {
            startActivity(new Intent(this, ServicoActivity.class));
            return;
        }

        if (podSelecionado == null) {
            Toast.makeText(this, "Escolha um skill pod", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, RevisaoEquipeActivity.class);
        intent.putExtra("pod_nome", podSelecionado.nome);
        intent.putExtra("pod_descricao", podSelecionado.descricao);
        intent.putExtra("pod_pessoas", podSelecionado.pessoas);
        intent.putExtra("pod_tipo", podSelecionado.digital ? "Digital" : "Presencial");
        startActivity(intent);
    }
}