
package com.example.pimobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_LOGIN = "controle_login_email";

    public static final String CHAVE_UID_PENDENTE =
            "uid_aguardando_confirmacao";

    public static final String CHAVE_UID_CONFIRMADO =
            "uid_confirmado_email";

    // IDENTIFICA O PRIMEIRO ACESSO APÓS O CADASTRO
    public static final String CHAVE_PRIMEIRO_ACESSO_UID =
            "uid_primeiro_acesso";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getSharedPreferences(
                PREFS_LOGIN,
                MODE_PRIVATE
        );

        String uidConfirmado = prefs.getString(
                CHAVE_UID_CONFIRMADO,
                null
        );

        String uidPendente = prefs.getString(
                CHAVE_UID_PENDENTE,
                null
        );

        boolean confirmado = usuario != null
                && usuario.getUid().equals(uidConfirmado);

        boolean aguardandoConfirmacao = usuario != null
                && usuario.getUid().equals(uidPendente);

        Class<?> destino;

        if (usuario != null
                && usuario.isEmailVerified()
                && confirmado
                && !aguardandoConfirmacao) {

            destino = HomeActivity.class;

        } else {

            destino = LoginActivity.class;
        }

        if (usuario == null) {
            prefs.edit()
                    .remove(CHAVE_UID_PENDENTE)
                    .remove(CHAVE_UID_CONFIRMADO)
                    .apply();
        }

        startActivity(new Intent(this, destino));
        finish();
    }
}
