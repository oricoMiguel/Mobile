package com.example.pimobile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AreaAdmBaseActivity extends AppCompatActivity {

    private final int[] fundoAvatar = {R.drawable.bg_avatar_roxo, R.drawable.bg_avatar_verde, R.drawable.bg_avatar_laranja};
    private final int[] corAvatar = {R.color.roxo_icone, R.color.texto_verde, R.color.laranja_icone};
    private final int[] fundoBadge = {R.drawable.bg_tag_verde, R.drawable.bg_tag_laranja, R.drawable.bg_tag_cinza};
    private final int[] corBadge = {R.color.texto_verde, R.color.laranja_icone, R.color.eq_texto_claro};

    private List<RegistroAdm> todos = new ArrayList<>();
    private String textoBusca = "";
    private LinearLayout listaRegistros;
    private TextView txtVazio;

    // Cada área informa o que é diferente:
    protected abstract String tituloLista();
    protected abstract String dicaBusca();
    protected abstract void carregarRegistros(Callback<List<RegistroAdm>> callback);
    protected abstract String[] rotulosCards();                        // 4 rótulos
    protected abstract String[] valoresCards(List<RegistroAdm> lista); // 4 valores

    // Quem quiser uma ação ao tocar no registro sobrescreve este método
    protected void aoClicar(RegistroAdm registro) {
        Toast.makeText(this, registro.getNome(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_adm);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        listaRegistros = findViewById(R.id.listaRegistros);
        txtVazio = findViewById(R.id.txtVazio);
        EditText edtBusca = findViewById(R.id.edtBusca);

        ((TextView) findViewById(R.id.txtTituloLista)).setText(tituloLista());
        edtBusca.setHint(dicaBusca());

        findViewById(R.id.btnFiltro).setOnClickListener(v ->
                Toast.makeText(this, "Filtros em breve", Toast.LENGTH_SHORT).show());

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                textoBusca = s.toString().trim().toLowerCase(Locale.getDefault());
                renderizarLista();
            }
        });

        carregar();
    }

    protected void recarregar() {
        carregar();
    }

    private void carregar() {
        carregarRegistros(new Callback<List<RegistroAdm>>() {
            @Override
            public void onSucesso(List<RegistroAdm> lista) {
                if (isFinishing() || isDestroyed()) return;
                todos = lista;
                preencherCards();
                renderizarLista();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                if (AdmRepository.permissaoNegada(e)) {
                    Toast.makeText(AreaAdmBaseActivity.this,
                            "Acesso restrito a administradores", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AreaAdmBaseActivity.this,
                            "Não foi possível carregar os dados", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void preencherCards() {
        int[] idsNum = {R.id.txtNum1, R.id.txtNum2, R.id.txtNum3, R.id.txtNum4};
        int[] idsRotulo = {R.id.txtRotulo1, R.id.txtRotulo2, R.id.txtRotulo3, R.id.txtRotulo4};
        String[] rotulos = rotulosCards();
        String[] valores = valoresCards(todos);

        for (int i = 0; i < 4; i++) {
            ((TextView) findViewById(idsNum[i])).setText(valores[i]);
            ((TextView) findViewById(idsRotulo[i])).setText(rotulos[i]);
        }
    }

    private void renderizarLista() {
        listaRegistros.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int exibidos = 0;

        for (RegistroAdm r : todos) {
            if (!textoBusca.isEmpty()
                    && !r.getNome().toLowerCase(Locale.getDefault()).contains(textoBusca)
                    && !r.getSubtitulo().toLowerCase(Locale.getDefault()).contains(textoBusca)) {
                continue;
            }
            exibidos++;

            View item = inflater.inflate(R.layout.item_registro_adm, listaRegistros, false);

            TextView txtIniciais = item.findViewById(R.id.txtIniciais);
            txtIniciais.setText(r.getIniciais());
            txtIniciais.setBackgroundResource(fundoAvatar[r.getCor()]);
            txtIniciais.setTextColor(ContextCompat.getColor(this, corAvatar[r.getCor()]));

            ((TextView) item.findViewById(R.id.txtNome)).setText(r.getNome());
            ((TextView) item.findViewById(R.id.txtSubtitulo)).setText(r.getSubtitulo());
            ((TextView) item.findViewById(R.id.txtDetalhe)).setText(r.getDetalhe());

            TextView txtBadge = item.findViewById(R.id.txtBadge);
            txtBadge.setText(r.getBadge());
            txtBadge.setBackgroundResource(fundoBadge[r.getBadgeTipo()]);
            txtBadge.setTextColor(ContextCompat.getColor(this, corBadge[r.getBadgeTipo()]));

            item.setOnClickListener(v -> aoClicar(r));

            listaRegistros.addView(item);
        }

        txtVazio.setVisibility(exibidos == 0 ? View.VISIBLE : View.GONE);
    }

    // Conta quantos registros têm determinado badgeTipo
    protected static int contar(List<RegistroAdm> lista, int badgeTipo) {
        int total = 0;
        for (RegistroAdm r : lista) {
            if (r.getBadgeTipo() == badgeTipo) total++;
        }
        return total;
    }
}