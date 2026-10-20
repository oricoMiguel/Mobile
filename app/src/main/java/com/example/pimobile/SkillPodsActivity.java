package com.example.pimobile;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkillPodsActivity extends AppCompatActivity {

    private static final String TODOS = "Todos";
    private static final String DIGITAIS = "Digitais";
    private static final String PRESENCIAIS = "Presenciais";

    private final int[] fundoIcone = {R.drawable.bg_icone_roxo, R.drawable.bg_icone_verde, R.drawable.bg_icone_laranja};
    private final int[] fundoTag = {R.drawable.bg_tag_roxa, R.drawable.bg_tag_verde, R.drawable.bg_tag_laranja};
    private final int[] fundoMini = {R.drawable.bg_mini_roxo, R.drawable.bg_mini_verde, R.drawable.bg_mini_laranja};
    private final int[] corTexto = {R.color.roxo_icone, R.color.texto_verde, R.color.laranja_icone};

    private List<SkillPod> todos;
    private String filtroAtual = TODOS;
    private String textoBusca = "";

    private LinearLayout listaPods, listaFiltros;
    private TextView txtVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_pods);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        listaPods = findViewById(R.id.listaPods);
        listaFiltros = findViewById(R.id.listaFiltros);
        txtVazio = findViewById(R.id.txtVazio);
        EditText edtBusca = findViewById(R.id.edtBusca);

        todos = new ArrayList<>();

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnFiltro).setOnClickListener(v ->
                Toast.makeText(this, "Filtros em breve", Toast.LENGTH_SHORT).show());

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                textoBusca = s.toString().trim().toLowerCase(Locale.getDefault());
                renderizarPods();
            }
        });

        renderizarFiltros();

        SkillPodRepository.getTodos(new Callback<List<SkillPod>>() {
            @Override
            public void onSucesso(List<SkillPod> pods) {
                if (isFinishing() || isDestroyed()) return;
                todos = pods;
                renderizarPods();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(SkillPodsActivity.this,
                        "Não foi possível carregar os Skill Pods", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderizarFiltros() {
        listaFiltros.removeAllViews();
        String[] filtros = {TODOS, DIGITAIS, PRESENCIAIS};

        for (String filtro : filtros) {
            boolean ativo = filtro.equals(filtroAtual);

            TextView chip = new TextView(this);
            chip.setText(filtro);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setPadding(dp(16), dp(8), dp(16), dp(8));
            chip.setBackgroundResource(ativo ? R.drawable.bg_chip_filtro_ativo : R.drawable.bg_chip_filtro);
            chip.setTextColor(ContextCompat.getColor(this, ativo ? R.color.branco : R.color.eq_escuro));
            if (ativo) {
                chip.setTypeface(chip.getTypeface(), Typeface.BOLD);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                filtroAtual = filtro;
                renderizarFiltros();
                renderizarPods();
            });

            listaFiltros.addView(chip);
        }
    }

    private void renderizarPods() {
        listaPods.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int exibidos = 0;

        for (SkillPod pod : todos) {
            if (!passaNoFiltro(pod)) continue;
            exibidos++;

            View card = inflater.inflate(R.layout.item_skill_pod, listaPods, false);
            int cor = pod.getCor();

            FrameLayout iconeContainer = card.findViewById(R.id.iconeContainer);
            ImageView imgIcone = card.findViewById(R.id.imgIcone);
            TextView txtNome = card.findViewById(R.id.txtNome);
            TextView txtPessoas = card.findViewById(R.id.txtPessoas);
            TextView txtDescricao = card.findViewById(R.id.txtDescricao);
            LinearLayout listaTags = card.findViewById(R.id.listaTags);
            LinearLayout listaMembros = card.findViewById(R.id.listaMembros);

            iconeContainer.setBackgroundResource(fundoIcone[cor]);
            imgIcone.setImageResource(pod.getIconeRes());
            txtNome.setText(pod.getNome());
            txtPessoas.setText(String.valueOf(pod.getTotalPessoas()));
            txtDescricao.setText(pod.getDescricao());

            for (String tagTexto : pod.getTags()) {
                TextView tag = new TextView(this);
                tag.setText(tagTexto);
                tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                tag.setTextColor(ContextCompat.getColor(this, corTexto[cor]));
                tag.setBackgroundResource(fundoTag[cor]);
                tag.setPadding(dp(10), dp(4), dp(10), dp(4));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(6));
                tag.setLayoutParams(lp);
                listaTags.addView(tag);
            }

            montarMembros(listaMembros, pod);

            card.findViewById(R.id.btnVerPod).setOnClickListener(v -> {
                // TODO: abrir os detalhes do skill pod
                Toast.makeText(this, pod.getNome(), Toast.LENGTH_SHORT).show();
            });

            listaPods.addView(card);
        }

        txtVazio.setVisibility(exibidos == 0 ? View.VISIBLE : View.GONE);
    }

    // Até 3 pessoas aparecem todas; acima disso, 2 avatares + "+N"
    private void montarMembros(LinearLayout container, SkillPod pod) {
        List<String> membros = pod.getMembros();
        int total = pod.getTotalPessoas();
        boolean temExtra = total > 3;
        int visiveis = temExtra ? 2 : Math.min(membros.size(), 3);
        int posicao = 0;

        for (int i = 0; i < visiveis && i < membros.size(); i++) {
            int corMembro = i % 3;
            container.addView(criarAvatar(membros.get(i), fundoMini[corMembro],
                    corTexto[corMembro], posicao++));
        }

        if (temExtra) {
            container.addView(criarAvatar("+" + (total - 2), R.drawable.bg_mini_cinza,
                    R.color.eq_texto_claro, posicao));
        }
    }

    private TextView criarAvatar(String texto, int fundoRes, int corTextoRes, int posicao) {
        TextView avatar = new TextView(this);
        avatar.setText(texto);
        avatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        avatar.setTypeface(avatar.getTypeface(), Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(fundoRes);
        avatar.setTextColor(ContextCompat.getColor(this, corTextoRes));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(30), dp(30));
        if (posicao > 0) {
            lp.setMarginStart(-dp(8)); // sobrepõe os avatares
        }
        avatar.setLayoutParams(lp);
        return avatar;
    }

    private boolean passaNoFiltro(SkillPod pod) {
        if (filtroAtual.equals(DIGITAIS) && !pod.isDigital()) return false;
        if (filtroAtual.equals(PRESENCIAIS) && pod.isDigital()) return false;

        if (textoBusca.isEmpty()) return true;
        if (pod.getNome().toLowerCase(Locale.getDefault()).contains(textoBusca)) return true;
        for (String tag : pod.getTags()) {
            if (tag.toLowerCase(Locale.getDefault()).contains(textoBusca)) return true;
        }
        return false;
    }

    private int dp(int valor) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics()));
    }
}