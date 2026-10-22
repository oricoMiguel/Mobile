package com.example.pimobile;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificacoesActivity extends AppCompatActivity {

    private LinearLayout listaNotificacoes;
    private TextView txtVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes);
        InsetsUtil.aplicar(this);

        listaNotificacoes = findViewById(R.id.listaNotificacoes);
        txtVazio = findViewById(R.id.txtVazio);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        NotificacaoRepository.getMinhas(new Callback<List<Notificacao>>() {
            @Override
            public void onSucesso(List<Notificacao> avisos) {
                if (isFinishing() || isDestroyed()) return;
                renderizar(avisos);
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                txtVazio.setText("Não foi possível carregar as notificações");
                Toast.makeText(NotificacoesActivity.this,
                        "Não foi possível carregar as notificações", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderizar(List<Notificacao> avisos) {
        listaNotificacoes.removeAllViews();

        if (avisos.isEmpty()) {
            txtVazio.setText("Você não tem notificações");
            txtVazio.setVisibility(View.VISIBLE);
            return;
        }
        txtVazio.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

        for (Notificacao aviso : avisos) {
            View card = inflater.inflate(R.layout.item_notificacao, listaNotificacoes, false);

            ((TextView) card.findViewById(R.id.txtTitulo)).setText(aviso.getTitulo());
            ((TextView) card.findViewById(R.id.txtMensagem)).setText(aviso.getMensagem());

            TextView txtData = card.findViewById(R.id.txtData);
            if (aviso.getCriadoEm() > 0) {
                txtData.setText(formato.format(new Date(aviso.getCriadoEm())));
            } else {
                txtData.setVisibility(View.GONE);
            }

            aplicarEstado(card, aviso);

            card.setOnClickListener(v -> {
                if (!aviso.isLida()) {
                    aviso.setLida(true);
                    NotificacaoRepository.marcarComoLida(aviso.getId());
                    aplicarEstado(card, aviso);
                }
                startActivity(new Intent(this, MinhasEquipesActivity.class));
            });

            listaNotificacoes.addView(card);
        }
    }

    // Não lida: pontinho laranja e título em negrito
    private void aplicarEstado(View card, Notificacao aviso) {
        card.findViewById(R.id.dotNaoLida).setVisibility(aviso.isLida() ? View.INVISIBLE : View.VISIBLE);
        ((TextView) card.findViewById(R.id.txtTitulo))
                .setTypeface(null, aviso.isLida() ? Typeface.NORMAL : Typeface.BOLD);
    }
}