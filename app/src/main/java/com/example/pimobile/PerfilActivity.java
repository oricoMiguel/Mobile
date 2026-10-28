
package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        InsetsUtil.aplicar(this);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        findViewById(R.id.btnEditar).setOnClickListener(v ->
                startActivity(new Intent(this, EditarPerfilActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        carregarOpcoes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPerfil();
    }

    private void carregarPerfil() {
        PerfilRepository.getPerfilAtual(new Callback<Perfil>() {
            @Override
            public void onSucesso(Perfil perfil) {
                if (isFinishing() || isDestroyed()) return;
                preencher(perfil);
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;

                Toast.makeText(
                        PerfilActivity.this,
                        "Não foi possível carregar o perfil",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void preencher(Perfil perfil) {
        ((TextView) findViewById(R.id.txtIniciais))
                .setText(perfil.getIniciais());

        ((TextView) findViewById(R.id.txtNome))
                .setText(perfil.getNome());

        ((TextView) findViewById(R.id.txtTipoConta))
                .setText(perfil.getTipoConta());

        ((TextView) findViewById(R.id.txtEmail))
                .setText(perfil.getEmail());

        findViewById(R.id.badgeVerificado).setVisibility(
                perfil.isVerificado() ? View.VISIBLE : View.GONE
        );

        TextView txtTelefone = findViewById(R.id.txtTelefonePerfil);
        TextView txtEmpresa = findViewById(R.id.txtEmpresaPerfil);
        TextView txtLocalizacao = findViewById(R.id.txtLocalizacaoPerfil);
        boolean ehUsuario = !"Freelancer".equals(perfil.getTipoConta());
        txtTelefone.setVisibility(ehUsuario && !perfil.getTelefone().trim().isEmpty() ? View.VISIBLE : View.GONE);
        txtEmpresa.setVisibility(ehUsuario && !perfil.getEmpresa().trim().isEmpty() ? View.VISIBLE : View.GONE);
        String localizacao = (perfil.getCidade() + (perfil.getCidade().isEmpty() || perfil.getEstado().isEmpty() ? "" : " - ") + perfil.getEstado()).trim();
        txtLocalizacao.setText(localizacao);
        txtLocalizacao.setVisibility(ehUsuario && !localizacao.isEmpty() ? View.VISIBLE : View.GONE);
        txtTelefone.setText("Telefone: " + perfil.getTelefone());
        txtEmpresa.setText("Organização: " + perfil.getEmpresa());
    }

    private void carregarOpcoes() {
        LinearLayout lista = findViewById(R.id.listaOpcoes);
        lista.removeAllViews();

        adicionar(
                lista,
                R.drawable.ic_person,
                "Meu perfil",
                EditarPerfilActivity.class
        );

        adicionar(
                lista,
                R.drawable.ic_pods,
                "Minhas equipes",
                MinhasEquipesActivity.class
        );

        adicionar(
                lista,
                R.drawable.ic_bell,
                "Notificações",
                NotificacoesActivity.class
        );

        adicionar(
                lista,
                R.drawable.ic_doc,
                "Termos e privacidade",
                TermosActivity.class
        );
    }

    private void adicionar(
            LinearLayout lista,
            int iconeRes,
            String titulo,
            Class<?> destino
    ) {
        View item = LayoutInflater.from(this)
                .inflate(R.layout.item_perfil_opcao, lista, false);

        ImageView icone = item.findViewById(R.id.imgIcone);
        icone.setImageResource(iconeRes);

        icone.setColorFilter(
                ContextCompat.getColor(this, R.color.roxo_icone)
        );

        ((TextView) item.findViewById(R.id.txtTitulo))
                .setText(titulo);

        item.setOnClickListener(v -> {
            if (destino == null) {
                emBreve();
            } else {
                startActivity(new Intent(this, destino));
            }
        });

        lista.addView(item);
    }

    private void emBreve() {
        Toast.makeText(
                this,
                "Em breve",
                Toast.LENGTH_SHORT
        ).show();
    }
}
