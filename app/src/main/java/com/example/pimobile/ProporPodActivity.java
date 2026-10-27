package com.example.pimobile;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProporPodActivity extends AppCompatActivity {

    private static final int MAX_TAGS = 4;

    private final int[] fundoIcone = {R.drawable.bg_icone_roxo, R.drawable.bg_icone_verde, R.drawable.bg_icone_laranja};

    private EditText edtNome, edtDescricao, edtTags;
    private LinearLayout listaFormato;
    private GridLayout gridIcones;
    private Button btnEnviar;

    private boolean digital = true;
    private String iconeEscolhido = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propor_pod);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        edtNome = findViewById(R.id.edtNome);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtTags = findViewById(R.id.edtTags);
        listaFormato = findViewById(R.id.listaFormato);
        gridIcones = findViewById(R.id.gridIcones);
        btnEnviar = findViewById(R.id.btnEnviar);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        btnEnviar.setOnClickListener(v -> enviar());

        renderizarFormato();
        renderizarIcones();
    }

    private void renderizarFormato() {
        listaFormato.removeAllViews();
        String[] opcoes = {"Digital", "Presencial"};

        for (String opcao : opcoes) {
            boolean ativo = opcao.equals("Digital") == digital;

            TextView chip = new TextView(this);
            chip.setText(opcao);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setPadding(dp(16), dp(8), dp(16), dp(8));
            chip.setBackgroundResource(ativo ? R.drawable.bg_chip_filtro_ativo : R.drawable.bg_chip_filtro);
            chip.setTextColor(ContextCompat.getColor(this, ativo ? R.color.branco : R.color.eq_escuro));
            if (ativo) chip.setTypeface(chip.getTypeface(), Typeface.BOLD);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                digital = opcao.equals("Digital");
                renderizarFormato();
            });
            listaFormato.addView(chip);
        }
    }

    private void renderizarIcones() {
        gridIcones.removeAllViews();

        for (IconesPod.Item item : IconesPod.TODOS) {
            boolean selecionado = item.chave.equals(iconeEscolhido);

            LinearLayout celula = new LinearLayout(this);
            celula.setOrientation(LinearLayout.VERTICAL);
            celula.setGravity(Gravity.CENTER_HORIZONTAL);
            celula.setPadding(dp(6), dp(10), dp(6), dp(10));
            celula.setBackgroundResource(selecionado
                    ? R.drawable.bg_card_opcao_selecionado : R.drawable.bg_card_opcao);

            FrameLayout fundo = new FrameLayout(this);
            fundo.setBackgroundResource(fundoIcone[item.cor]);
            fundo.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));

            ImageView img = new ImageView(this);
            img.setImageResource(item.res);
            img.setContentDescription(item.rotulo);
            FrameLayout.LayoutParams imgLp = new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER);
            img.setLayoutParams(imgLp);
            fundo.addView(img);

            TextView rotulo = new TextView(this);
            rotulo.setText(item.rotulo);
            rotulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            rotulo.setGravity(Gravity.CENTER);
            rotulo.setTextColor(ContextCompat.getColor(this, R.color.eq_escuro));
            LinearLayout.LayoutParams rotuloLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rotuloLp.topMargin = dp(6);
            rotulo.setLayoutParams(rotuloLp);

            celula.addView(fundo);
            celula.addView(rotulo);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            celula.setLayoutParams(lp);

            celula.setOnClickListener(v -> {
                iconeEscolhido = item.chave;
                renderizarIcones();
            });
            gridIcones.addView(celula);
        }
    }

    private void enviar() {
        String nome = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        List<String> tags = lerTags(edtTags.getText().toString());

        if (nome.length() < 3) {
            edtNome.setError("Use pelo menos 3 letras");
            edtNome.requestFocus();
            return;
        }
        if (descricao.length() < 10) {
            edtDescricao.setError("Descreva em pelo menos 10 letras");
            edtDescricao.requestFocus();
            return;
        }
        if (tags.isEmpty()) {
            edtTags.setError("Coloque ao menos uma tag, separada por vírgula");
            edtTags.requestFocus();
            return;
        }
        if (tags.size() > MAX_TAGS) {
            edtTags.setError("No máximo " + MAX_TAGS + " tags");
            edtTags.requestFocus();
            return;
        }
        if (iconeEscolhido == null) {
            Toast.makeText(this, "Escolha um ícone", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setAlpha(0.5f);

        PropostaPodRepository.enviar(nome, descricao, digital, tags, iconeEscolhido,
                new Callback<Void>() {
                    @Override
                    public void onSucesso(Void resultado) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(ProporPodActivity.this,
                                "Proposta enviada! Você será avisado quando um administrador decidir.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onErro(Exception e) {
                        if (isFinishing() || isDestroyed()) return;
                        btnEnviar.setEnabled(true);
                        btnEnviar.setAlpha(1f);
                        Toast.makeText(ProporPodActivity.this,
                                "Não foi possível enviar a proposta", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // "Front-end, UX , front-end" vira ["Front-end", "UX"]; tags com mais de 20 letras são cortadas
    private List<String> lerTags(String texto) {
        List<String> tags = new ArrayList<>();
        for (String parte : texto.split(",")) {
            String tag = parte.trim();
            if (tag.isEmpty()) continue;
            if (tag.length() > 20) tag = tag.substring(0, 20).trim();

            boolean repetida = false;
            for (String existente : tags) {
                if (existente.toLowerCase(Locale.getDefault()).equals(tag.toLowerCase(Locale.getDefault()))) {
                    repetida = true;
                    break;
                }
            }
            if (!repetida) tags.add(tag);
        }
        return tags;
    }

    private int dp(int valor) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics()));
    }
}
