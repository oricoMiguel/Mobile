package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Abre direto a tela de cadastro
        startActivity(new Intent(MainActivity.this, CadastroActivity.class));
        finish();
    }
}