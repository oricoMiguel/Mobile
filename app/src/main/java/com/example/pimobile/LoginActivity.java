package com.example.pimobile;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InsetsUtil.aplicar(this);

        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtSenha = findViewById(R.id.edtSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        Button btnCriarConta = findViewById(R.id.btnCriarConta);
        TextView txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);

        txtEsqueciSenha.setPaintFlags(txtEsqueciSenha.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnEntrar.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(LoginActivity.this,
                        "Preencha email e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: validar o login (banco de dados ou API)

            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        });

        btnCriarConta.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class)));

        txtEsqueciSenha.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this,
                        "Recuperação de senha em breve", Toast.LENGTH_SHORT).show());
    }
}