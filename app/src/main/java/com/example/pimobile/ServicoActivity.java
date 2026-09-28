package com.example.pimobile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ServicoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servico);

        ImageView btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v ->
                Toast.makeText(ServicoActivity.this, "Menu", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardFreelancer1).setOnClickListener(v -> {
            // TODO: abrir o perfil do freelancer 1
        });
        findViewById(R.id.cardFreelancer2).setOnClickListener(v -> {
            // TODO: abrir o perfil do freelancer 2
        });
        findViewById(R.id.cardFreelancer3).setOnClickListener(v -> {
            // TODO: abrir o perfil do freelancer 3
        });
    }
}