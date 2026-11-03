package com.example.pimobile;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChatbotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        TextView btnVoltar = findViewById(R.id.btnVoltarChat);

        btnVoltar.setOnClickListener(v -> finish());
    }
}
