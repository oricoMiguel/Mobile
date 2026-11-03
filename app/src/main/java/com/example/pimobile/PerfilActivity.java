
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilActivity extends AppCompatActivity {

    private String usuarioIdVisualizado;
    private boolean visualizandoOutroPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        InsetsUtil.aplicar(this);

        usuarioIdVisualizado = getIntent().getStringExtra("usuarioId");

        visualizandoOutroPerfil = usuarioIdVisualizado != null
                && !usuarioIdVisualizado.trim().isEmpty();

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            if (visualizandoOutroPerfil) return;

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        if (visualizandoOutroPerfil) {
            findViewById(R.id.btnLogout).setVisibility(View.GONE);
            findViewById(R.id.listaOpcoes).setVisibility(View.GONE);
        } else {
            carregarOpcoes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPerfil();
    }

    // =====================================================
    // CARREGAMENTO DO PERFIL
    // =====================================================

    private void carregarPerfil() {

        Callback<Perfil> callback = new Callback<Perfil>() {

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

                if (visualizandoOutroPerfil) {
                    finish();
                }
            }
        };

        if (visualizandoOutroPerfil) {
            PerfilRepository.getPerfilPorId(usuarioIdVisualizado, callback);
        } else {
            PerfilRepository.getPerfilAtual(callback);
        }
    }

    // =====================================================
    // PREENCHIMENTO DAS INFORMAÇÕES
    // =====================================================

    private void preencher(Perfil perfil) {

        if (perfil == null) return;

        TextView txtIniciais = findViewById(R.id.txtIniciais);
        ImageView imgFotoPerfil = findViewById(R.id.imgFotoPerfil);

        String iniciais = textoSeguro(perfil.getIniciais());
        String fotoUsuario = textoSeguro(perfil.getFotoUrl());

        txtIniciais.setText(iniciais);

        if (!fotoUsuario.isEmpty()) {

            Glide.with(this)
                    .load(fotoUsuario)
                    .circleCrop()
                    .into(imgFotoPerfil);

            imgFotoPerfil.setVisibility(View.VISIBLE);
            txtIniciais.setVisibility(View.GONE);

        } else {

            Glide.with(this).clear(imgFotoPerfil);

            imgFotoPerfil.setVisibility(View.GONE);
            txtIniciais.setVisibility(View.VISIBLE);
        }

        // Compatibilidade com fotos antigas de freelancers.
        if (!visualizandoOutroPerfil
                && "Freelancer".equals(perfil.getTipoConta())) {

            FreelancerRepository.getMeuPerfil(
                    new Callback<PerfilProfissional>() {

                        @Override
                        public void onSucesso(PerfilProfissional profissional) {

                            if (isFinishing() || isDestroyed()) return;

                            if (profissional != null
                                    && profissional.getFotoUrl() != null
                                    && !profissional.getFotoUrl().trim().isEmpty()) {

                                Glide.with(PerfilActivity.this)
                                        .load(profissional.getFotoUrl())
                                        .circleCrop()
                                        .into(imgFotoPerfil);

                                imgFotoPerfil.setVisibility(View.VISIBLE);
                                txtIniciais.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onErro(Exception e) {
                            // Mantém a foto atual ou as iniciais.
                        }
                    }
            );
        }

        // Nome.
        String nome = textoSeguro(perfil.getNome());

        ((TextView) findViewById(R.id.txtNome))
                .setText(nome);

        // Tipo de conta.
        String tipoConta = textoSeguro(perfil.getTipoConta());

        ((TextView) findViewById(R.id.txtTipoConta))
                .setText(tipoConta);

        // E-mail.
        TextView txtEmail = findViewById(R.id.txtEmail);

        String email = textoSeguro(perfil.getEmail());

        txtEmail.setText(email);

        txtEmail.setVisibility(
                email.isEmpty() ? View.GONE : View.VISIBLE
        );

        // Selo de verificação.
        findViewById(R.id.badgeVerificado).setVisibility(
                perfil.isVerificado() ? View.VISIBLE : View.GONE
        );

        // =================================================
        // INFORMAÇÕES COMPLEMENTARES OCULTAS NESTA TELA
        // =================================================

        findViewById(R.id.txtTelefonePerfil).setVisibility(View.GONE);
        findViewById(R.id.txtEmpresaPerfil).setVisibility(View.GONE);
        findViewById(R.id.txtLocalizacaoPerfil).setVisibility(View.GONE);
        findViewById(R.id.tituloInformacoes).setVisibility(View.GONE);
    }

    private String textoSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // =====================================================
    // OPÇÕES DO PRÓPRIO PERFIL
    // =====================================================

    private void carregarOpcoes() {

        LinearLayout lista = findViewById(R.id.listaOpcoes);

        lista.removeAllViews();

        if (visualizandoOutroPerfil) {
            lista.setVisibility(View.GONE);
            return;
        }

        lista.setVisibility(View.VISIBLE);

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

    // =====================================================
    // CRIAÇÃO DAS OPÇÕES
    // =====================================================

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

    // =====================================================
    // AVISO DE FUNCIONALIDADE FUTURA
    // =====================================================

    private void emBreve() {

        Toast.makeText(
                this,
                "Em breve",
                Toast.LENGTH_SHORT
        ).show();
    }
}
