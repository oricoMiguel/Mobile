package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RevisaoEquipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revisao_equipe);
        InsetsUtil.aplicar(this);

        Intent dados = getIntent();
        ((TextView) findViewById(R.id.txtTipo))
                .setText("Skill pods · " + dados.getStringExtra("pod_tipo"));
        ((TextView) findViewById(R.id.txtPodNome))
                .setText(dados.getStringExtra("pod_nome"));
        ((TextView) findViewById(R.id.txtPodDescricao))
                .setText(dados.getStringExtra("pod_descricao"));
        ((TextView) findViewById(R.id.txtPessoas))
                .setText(dados.getIntExtra("pod_pessoas", 0) + " pessoas");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            // TODO: salvar a equipe (banco de dados ou API)
            Toast.makeText(this, "Equipe confirmada!", Toast.LENGTH_SHORT).show();
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(home);
        });
    }
}