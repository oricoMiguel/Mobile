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
        findViewById(R.id.btnEditar).setOnClickListener(v -> emBreve());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Mostra a lista de opções já, e depois atualiza o contador quando o perfil chegar
        carregarOpcoes(0);

        PerfilRepository.getPerfilAtual(new Callback<Perfil>() {
            @Override
            public void onSucesso(Perfil perfil) {
                if (isFinishing() || isDestroyed()) return;
                preencher(perfil);
                carregarOpcoes(perfil.getMensagensNaoLidas());
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(PerfilActivity.this,
                        "Não foi possível carregar o perfil", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void preencher(Perfil perfil) {
        ((TextView) findViewById(R.id.txtIniciais)).setText(perfil.getIniciais());
        ((TextView) findViewById(R.id.txtNome)).setText(perfil.getNome());
        ((TextView) findViewById(R.id.txtTipoConta)).setText(perfil.getTipoConta());
        ((TextView) findViewById(R.id.txtEmail)).setText(perfil.getEmail());
        findViewById(R.id.badgeVerificado).setVisibility(
                perfil.isVerificado() ? View.VISIBLE : View.GONE);
    }

    private void carregarOpcoes(int mensagensNaoLidas) {
        LinearLayout lista = findViewById(R.id.listaOpcoes);
        lista.removeAllViews();

        adicionar(lista, R.drawable.ic_person, "Meu perfil", 0, null);
        adicionar(lista, R.drawable.ic_briefcase, "Meus projetos", 0, null);
        adicionar(lista, R.drawable.ic_pods, "Minhas equipes", 0, MinhasEquipesActivity.class);
        adicionar(lista, R.drawable.ic_mail, "Mensagens", mensagensNaoLidas, null);
        adicionar(lista, R.drawable.ic_bell, "Notificações", 0, NotificacoesActivity.class);
        adicionar(lista, R.drawable.ic_settings, "Configurações", 0, null);
        adicionar(lista, R.drawable.ic_doc, "Termos e privacidade", 0, null);
    }

    private void adicionar(LinearLayout lista, int iconeRes, String titulo, int contador,
                           Class<?> destino) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_perfil_opcao, lista, false);

        ImageView icone = item.findViewById(R.id.imgIcone);
        icone.setImageResource(iconeRes);
        icone.setColorFilter(ContextCompat.getColor(this, R.color.roxo_icone));

        ((TextView) item.findViewById(R.id.txtTitulo)).setText(titulo);

        TextView txtContador = item.findViewById(R.id.txtContador);
        if (contador > 0) {
            txtContador.setText(String.valueOf(contador));
            txtContador.setVisibility(View.VISIBLE);
        }

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
        Toast.makeText(this, "Em breve", Toast.LENGTH_SHORT).show();
    }
}