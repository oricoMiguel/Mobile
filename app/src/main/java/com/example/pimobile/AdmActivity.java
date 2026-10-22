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

import java.util.List;

public class AdmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adm);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        findViewById(R.id.btnNotificacoes).setOnClickListener(v ->
                Toast.makeText(this, "Sem notificações", Toast.LENGTH_SHORT).show());

        // O alerta só aparece quando houver algo para revisar
        findViewById(R.id.cardAlerta).setVisibility(View.GONE);

        AdmRepository.getAreas(new Callback<List<AreaAdm>>() {
            @Override
            public void onSucesso(List<AreaAdm> areas) {
                if (isFinishing() || isDestroyed()) return;
                carregarAlerta(areas);
                carregarAreas(areas);
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                if (AdmRepository.permissaoNegada(e)) {
                    Toast.makeText(AdmActivity.this,
                            "Acesso restrito a administradores", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AdmActivity.this,
                            "Não foi possível carregar o painel", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void carregarAlerta(List<AreaAdm> areas) {
        int totalPendentes = 0;
        for (AreaAdm area : areas) {
            totalPendentes += area.getPendentes();
        }

        ((TextView) findViewById(R.id.txtAlertaTitulo)).setText(totalPendentes
                + (totalPendentes == 1 ? " item para revisar" : " itens para revisar"));
        ((TextView) findViewById(R.id.txtAlertaDetalhe)).setText(totalPendentes
                + (totalPendentes == 1 ? " equipe aguardando aprovação"
                : " equipes aguardando aprovação"));

        View.OnClickListener revisar = v ->
                startActivity(new Intent(this, AreaEquipesActivity.class));
        findViewById(R.id.btnRevisar).setOnClickListener(revisar);
        findViewById(R.id.cardAlerta).setOnClickListener(revisar);

        findViewById(R.id.cardAlerta).setVisibility(
                totalPendentes == 0 ? View.GONE : View.VISIBLE);
    }

    private void carregarAreas(List<AreaAdm> areas) {
        LinearLayout lista = findViewById(R.id.listaAreas);
        lista.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (AreaAdm area : areas) {
            View card = inflater.inflate(R.layout.item_area_adm, lista, false);

            FrameLayout iconeContainer = card.findViewById(R.id.iconeContainer);
            ImageView imgIcone = card.findViewById(R.id.imgIcone);

            iconeContainer.setBackgroundResource(area.getFundoIconeRes());
            imgIcone.setImageResource(area.getIconeRes());
            ((TextView) card.findViewById(R.id.txtTitulo)).setText(area.getTitulo());
            ((TextView) card.findViewById(R.id.txtDescricao)).setText(area.getDescricao());
            ((TextView) card.findViewById(R.id.txtResumo)).setText(area.getResumo());

            card.findViewById(R.id.btnAcessar).setOnClickListener(v ->
                    startActivity(new Intent(this, area.getDestino())));

            lista.addView(card);
        }
    }
}