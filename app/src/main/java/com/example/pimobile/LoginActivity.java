package com.example.pimobile;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
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
                Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            btnEntrar.setEnabled(false);
            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(r -> {
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnEntrar.setEnabled(true);
                        Toast.makeText(this, AuthErros.traduzir(e), Toast.LENGTH_LONG).show();
                    });
        });


        btnCriarConta.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class)));

        txtEsqueciSenha.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Digite seu email para recuperar a senha", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(x -> Toast.makeText(this,
                            "Se o email existir, enviamos um link de recuperação", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this,
                            AuthErros.traduzir(e), Toast.LENGTH_LONG).show());
        });
    }
}