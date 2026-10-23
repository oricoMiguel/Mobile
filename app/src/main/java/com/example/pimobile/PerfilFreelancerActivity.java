package com.example.pimobile;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PerfilFreelancerActivity extends AppCompatActivity {

    private final int[] fundoAvatar = {R.drawable.bg_avatar_roxo, R.drawable.bg_avatar_verde, R.drawable.bg_avatar_laranja};
    private final int[] fundoTag = {R.drawable.bg_tag_roxa, R.drawable.bg_tag_verde, R.drawable.bg_tag_laranja};
    private final int[] corTexto = {R.color.roxo_icone, R.color.texto_verde, R.color.laranja_icone};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_freelancer);
        InsetsUtil.aplicar(this);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra("freelancer_id");
        if (id == null) {
            Toast.makeText(this, "Perfil inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FreelancerRepository.getPorId(id, new Callback<Freelancer>() {
            @Override
            public void onSucesso(Freelancer freelancer) {
                if (isFinishing() || isDestroyed()) return;
                preencher(freelancer);
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                ((TextView) findViewById(R.id.txtCarregando))
                        .setText("Não foi possível carregar este perfil");
                Toast.makeText(PerfilFreelancerActivity.this,
                        "Não foi possível carregar o perfil", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void preencher(Freelancer f) {
        int cor = f.getCor();

        TextView txtIniciais = findViewById(R.id.txtIniciais);
        txtIniciais.setText(f.getIniciais());
        txtIniciais.setBackgroundResource(fundoAvatar[cor]);
        txtIniciais.setTextColor(ContextCompat.getColor(this, corTexto[cor]));

        ((TextView) findViewById(R.id.txtNome)).setText(f.getNome());
        ((TextView) findViewById(R.id.txtArea)).setText(f.getArea());
        ((TextView) findViewById(R.id.txtNota)).setText(f.getNota() > 0
                ? String.format(Locale.US, "%.1f", f.getNota()) : "Novo");
        ((TextView) findViewById(R.id.txtCategoria)).setText("· " + f.getCategoria());

        findViewById(R.id.dotStatus).setBackgroundResource(f.isDisponivel()
                ? R.drawable.bg_dot_verde : R.drawable.bg_dot_laranja);
        TextView txtDisponibilidade = findViewById(R.id.txtDisponibilidade);
        txtDisponibilidade.setText(f.getDisponibilidade());
        txtDisponibilidade.setTextColor(ContextCompat.getColor(this,
                f.isDisponivel() ? R.color.texto_verde : R.color.laranja_status));

        LinearLayout listaHabilidades = findViewById(R.id.listaHabilidades);
        listaHabilidades.removeAllViews();
        for (String habilidade : f.getHabilidades()) {
            TextView tag = new TextView(this);
            tag.setText(habilidade);
            tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tag.setTextColor(ContextCompat.getColor(this, corTexto[cor]));
            tag.setBackgroundResource(fundoTag[cor]);
            tag.setPadding(dp(12), dp(6), dp(12), dp(6));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            tag.setLayoutParams(lp);
            listaHabilidades.addView(tag);
        }

        TextView txtMembroDesde = findViewById(R.id.txtMembroDesde);
        if (f.getCriadoEm() > 0) {
            SimpleDateFormat formato = new SimpleDateFormat("MMMM 'de' yyyy", new Locale("pt", "BR"));
            txtMembroDesde.setText("Membro desde " + formato.format(new Date(f.getCriadoEm())));
        } else {
            txtMembroDesde.setVisibility(View.GONE);
        }

        findViewById(R.id.txtCarregando).setVisibility(View.GONE);
        findViewById(R.id.conteudoPerfil).setVisibility(View.VISIBLE);
    }

    private int dp(int valor) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics()));
    }
}