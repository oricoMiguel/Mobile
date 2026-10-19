package com.example.pimobile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicoActivity extends AppCompatActivity {

    private static final String TODOS = "Todos";

    private final int[] fundoAvatar = {R.drawable.bg_avatar_roxo, R.drawable.bg_avatar_verde, R.drawable.bg_avatar_laranja};
    private final int[] fundoTag = {R.drawable.bg_tag_roxa, R.drawable.bg_tag_verde, R.drawable.bg_tag_laranja};
    private final int[] corTexto = {R.color.roxo_icone, R.color.texto_verde, R.color.laranja_icone};

    private List<Freelancer> todos;
    private String categoriaAtual = TODOS;
    private String textoBusca = "";

    private LinearLayout listaFreelancers, listaCategorias;
    private TextView txtVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servico);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        listaFreelancers = findViewById(R.id.listaFreelancers);
        listaCategorias = findViewById(R.id.listaCategorias);
        txtVazio = findViewById(R.id.txtVazio);
        EditText edtBusca = findViewById(R.id.edtBusca);

        todos = FreelancerRepository.getTodos();

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnFiltro).setOnClickListener(v ->
                Toast.makeText(this, "Filtros em breve", Toast.LENGTH_SHORT).show());

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                textoBusca = s.toString().trim().toLowerCase(Locale.getDefault());
                renderizarFreelancers();
            }
        });

        renderizarCategorias();
        renderizarFreelancers();
    }

    private void renderizarCategorias() {
        listaCategorias.removeAllViews();

        List<String> categorias = new ArrayList<>();
        categorias.add(TODOS);
        for (Freelancer f : todos) {
            if (!categorias.contains(f.getCategoria())) {
                categorias.add(f.getCategoria());
            }
        }

        for (String categoria : categorias) {
            boolean ativo = categoria.equals(categoriaAtual);

            TextView chip = new TextView(this);
            chip.setText(categoria);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setPadding(dp(16), dp(8), dp(16), dp(8));
            chip.setBackgroundResource(ativo ? R.drawable.bg_chip_filtro_ativo : R.drawable.bg_chip_filtro);
            chip.setTextColor(ContextCompat.getColor(this, ativo ? R.color.branco : R.color.eq_escuro));
            if (ativo) {
                chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                categoriaAtual = categoria;
                renderizarCategorias();
                renderizarFreelancers();
            });

            listaCategorias.addView(chip);
        }
    }

    private void renderizarFreelancers() {
        listaFreelancers.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int exibidos = 0;

        for (Freelancer f : todos) {
            if (!passaNoFiltro(f)) continue;
            exibidos++;

            View card = inflater.inflate(R.layout.item_freelancer, listaFreelancers, false);

            TextView txtIniciais = card.findViewById(R.id.txtIniciais);
            View dotStatus = card.findViewById(R.id.dotStatus);
            TextView txtNome = card.findViewById(R.id.txtNome);
            TextView txtArea = card.findViewById(R.id.txtArea);
            TextView txtNota = card.findViewById(R.id.txtNota);
            TextView txtDisponibilidade = card.findViewById(R.id.txtDisponibilidade);
            LinearLayout listaHabilidades = card.findViewById(R.id.listaHabilidades);

            int cor = f.getCor();

            txtIniciais.setText(f.getIniciais());
            txtIniciais.setBackgroundResource(fundoAvatar[cor]);
            txtIniciais.setTextColor(ContextCompat.getColor(this, corTexto[cor]));

            dotStatus.setBackgroundResource(f.isDisponivel()
                    ? R.drawable.bg_dot_verde : R.drawable.bg_dot_laranja);

            txtNome.setText(f.getNome());
            txtArea.setText(f.getArea());
            txtNota.setText(String.format(Locale.US, "%.1f", f.getNota()));

            txtDisponibilidade.setText(f.getDisponibilidade());
            txtDisponibilidade.setTextColor(ContextCompat.getColor(this,
                    f.isDisponivel() ? R.color.texto_verde : R.color.laranja_status));

            for (String habilidade : f.getHabilidades()) {
                TextView tag = new TextView(this);
                tag.setText(habilidade);
                tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                tag.setTextColor(ContextCompat.getColor(this, corTexto[cor]));
                tag.setBackgroundResource(fundoTag[cor]);
                tag.setPadding(dp(10), dp(4), dp(10), dp(4));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(6));
                tag.setLayoutParams(lp);
                listaHabilidades.addView(tag);
            }

            card.findViewById(R.id.btnVerPerfil).setOnClickListener(v -> {
                // TODO: abrir a tela de perfil do freelancer
                Toast.makeText(this, "Perfil de " + f.getNome(), Toast.LENGTH_SHORT).show();
            });

            listaFreelancers.addView(card);
        }

        txtVazio.setVisibility(exibidos == 0 ? View.VISIBLE : View.GONE);
    }

    private boolean passaNoFiltro(Freelancer f) {
        if (!categoriaAtual.equals(TODOS) && !f.getCategoria().equals(categoriaAtual)) {
            return false;
        }
        if (textoBusca.isEmpty()) {
            return true;
        }
        if (f.getNome().toLowerCase(Locale.getDefault()).contains(textoBusca)) {
            return true;
        }
        for (String habilidade : f.getHabilidades()) {
            if (habilidade.toLowerCase(Locale.getDefault()).contains(textoBusca)) {
                return true;
            }
        }
        return false;
    }

    private int dp(int valor) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics()));
    }
}