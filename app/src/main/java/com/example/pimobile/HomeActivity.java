package com.example.pimobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView btnMenu = findViewById(R.id.btnMenu);
        Button btnMonteEquipe = findViewById(R.id.btnMonteEquipe);
        Button btnVerSkillPods = findViewById(R.id.btnVerSkillPods);

        btnMenu.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Menu", Toast.LENGTH_SHORT).show());

        btnMonteEquipe.setOnClickListener(v -> {
            // TODO: abrir a tela de montar equipe
        });

        btnVerSkillPods.setOnClickListener(v -> {
            // TODO: abrir a tela de skill pods
        });
    }
}