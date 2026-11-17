
package com.example.pimobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final String TAG = "SERV_EMAIL";

    private static final String PREFS_REENVIO =
            "controle_reenvio_verificacao";

    private static final String CHAVE_ULTIMO_ENVIO =
            "ultimo_envio_";

    private static final long INTERVALO_REENVIO =
            60 * 1000L;

    private static final String TEXTO_REENVIAR =
            "Não recebeu o e-mail? Reenviar verificação";

    private static final String CHAVE_EMAIL_PENDENTE =
            "email_aguardando_confirmacao";

    private static final String URL_CONTINUACAO =
            "https://mobile-132df.firebaseapp.com";

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private Runnable tarefaCooldown;

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
        TextView txtReenviarVerificacao =
                findViewById(R.id.txtReenviarVerificacao);

        txtEsqueciSenha.setPaintFlags(
                txtEsqueciSenha.getPaintFlags()
                        | Paint.UNDERLINE_TEXT_FLAG
        );

        txtReenviarVerificacao.setPaintFlags(
                txtReenviarVerificacao.getPaintFlags()
                        | Paint.UNDERLINE_TEXT_FLAG
        );

        SharedPreferences prefsLogin = getSharedPreferences(
                MainActivity.PREFS_LOGIN,
                MODE_PRIVATE
        );

        String emailPendente = prefsLogin.getString(
                CHAVE_EMAIL_PENDENTE,
                null
        );

        if (emailPendente != null) {
            edtEmail.setText(emailPendente);
        }

        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual != null) {
            atualizarEstadoCooldown(
                    txtReenviarVerificacao,
                    usuarioAtual.getUid()
            );
        }

        // LOGIN
        btnEntrar.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this,
                        "Preencha email e senha",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnEntrar.setEnabled(false);

            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(resultado -> {

                        FirebaseUser usuario = resultado.getUser();

                        if (usuario == null) {
                            btnEntrar.setEnabled(true);

                            Toast.makeText(this,
                                    "Não foi possível identificar sua conta",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        usuario.reload()
                                .addOnSuccessListener(x -> {

                                    FirebaseUser usuarioAtualizado =
                                            auth.getCurrentUser();

                                    if (usuarioAtualizado == null) {
                                        btnEntrar.setEnabled(true);

                                        Toast.makeText(this,
                                                "Não foi possível consultar sua conta",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    if (usuarioAtualizado.isEmailVerified()) {

                                        SharedPreferences prefsPrimeiroAcesso =
                                                getSharedPreferences(
                                                        MainActivity.PREFS_LOGIN,
                                                        MODE_PRIVATE
                                                );

                                        String uidPrimeiroAcesso =
                                                prefsPrimeiroAcesso.getString(
                                                        MainActivity.CHAVE_PRIMEIRO_ACESSO_UID,
                                                        null
                                                );

                                        // PRIMEIRO ACESSO:
                                        // NÃO ENVIA O SEGUNDO E-MAIL
                                        if (usuarioAtualizado.getUid()
                                                .equals(uidPrimeiroAcesso)) {

                                            prefsPrimeiroAcesso.edit()
                                                    .putString(
                                                            MainActivity.CHAVE_UID_CONFIRMADO,
                                                            usuarioAtualizado.getUid()
                                                    )
                                                    .remove(MainActivity.CHAVE_UID_PENDENTE)
                                                    .remove(MainActivity.CHAVE_PRIMEIRO_ACESSO_UID)
                                                    .remove(CHAVE_EMAIL_PENDENTE)
                                                    .apply();

                                            Toast.makeText(this,
                                                    "E-mail verificado! Primeiro acesso liberado.",
                                                    Toast.LENGTH_LONG).show();

                                            Intent home = new Intent(
                                                    LoginActivity.this,
                                                    HomeActivity.class
                                            );

                                            home.setFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            );

                                            startActivity(home);
                                            finish();

                                        } else {

                                            // ACESSOS SEGUINTES:
                                            // EXIGE O SEGUNDO E-MAIL
                                            enviarLinkConfirmacao(
                                                    usuarioAtualizado,
                                                    email,
                                                    btnEntrar,
                                                    null
                                            );
                                        }

                                    } else {

                                        btnEntrar.setEnabled(true);

                                        Toast.makeText(this,
                                                "Seu e-mail ainda não foi verificado. Confira sua caixa de entrada ou solicite um novo link.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {

                                    btnEntrar.setEnabled(true);

                                    Log.e(TAG,
                                            "Erro ao consultar verificação no login",
                                            e);

                                    Toast.makeText(this,
                                            "Não foi possível verificar o status do seu e-mail. Tente novamente.",
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {

                        btnEntrar.setEnabled(true);

                        Toast.makeText(this,
                                AuthErros.traduzir(e),
                                Toast.LENGTH_LONG).show();
                    });
        });

        // REENVIAR VERIFICAÇÃO OU LINK DE CONFIRMAÇÃO
        txtReenviarVerificacao.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this,
                        "Digite seu e-mail e faça login primeiro",
                        Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseUser usuario = auth.getCurrentUser();

            if (usuario == null) {
                Toast.makeText(this,
                        "Entre com seu e-mail e senha antes de solicitar a verificação",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (usuario.getEmail() == null ||
                    !usuario.getEmail().equalsIgnoreCase(email)) {

                Toast.makeText(this,
                        "Entre primeiro com a conta que deseja verificar",
                        Toast.LENGTH_LONG).show();
                return;
            }

            usuario.reload()
                    .addOnSuccessListener(x -> {

                        FirebaseUser usuarioAtualizado =
                                auth.getCurrentUser();

                        if (usuarioAtualizado == null) {

                            txtReenviarVerificacao.setEnabled(true);
                            txtReenviarVerificacao.setText(TEXTO_REENVIAR);

                            Toast.makeText(this,
                                    "Sua sessão não está mais ativa. Faça login novamente.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (usuarioAtualizado.isEmailVerified()) {

                            SharedPreferences prefs = getSharedPreferences(
                                    MainActivity.PREFS_LOGIN,
                                    MODE_PRIVATE
                            );

                            String uidPendente = prefs.getString(
                                    MainActivity.CHAVE_UID_PENDENTE,
                                    null
                            );

                            if (usuarioAtualizado.getUid().equals(uidPendente)) {

                                enviarLinkConfirmacao(
                                        usuarioAtualizado,
                                        email,
                                        null,
                                        txtReenviarVerificacao
                                );

                            } else {

                                txtReenviarVerificacao.setEnabled(true);
                                txtReenviarVerificacao.setText(TEXTO_REENVIAR);

                                Toast.makeText(this,
                                        "Seu e-mail já está verificado!",
                                        Toast.LENGTH_LONG).show();
                            }

                        } else {

                            reenviarVerificacaoCadastro(
                                    usuarioAtualizado,
                                    txtReenviarVerificacao
                            );
                        }
                    })
                    .addOnFailureListener(e -> {

                        txtReenviarVerificacao.setEnabled(true);
                        txtReenviarVerificacao.setText(TEXTO_REENVIAR);

                        Log.e(TAG,
                                "Erro ao atualizar conta antes do reenvio",
                                e);

                        Toast.makeText(this,
                                "Não foi possível consultar sua conta. Verifique sua conexão e tente novamente.",
                                Toast.LENGTH_LONG).show();
                    });
        });

        // CRIAR CONTA
        btnCriarConta.setOnClickListener(v ->
                startActivity(new Intent(
                        LoginActivity.this,
                        CadastroActivity.class
                ))
        );

        // RECUPERAR SENHA
        txtEsqueciSenha.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this,
                        "Digite seu email para recuperar a senha",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(x ->
                            Toast.makeText(this,
                                    "Se o email existir, enviamos um link de recuperação",
                                    Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    AuthErros.traduzir(e),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        processarLinkRecebido(getIntent());
    }

    // ENVIA O SEGUNDO LINK DE CONFIRMAÇÃO
    private void enviarLinkConfirmacao(
            FirebaseUser usuario,
            String email,
            Button btnEntrar,
            TextView textoReenvio) {

        if (btnEntrar != null) {
            btnEntrar.setEnabled(false);
        }

        if (textoReenvio != null) {
            textoReenvio.setEnabled(false);
            textoReenvio.setText("Enviando link...");
        }

        SharedPreferences prefs = getSharedPreferences(
                MainActivity.PREFS_LOGIN,
                MODE_PRIVATE
        );

        prefs.edit()
                .putString(
                        MainActivity.CHAVE_UID_PENDENTE,
                        usuario.getUid()
                )
                .putString(
                        CHAVE_EMAIL_PENDENTE,
                        email
                )
                .remove(MainActivity.CHAVE_UID_CONFIRMADO)
                .apply();

        ActionCodeSettings configuracoes =
                ActionCodeSettings.newBuilder()
                        .setUrl(URL_CONTINUACAO)
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                "com.example.pimobile",
                                false,
                                null
                        )
                        .build();

        auth.sendSignInLinkToEmail(email, configuracoes)
                .addOnSuccessListener(x -> {

                    if (btnEntrar != null) {
                        btnEntrar.setEnabled(true);
                    }

                    if (textoReenvio != null) {
                        textoReenvio.setEnabled(true);
                        textoReenvio.setText(TEXTO_REENVIAR);
                    }

                    Toast.makeText(this,
                            "Enviamos um novo link para seu e-mail. Abra-o neste mesmo aparelho para liberar o acesso.",
                            Toast.LENGTH_LONG).show();

                })
                .addOnFailureListener(e -> {

                    if (btnEntrar != null) {
                        btnEntrar.setEnabled(true);
                    }

                    if (textoReenvio != null) {
                        textoReenvio.setEnabled(true);
                        textoReenvio.setText(TEXTO_REENVIAR);
                    }

                    Log.e(TAG,
                            "Erro ao enviar link de confirmação",
                            e);

                    Toast.makeText(this,
                            AuthErros.traduzir(e),
                            Toast.LENGTH_LONG).show();
                });
    }

    // PROCESSA O LINK RECEBIDO PELO APLICATIVO
    private void processarLinkRecebido(Intent intent) {

        if (intent == null || intent.getData() == null) {
            return;
        }

        String link = intent.getData().toString();

        if (!auth.isSignInWithEmailLink(link)) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(
                MainActivity.PREFS_LOGIN,
                MODE_PRIVATE
        );

        String email = prefs.getString(
                CHAVE_EMAIL_PENDENTE,
                null
        );

        String uidPendente = prefs.getString(
                MainActivity.CHAVE_UID_PENDENTE,
                null
        );

        FirebaseUser usuario = auth.getCurrentUser();

        if (email == null ||
                uidPendente == null ||
                usuario == null ||
                !uidPendente.equals(usuario.getUid()) ||
                usuario.getEmail() == null ||
                !usuario.getEmail().equalsIgnoreCase(email)) {

            Toast.makeText(this,
                    "Não foi possível confirmar. Abra o link no mesmo aparelho e na mesma conta em que iniciou o login.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential credencial =
                EmailAuthProvider.getCredentialWithLink(email, link);

        usuario.reauthenticate(credencial)
                .addOnSuccessListener(x -> {

                    FirebaseUser usuarioAtualizado =
                            auth.getCurrentUser();

                    if (usuarioAtualizado == null ||
                            !uidPendente.equals(usuarioAtualizado.getUid())) {

                        Toast.makeText(this,
                                "Não foi possível identificar sua conta após a confirmação.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    prefs.edit()
                            .putString(
                                    MainActivity.CHAVE_UID_CONFIRMADO,
                                    usuarioAtualizado.getUid()
                            )
                            .remove(MainActivity.CHAVE_UID_PENDENTE)
                            .remove(CHAVE_EMAIL_PENDENTE)
                            .apply();

                    Toast.makeText(this,
                            "E-mail confirmado! Acesso liberado.",
                            Toast.LENGTH_LONG).show();

                    Intent home = new Intent(
                            LoginActivity.this,
                            HomeActivity.class
                    );

                    home.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(home);
                    finish();
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG,
                            "Erro ao reautenticar com link de e-mail",
                            e);

                    Toast.makeText(this,
                            "Não foi possível confirmar. O link pode ter expirado ou ser inválido. Solicite um novo.",
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processarLinkRecebido(intent);
    }

    // REENVIA A VERIFICAÇÃO INICIAL DO CADASTRO
    private void reenviarVerificacaoCadastro(
            FirebaseUser usuario,
            TextView texto) {

        long restante = obterTempoRestante(usuario.getUid());

        if (restante > 0) {

            atualizarEstadoCooldown(
                    texto,
                    usuario.getUid()
            );

            Toast.makeText(this,
                    "Aguarde o intervalo antes de solicitar outro e-mail.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        texto.setEnabled(false);
        texto.setText("Enviando...");

        usuario.sendEmailVerification()
                .addOnSuccessListener(y -> {

                    iniciarCooldown(
                            texto,
                            usuario.getUid()
                    );

                    Toast.makeText(this,
                            "Solicitação de envio concluída! Confira sua caixa de entrada e o spam.",
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG,
                            "Erro ao reenviar verificação",
                            e);

                    iniciarCooldown(
                            texto,
                            usuario.getUid()
                    );

                    Toast.makeText(this,
                            traduzirErroReenvio(e),
                            Toast.LENGTH_LONG).show();
                });
    }

    // REGISTRA O MOMENTO DA SOLICITAÇÃO
    private void iniciarCooldown(TextView texto, String uid) {

        SharedPreferences prefs = getSharedPreferences(
                PREFS_REENVIO,
                MODE_PRIVATE
        );

        prefs.edit()
                .putLong(
                        CHAVE_ULTIMO_ENVIO + uid,
                        System.currentTimeMillis()
                )
                .apply();

        atualizarEstadoCooldown(texto, uid);
    }

    // CALCULA QUANTO TEMPO FALTA
    private long obterTempoRestante(String uid) {

        SharedPreferences prefs = getSharedPreferences(
                PREFS_REENVIO,
                MODE_PRIVATE
        );

        long ultimoEnvio = prefs.getLong(
                CHAVE_ULTIMO_ENVIO + uid,
                0L
        );

        if (ultimoEnvio == 0L) {
            return 0L;
        }

        long restante = INTERVALO_REENVIO -
                (System.currentTimeMillis() - ultimoEnvio);

        return Math.max(restante, 0L);
    }

    // ATUALIZA O TEXTO E O ESTADO DO BOTÃO
    private void atualizarEstadoCooldown(TextView texto, String uid) {

        if (tarefaCooldown != null) {
            handler.removeCallbacks(tarefaCooldown);
        }

        long restante = obterTempoRestante(uid);

        if (restante <= 0) {

            texto.setEnabled(true);
            texto.setText(TEXTO_REENVIAR);
            tarefaCooldown = null;
            return;
        }

        texto.setEnabled(false);

        long segundos = (restante + 999) / 1000;

        texto.setText("Aguarde " + segundos + "s para reenviar");

        tarefaCooldown = () ->
                atualizarEstadoCooldown(texto, uid);

        handler.postDelayed(tarefaCooldown, 1000);
    }

    // TRADUZ OS ERROS DO FIREBASE
    private String traduzirErroReenvio(Exception e) {

        if (e instanceof FirebaseTooManyRequestsException) {
            return "O Firebase bloqueou temporariamente novos envios. Aguarde alguns minutos antes de tentar novamente.";
        }

        if (e instanceof FirebaseAuthException) {

            String codigo = ((FirebaseAuthException) e)
                    .getErrorCode()
                    .toLowerCase(Locale.ROOT)
                    .replace('_', '-');

            if (codigo.contains("too-many-requests")) {
                return "Muitas tentativas de envio. Aguarde alguns minutos antes de tentar novamente.";
            }

            if (codigo.contains("network")) {
                return "Falha de conexão. Verifique sua internet e tente novamente.";
            }

            if (codigo.contains("user-token-expired") ||
                    codigo.contains("invalid-user-token") ||
                    codigo.contains("user-not-found") ||
                    codigo.contains("user-disabled")) {

                return "Sua sessão expirou ou não está mais válida. Faça login novamente.";
            }

            if (codigo.contains("operation-not-allowed")) {
                return "O envio de e-mails de verificação não está habilitado nas configurações do Firebase.";
            }
        }

        return "O Firebase não conseguiu processar o envio agora. Aguarde um pouco e tente novamente.";
    }

    @Override
    protected void onDestroy() {

        if (tarefaCooldown != null) {
            handler.removeCallbacks(tarefaCooldown);
        }

        super.onDestroy();
    }
}
