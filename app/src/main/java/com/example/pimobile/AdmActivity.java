package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
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
        int total = 0;
        Class<?> primeiroDestino = null;
        List<String> partes = new ArrayList<>();

        for (AreaAdm area : areas) {
            int pendentes = area.getPendentes();
            if (pendentes <= 0) continue;

            total += pendentes;
            if (primeiroDestino == null) primeiroDestino = area.getDestino();
            partes.add(rotuloPendentes(area));
        }

        if (total == 0) {
            findViewById(R.id.cardAlerta).setVisibility(View.GONE);
            return;
        }

        String detalhe = partes.size() == 1
                ? partes.get(0)
                : TextUtils.join(", ", partes.subList(0, partes.size() - 1))
                  + " e " + partes.get(partes.size() - 1);

        ((TextView) findViewById(R.id.txtAlertaTitulo)).setText(total
                + (total == 1 ? " item para revisar" : " itens para revisar"));
        ((TextView) findViewById(R.id.txtAlertaDetalhe)).setText(detalhe + " aguardando aprovação");

        // Abre primeiro a área que tem pendências
        Class<?> destino = primeiroDestino;
        View.OnClickListener revisar = v -> startActivity(new Intent(this, destino));
        findViewById(R.id.btnRevisar).setOnClickListener(revisar);
        findViewById(R.id.cardAlerta).setOnClickListener(revisar);

        findViewById(R.id.cardAlerta).setVisibility(View.VISIBLE);
    }

    private String rotuloPendentes(AreaAdm area) {
        int n = area.getPendentes();
        if (area.getDestino() == AreaEquipesActivity.class) {
            return n + (n == 1 ? " equipe" : " equipes");
        }
        if (area.getDestino() == AreaFreelancerActivity.class) {
            return n + (n == 1 ? " perfil de freelancer" : " perfis de freelancer");
        }
        if (area.getDestino() == AreaParticipacoesActivity.class) {
            return n + (n == 1 ? " pedido de pod" : " pedidos de pod");
        }
        return n + (n == 1 ? " item" : " itens");
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