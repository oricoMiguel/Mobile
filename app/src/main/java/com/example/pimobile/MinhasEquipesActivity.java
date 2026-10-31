package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MinhasEquipesActivity extends AppCompatActivity {

    private LinearLayout listaEquipes;
    private TextView txtVazio, btnMontarEquipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_equipes);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        listaEquipes = findViewById(R.id.listaEquipes);
        txtVazio = findViewById(R.id.txtVazio);
        btnMontarEquipe = findViewById(R.id.btnMontarEquipe);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        btnMontarEquipe.setOnClickListener(v ->
                startActivity(new Intent(this, MontarEquipeActivity.class)));
    }

    // Recarrega ao voltar para a tela, assim o status novo (aceito/recusado) aparece
    @Override
    protected void onResume() {
        super.onResume();
        carregarEquipes();
    }

    private void carregarEquipes() {
        EquipeRepository.getMinhasEquipes(new Callback<List<Equipe>>() {
            @Override
            public void onSucesso(List<Equipe> equipes) {
                if (isFinishing() || isDestroyed()) return;
                renderizar(equipes);
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                txtVazio.setText("Não foi possível carregar suas equipes");
                txtVazio.setVisibility(View.VISIBLE);
                Toast.makeText(MinhasEquipesActivity.this,
                        "Não foi possível carregar suas equipes", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderizar(List<Equipe> equipes) {
        listaEquipes.removeAllViews();

        if (equipes.isEmpty()) {
            txtVazio.setText("Você ainda não montou nenhuma equipe");
            txtVazio.setVisibility(View.VISIBLE);
            btnMontarEquipe.setVisibility(View.VISIBLE);
            return;
        }

        txtVazio.setVisibility(View.GONE);
        btnMontarEquipe.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

        for (Equipe equipe : equipes) {
            View card = inflater.inflate(R.layout.item_equipe, listaEquipes, false);

            ((TextView) card.findViewById(R.id.txtNome)).setText(equipe.getPodNome());

            String detalhe = equipe.getPessoas() == 1 ? "1 pessoa" : equipe.getPessoas() + " pessoas";
            if (equipe.isFreelancer()) {
                detalhe = "Freelancer · " + detalhe;
            } else if (!equipe.getTipo().isEmpty()) {
                detalhe = equipe.getTipo() + " · " + detalhe;
            }
            ((TextView) card.findViewById(R.id.txtDetalhe)).setText(detalhe);

            TextView txtData = card.findViewById(R.id.txtData);
            if (equipe.getCriadoEm() > 0) {
                txtData.setText("Pedido em " + formato.format(new Date(equipe.getCriadoEm())));
            } else {
                txtData.setVisibility(View.GONE);
            }

            configurarStatus(card.findViewById(R.id.txtStatus), equipe.getStatus(),
                    equipe.isFreelancer());

            if (equipe.isFreelancer() && "aprovada".equals(equipe.getStatus())) {
                LinearLayout grupo = new LinearLayout(this);
                grupo.setOrientation(LinearLayout.VERTICAL);
                grupo.addView(card);
                Button btnConcluir = new Button(this);
                btnConcluir.setText("Marcar serviço como concluído");
                btnConcluir.setAllCaps(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = dp(12);
                btnConcluir.setLayoutParams(lp);
                btnConcluir.setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Concluir serviço")
                        .setMessage("Confirma que o serviço com " + equipe.getPodNome() + " foi concluído? Depois disso, você poderá avaliar o freelancer.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Confirmar", (dialog, which) -> {
                            btnConcluir.setEnabled(false);
                            AvaliacaoRepository.concluirPedido(equipe.getId(), new Callback<Void>() {
                                @Override public void onSucesso(Void ignorado) {
                                    Toast.makeText(MinhasEquipesActivity.this, "Serviço concluído. Você já pode avaliar o freelancer.", Toast.LENGTH_LONG).show();
                                    carregarEquipes();
                                }
                                @Override public void onErro(Exception e) {
                                    btnConcluir.setEnabled(true);
                                    Toast.makeText(MinhasEquipesActivity.this, "Não foi possível concluir o serviço. Confira as regras do Firebase.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }).show());
                grupo.addView(btnConcluir);
                listaEquipes.addView(grupo);
            } else {
                listaEquipes.addView(card);
            }
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    // Pedido de freelancer: o freelancer "aceita"; pedido de pod: o admin "aprova"
    private void configurarStatus(TextView chip, String status, boolean freelancer) {
        switch (status) {
            case "aprovada":
                chip.setText(freelancer ? "Aceita" : "Aprovada");
                chip.setBackgroundResource(R.drawable.bg_tag_verde);
                chip.setTextColor(ContextCompat.getColor(this, R.color.texto_verde));
                break;
            case "concluida":
                chip.setText("Concluída");
                chip.setBackgroundResource(R.drawable.bg_tag_verde);
                chip.setTextColor(ContextCompat.getColor(this, R.color.texto_verde));
                break;
            case "recusada":
                chip.setText("Recusada");
                chip.setBackgroundResource(R.drawable.bg_tag_cinza);
                chip.setTextColor(ContextCompat.getColor(this, R.color.eq_texto_claro));
                break;
            case "pendente":
            default:
                chip.setText(freelancer ? "Aguardando freelancer" : "Pendente");
                chip.setBackgroundResource(R.drawable.bg_tag_laranja);
                chip.setTextColor(ContextCompat.getColor(this, R.color.laranja_status));
                break;
        }
    }
}