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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import android.view.View;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_spinner_cadastro,
                tipos
        ) {
            @Override
            public View getDropDownView(int position, View convertView,
                                        android.view.ViewGroup parent) {

                View view = super.getDropDownView(position, convertView, parent);

                TextView texto = (TextView) view;
                texto.setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                                CadastroActivity.this,
                                R.color.eq_escuro
                        )
                );

                return view;
            }
        };

        adapter.setDropDownViewResource(
                R.layout.item_spinner_cadastro_dropdown
        );

        spinner.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnEntrar.setOnClickListener(v -> {
            String nome = edtNome.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();
            String tipo = spinner.getSelectedItem().toString();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (senha.length() < 6) {
                Toast.makeText(this, "A senha precisa ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            btnEntrar.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(resultado -> {
                        String uid = resultado.getUser().getUid();

                        Map<String, Object> usuario = new HashMap<>();
                        usuario.put("nome", nome);
                        usuario.put("email", email);
                        usuario.put("tipoConta", tipo);
                        usuario.put("papel", "usuario");
                        usuario.put("criadoEm", FieldValue.serverTimestamp());

                        db.collection("usuarios").document(uid).set(usuario)
                                .addOnSuccessListener(x -> {
                                    startActivity(new Intent(this, HomeActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnEntrar.setEnabled(true);
                                    Toast.makeText(this, "Conta criada, mas falhou ao salvar o perfil", Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        btnEntrar.setEnabled(true);
                        Toast.makeText(this, AuthErros.traduzir(e), Toast.LENGTH_LONG).show();
                    });
        });

        txtJaTenhoConta.setOnClickListener(v -> {
            startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
            finish();
        });
    }
}