package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServicoActivity extends AppCompatActivity {

    private final int[] fundoAvatar = {
            R.drawable.bg_avatar_roxo,
            R.drawable.bg_avatar_verde,
            R.drawable.bg_avatar_laranja
    };

    private final int[] fundoTag = {
            R.drawable.bg_tag_roxa,
            R.drawable.bg_tag_verde,
            R.drawable.bg_tag_laranja
    };

    private final int[] corTexto = {
            R.color.roxo_icone,
            R.color.texto_verde,
            R.color.laranja_icone
    };

    private List<Freelancer> todos;
    private String textoBusca = "";

    private LinearLayout listaFreelancers;
    private LinearLayout barraSelecao;
    private TextView txtVazio;
    private TextView txtQuantidadeSelecionada;

    private boolean modoEquipe = false;

    // LinkedHashMap mantém a ordem em que os profissionais foram selecionados.
    private final Map<String, Freelancer> selecionados = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servico);

        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        modoEquipe = getIntent().getBooleanExtra("modo_equipe", false);

        listaFreelancers = findViewById(R.id.listaFreelancers);
        txtVazio = findViewById(R.id.txtVazio);
        txtQuantidadeSelecionada = findViewById(R.id.txtQuantidadeSelecionada);
        barraSelecao = findViewById(R.id.barraSelecao);

        EditText edtBusca = findViewById(R.id.edtBusca);

        todos = new ArrayList<>();

        barraSelecao.setVisibility(modoEquipe ? View.VISIBLE : View.GONE);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        findViewById(R.id.btnFiltro).setOnClickListener(v ->
                Toast.makeText(this, "Filtros em breve", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnRevisarEquipe).setOnClickListener(v ->
                continuarParaRevisao());

        atualizarBarraSelecao();

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textoBusca = s.toString().trim().toLowerCase(Locale.getDefault());
                renderizarFreelancers();
            }
        });

        FreelancerRepository.getTodos(new Callback<List<Freelancer>>() {
            @Override
            public void onSucesso(List<Freelancer> freelancers) {
                if (isFinishing() || isDestroyed()) return;

                todos = freelancers;

                AvaliacaoRepository.aplicarMedias(todos, new Callback<Void>() {
                    @Override
                    public void onSucesso(Void ignorado) {
                        if (!isFinishing() && !isDestroyed()) {
                            renderizarFreelancers();
                        }
                    }

                    @Override
                    public void onErro(Exception e) {
                        if (!isFinishing() && !isDestroyed()) {
                            for (Freelancer freelancer : todos) {
                                freelancer.setNota(0.0);
                            }

                            renderizarFreelancers();
                        }
                    }
                });
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;

                Toast.makeText(ServicoActivity.this,
                        "Não foi possível carregar os freelancers",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderizarFreelancers() {
        listaFreelancers.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        int exibidos = 0;

        for (Freelancer f : todos) {
            if (!passaNoFiltro(f)) continue;

            exibidos++;

            View card = inflater.inflate(
                    R.layout.item_freelancer,
                    listaFreelancers,
                    false
            );

            TextView txtIniciais = card.findViewById(R.id.txtIniciais);
            ImageView imgFoto = card.findViewById(R.id.imgFotoPerfil);
            View dotStatus = card.findViewById(R.id.dotStatus);
            TextView txtNome = card.findViewById(R.id.txtNome);
            TextView txtArea = card.findViewById(R.id.txtArea);
            TextView txtNota = card.findViewById(R.id.txtNota);
            TextView txtDisponibilidade =
                    card.findViewById(R.id.txtDisponibilidade);

            LinearLayout listaHabilidades =
                    card.findViewById(R.id.listaHabilidades);

            CheckBox chkSelecionar =
                    card.findViewById(R.id.chkSelecionar);

            int cor = f.getCor();

            // Iniciais e foto de perfil
            txtIniciais.setText(f.getIniciais());
            txtIniciais.setBackgroundResource(fundoAvatar[cor]);
            txtIniciais.setTextColor(
                    ContextCompat.getColor(this, corTexto[cor])
            );

            if (f.getFotoUrl() != null && !f.getFotoUrl().trim().isEmpty()) {
                Glide.with(this)
                        .load(f.getFotoUrl())
                        .circleCrop()
                        .into(imgFoto);

                imgFoto.setVisibility(View.VISIBLE);
                txtIniciais.setVisibility(View.GONE);
            } else {
                imgFoto.setVisibility(View.GONE);
                txtIniciais.setVisibility(View.VISIBLE);
            }

            // Disponibilidade
            dotStatus.setBackgroundResource(
                    f.isDisponivel()
                            ? R.drawable.bg_dot_verde
                            : R.drawable.bg_dot_laranja
            );

            txtNome.setText(f.getNome());
            txtArea.setText(f.getArea());

            txtNota.setText(f.getNota() > 0
                    ? String.format(Locale.US, "%.1f", f.getNota())
                    : (f.isNovo() ? "Novo" : "Sem avaliações"));

            txtDisponibilidade.setText(f.getDisponibilidade());

            txtDisponibilidade.setTextColor(
                    ContextCompat.getColor(
                            this,
                            f.isDisponivel()
                                    ? R.color.texto_verde
                                    : R.color.laranja_status
                    )
            );

            // Habilidades
            for (String habilidade : f.getHabilidades()) {
                TextView tag = new TextView(this);

                tag.setText(habilidade);
                tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                tag.setTextColor(
                        ContextCompat.getColor(this, corTexto[cor])
                );
                tag.setBackgroundResource(fundoTag[cor]);
                tag.setPadding(dp(10), dp(4), dp(10), dp(4));

                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );

                lp.setMarginEnd(dp(6));
                tag.setLayoutParams(lp);

                listaHabilidades.addView(tag);
            }


// Seleção múltipla: aparece somente ao montar uma equipe.
            if (modoEquipe) {

                chkSelecionar.setVisibility(View.VISIBLE);

                // Recupera a seleção anterior sem disparar o listener.
                chkSelecionar.setOnCheckedChangeListener(null);
                chkSelecionar.setChecked(selecionados.containsKey(f.getId()));

                chkSelecionar.setOnCheckedChangeListener((buttonView, checked) -> {

                    if (checked) {

                        // Respeita o limite definido no Repository.
                        if (selecionados.size() >= EquipeRepository.MAX_FREELANCERS_EQUIPE) {

                            Toast.makeText(
                                    ServicoActivity.this,
                                    "Você pode selecionar no máximo "
                                            + EquipeRepository.MAX_FREELANCERS_EQUIPE
                                            + " freelancers.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            buttonView.setChecked(false);
                            return;
                        }

                        selecionados.put(f.getId(), f);

                    } else {

                        selecionados.remove(f.getId());
                    }

                    atualizarBarraSelecao();
                });

            } else {

                chkSelecionar.setVisibility(View.GONE);
            }


            // Visualizar perfil continua funcionando normalmente.
            card.findViewById(R.id.btnVerPerfil).setOnClickListener(v -> {
                Intent intent = new Intent(this, PerfilFreelancerActivity.class);
                intent.putExtra("freelancer_id", f.getId());
                startActivity(intent);
            });

            listaFreelancers.addView(card);
        }

        txtVazio.setVisibility(
                exibidos == 0 ? View.VISIBLE : View.GONE
        );
    }

    private void atualizarBarraSelecao() {
        if (!modoEquipe) return;

        int quantidade = selecionados.size();

        if (quantidade == 0) {
            txtQuantidadeSelecionada.setText("Nenhum profissional selecionado");
        } else if (quantidade == 1) {
            txtQuantidadeSelecionada.setText("1 profissional selecionado");
        } else {
            txtQuantidadeSelecionada.setText(
                    quantidade + " profissionais selecionados"
            );
        }

        findViewById(R.id.btnRevisarEquipe).setEnabled(quantidade > 0);

        findViewById(R.id.btnRevisarEquipe).setAlpha(
                quantidade > 0 ? 1f : 0.5f
        );
    }

    private void continuarParaRevisao() {
        if (selecionados.isEmpty()) {
            Toast.makeText(
                    this,
                    "Selecione pelo menos um freelancer",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> nomes = new ArrayList<>();

        for (Freelancer freelancer : selecionados.values()) {
            ids.add(freelancer.getId());
            nomes.add(freelancer.getNome());
        }

        Intent intent = new Intent(this, RevisaoEquipeActivity.class);

        intent.putExtra("formato", "freelancer_multiplo");
        intent.putStringArrayListExtra("freelancer_ids", ids);
        intent.putStringArrayListExtra("freelancer_nomes", nomes);

        startActivity(intent);
    }

    private boolean passaNoFiltro(Freelancer f) {
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
                TypedValue.COMPLEX_UNIT_DIP,
                valor,
                getResources().getDisplayMetrics()
        ));
    }
}