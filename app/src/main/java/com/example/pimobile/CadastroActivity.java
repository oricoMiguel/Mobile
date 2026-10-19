package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CadastroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        InsetsUtil.aplicar(this);

        Spinner spinner = findViewById(R.id.spinnerTipoConta);
        EditText edtNome = findViewById(R.id.edtNomeUsuario);
        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtSenha = findViewById(R.id.edtSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        TextView txtJaTenhoConta = findViewById(R.id.txtJaTenhoConta);

        String[] tipos = {"Freelancer", "Usuário"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, tipos);
        spinner.setAdapter(adapter);

        btnEntrar.setOnClickListener(v -> {
            String nome = edtNome.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(CadastroActivity.this,
                        "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: salvar/enviar os dados do cadastro (tipo, nome, email, senha)

            startActivity(new Intent(CadastroActivity.this, HomeActivity.class));
            finish();
        });

        txtJaTenhoConta.setOnClickListener(v -> {
            startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
            finish();
        });
    }
}