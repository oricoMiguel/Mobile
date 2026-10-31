package com.example.pimobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.widget.RatingBar;
import androidx.appcompat.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PerfilFreelancerActivity extends AppCompatActivity {

    private final int[] fundoAvatar = {R.drawable.bg_avatar_roxo, R.drawable.bg_avatar_verde, R.drawable.bg_avatar_laranja};
    private final int[] fundoTag = {R.drawable.bg_tag_roxa, R.drawable.bg_tag_verde, R.drawable.bg_tag_laranja};
    private final int[] corTexto = {R.color.roxo_icone, R.color.texto_verde, R.color.laranja_icone};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_freelancer);
        InsetsUtil.aplicar(this);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        String id = getIntent().getStringExtra("freelancer_id");
        if (id == null) {
            Toast.makeText(this, "Perfil inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FreelancerRepository.getPorId(id, new Callback<Freelancer>() {
            @Override
            public void onSucesso(Freelancer freelancer) {
                if (isFinishing() || isDestroyed()) return;
                java.util.List<Freelancer> um = new java.util.ArrayList<>();
                um.add(freelancer);
                AvaliacaoRepository.aplicarMedias(um, new Callback<Void>() {
                    @Override public void onSucesso(Void ignorado) {
                        if (!isFinishing() && !isDestroyed()) preencher(freelancer);
                    }
                    @Override public void onErro(Exception e) {
                        if (!isFinishing() && !isDestroyed()) {
                            freelancer.setNota(0.0);
                            preencher(freelancer);
                        }
                    }
                });
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                ((TextView) findViewById(R.id.txtCarregando))
                        .setText("Não foi possível carregar este perfil");
                Toast.makeText(PerfilFreelancerActivity.this,
                        "Não foi possível carregar o perfil", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void preencher(Freelancer f) {
        int cor = f.getCor();

        TextView txtIniciais = findViewById(R.id.txtIniciais);
        txtIniciais.setText(f.getIniciais());
        txtIniciais.setBackgroundResource(fundoAvatar[cor]);
        txtIniciais.setTextColor(ContextCompat.getColor(this, corTexto[cor]));
        ImageView imgFoto = findViewById(R.id.imgFotoPerfil);
        if (f.getFotoUrl() != null && !f.getFotoUrl().trim().isEmpty()) {
            Glide.with(this).load(f.getFotoUrl()).circleCrop().into(imgFoto);
            imgFoto.setVisibility(View.VISIBLE);
            txtIniciais.setVisibility(View.GONE);
        } else {
            imgFoto.setVisibility(View.GONE);
            txtIniciais.setVisibility(View.VISIBLE);
        }

        ((TextView) findViewById(R.id.txtNome)).setText(f.getNome());
        ((TextView) findViewById(R.id.txtArea)).setText(f.getArea());
        ((TextView) findViewById(R.id.txtNota)).setText(f.getNota() > 0
                ? String.format(Locale.US, "%.1f", f.getNota())
                : (f.isNovo() ? "Novo" : "Sem avaliações"));
        ((TextView) findViewById(R.id.txtCategoria)).setText("· " + f.getCategoria());

        findViewById(R.id.dotStatus).setBackgroundResource(f.isDisponivel()
                ? R.drawable.bg_dot_verde : R.drawable.bg_dot_laranja);
        TextView txtDisponibilidade = findViewById(R.id.txtDisponibilidade);
        txtDisponibilidade.setText(f.getDisponibilidade());
        txtDisponibilidade.setTextColor(ContextCompat.getColor(this,
                f.isDisponivel() ? R.color.texto_verde : R.color.laranja_status));

        LinearLayout listaHabilidades = findViewById(R.id.listaHabilidades);
        listaHabilidades.removeAllViews();
        for (String habilidade : f.getHabilidades()) {
            TextView tag = new TextView(this);
            tag.setText(habilidade);
            tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tag.setTextColor(ContextCompat.getColor(this, corTexto[cor]));
            tag.setBackgroundResource(fundoTag[cor]);
            tag.setPadding(dp(12), dp(6), dp(12), dp(6));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            tag.setLayoutParams(lp);
            listaHabilidades.addView(tag);
        }

        configurarContatos(f);

        TextView txtMembroDesde = findViewById(R.id.txtMembroDesde);
        if (f.getCriadoEm() > 0) {
            SimpleDateFormat formato = new SimpleDateFormat("MMMM 'de' yyyy", new Locale("pt", "BR"));
            txtMembroDesde.setText("Membro desde " + formato.format(new Date(f.getCriadoEm())));
        } else {
            txtMembroDesde.setVisibility(View.GONE);
        }

        configurarContratar(f);
        configurarAvaliacao(f);

        findViewById(R.id.txtCarregando).setVisibility(View.GONE);
        findViewById(R.id.conteudoPerfil).setVisibility(View.VISIBLE);
    }



    private void configurarAvaliacao(Freelancer f) {
        View btnAvaliar = findViewById(R.id.btnAvaliar);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getUid().equals(f.getId())) {
            btnAvaliar.setVisibility(View.GONE);
            return;
        }
        btnAvaliar.setVisibility(View.GONE);
        AvaliacaoRepository.getPedidoConcluido(f.getId(), new Callback<String>() {
            @Override public void onSucesso(String equipeId) {
                if (isFinishing() || isDestroyed()) return;
                if (equipeId == null) {
                    btnAvaliar.setVisibility(View.GONE);
                    return;
                }
                btnAvaliar.setVisibility(View.VISIBLE);
                AvaliacaoRepository.getMinhaNota(f.getId(), new Callback<Integer>() {
                    @Override public void onSucesso(Integer nota) {
                        if (!isFinishing() && !isDestroyed()) {
                            ((TextView) btnAvaliar).setText(nota != null && nota > 0
                                    ? "Alterar minha avaliação" : "Avaliar freelancer");
                        }
                    }
                    @Override public void onErro(Exception e) { }
                });
                btnAvaliar.setOnClickListener(v -> abrirDialogoAvaliacao(f, equipeId));
            }
            @Override public void onErro(Exception e) {
                if (!isFinishing() && !isDestroyed()) btnAvaliar.setVisibility(View.GONE);
            }
        });
    }

    private void abrirDialogoAvaliacao(Freelancer f, String equipeId) {
        LinearLayout caixa = new LinearLayout(this);
        caixa.setOrientation(LinearLayout.VERTICAL);
        caixa.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        caixa.setPadding(dp(24), dp(22), dp(24), dp(12));

        TextView icone = new TextView(this);
        icone.setText("★");
        icone.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        icone.setTextColor(ContextCompat.getColor(this, R.color.roxo_icone));
        icone.setGravity(android.view.Gravity.CENTER);
        android.graphics.drawable.GradientDrawable circulo = new android.graphics.drawable.GradientDrawable();
        circulo.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        circulo.setColor(ContextCompat.getColor(this, R.color.roxo_icone));
        icone.setTextColor(android.graphics.Color.WHITE);
        icone.setBackground(circulo);
        LinearLayout.LayoutParams iconeParams = new LinearLayout.LayoutParams(dp(60), dp(60));
        iconeParams.bottomMargin = dp(14);
        caixa.addView(icone, iconeParams);

        TextView titulo = new TextView(this);
        titulo.setText("Sua opinião importa!");
        titulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setTextColor(ContextCompat.getColor(this, R.color.eq_escuro));
        titulo.setGravity(android.view.Gravity.CENTER);
        caixa.addView(titulo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView instrucao = new TextView(this);
        instrucao.setText("Como foi sua experiência com " + f.getNome() + "?");
        instrucao.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        instrucao.setTextColor(ContextCompat.getColor(this, R.color.eq_texto_claro));
        instrucao.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams instrucaoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        instrucaoParams.topMargin = dp(8);
        caixa.addView(instrucao, instrucaoParams);

        RatingBar estrelas = new RatingBar(this);
        estrelas.setNumStars(5);
        estrelas.setStepSize(1f);
        estrelas.setRating(5f);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            estrelas.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.roxo_icone)));
            estrelas.setSecondaryProgressTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.roxo_icone)));
        }
        LinearLayout.LayoutParams estrelasParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        estrelasParams.topMargin = dp(20);
        caixa.addView(estrelas, estrelasParams);

        TextView notaTexto = new TextView(this);
        notaTexto.setText("Excelente");
        notaTexto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        notaTexto.setTypeface(null, android.graphics.Typeface.BOLD);
        notaTexto.setTextColor(ContextCompat.getColor(this, R.color.roxo_icone));
        LinearLayout.LayoutParams notaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        notaParams.topMargin = dp(6);
        caixa.addView(notaTexto, notaParams);
        estrelas.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            int nota = Math.round(rating);
            String[] descricoes = {"Selecione uma nota", "Muito ruim", "Ruim", "Regular", "Muito bom", "Excelente"};
            notaTexto.setText(descricoes[Math.max(0, Math.min(5, nota))]);
        });

        TextView rodape = new TextView(this);
        rodape.setText("Sua avaliação ajuda outras pessoas a escolherem um profissional.");
        rodape.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rodape.setTextColor(ContextCompat.getColor(this, R.color.eq_texto_claro));
        rodape.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams rodapeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rodapeParams.topMargin = dp(18);
        caixa.addView(rodape, rodapeParams);

        AvaliacaoRepository.getMinhaNota(f.getId(), new Callback<Integer>() {
            @Override public void onSucesso(Integer nota) {
                if (nota != null && nota > 0) {
                    estrelas.setRating(nota);
                    String[] descricoes = {"Selecione uma nota", "Muito ruim", "Ruim", "Regular", "Muito bom", "Excelente"};
                    notaTexto.setText(descricoes[Math.max(0, Math.min(5, nota))]);
                }
            }
            @Override public void onErro(Exception e) { }
        });

        AlertDialog dialogAvaliacao = new AlertDialog.Builder(this)
                .setView(caixa)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar avaliação", (dialog, which) -> {
                    int nota = Math.round(estrelas.getRating());
                    if (nota < 1) {
                        Toast.makeText(this, "Escolha pelo menos uma estrela", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AvaliacaoRepository.salvar(f.getId(), f.getNome(), equipeId, nota,
                            new Callback<Void>() {
                                @Override public void onSucesso(Void ignorado) {
                                    Toast.makeText(PerfilFreelancerActivity.this,
                                            "Avaliação salva!", Toast.LENGTH_SHORT).show();
                                    recreate();
                                }
                                @Override public void onErro(Exception e) {
                                    Toast.makeText(PerfilFreelancerActivity.this,
                                            "Não foi possível salvar. Confira as regras do Firebase.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }).create();

        // Fundo branco com borda roxa, seguindo a identidade visual do SERV+.
        android.graphics.drawable.GradientDrawable fundoDialogo =
                new android.graphics.drawable.GradientDrawable();
        fundoDialogo.setColor(android.graphics.Color.WHITE);
        fundoDialogo.setCornerRadius(dp(20));
        fundoDialogo.setStroke(dp(2), ContextCompat.getColor(this, R.color.roxo_icone));
        dialogAvaliacao.show();
        if (dialogAvaliacao.getWindow() != null) {
            dialogAvaliacao.getWindow().setBackgroundDrawable(fundoDialogo);
        }
    }

    private void configurarContatos(Freelancer f) {
        LinearLayout secao = findViewById(R.id.secaoContato);
        LinearLayout lista = findViewById(R.id.listaContatos);
        lista.removeAllViews();
        boolean temContato = false;

        String whatsapp = textoSeguro(f.getWhatsapp());
        if (!whatsapp.isEmpty()) {
            temContato = true;
            adicionarContato(lista, "WhatsApp  ·  " + whatsapp, () -> abrirWhatsApp(whatsapp));
        }

        String instagram = textoSeguro(f.getInstagram());
        if (!instagram.isEmpty()) {
            temContato = true;
            adicionarContato(lista, "Instagram  ·  " + instagram, () -> abrirInstagram(instagram));
        }

        String telefone = textoSeguro(f.getTelefone());
        if (!telefone.isEmpty()) {
            temContato = true;
            adicionarContato(lista, "Telefone  ·  " + telefone, () -> abrirTelefone(telefone));
        }

        String email = textoSeguro(f.getEmailContato());
        if (!email.isEmpty()) {
            temContato = true;
            adicionarContato(lista, "E-mail  ·  " + email, () -> abrirEmail(email));
        }

        secao.setVisibility(temContato ? View.VISIBLE : View.GONE);
    }

    private void adicionarContato(LinearLayout lista, String texto, Runnable acao) {
        TextView item = new TextView(this);
        item.setText(texto);
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        item.setTextColor(ContextCompat.getColor(this, R.color.eq_escuro));
        item.setBackgroundResource(R.drawable.bg_campo_login);
        item.setPadding(dp(14), dp(14), dp(14), dp(14));
        item.setClickable(true);
        item.setFocusable(true);
        item.setOnClickListener(v -> acao.run());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        item.setLayoutParams(lp);
        lista.addView(item);
    }

    private void abrirWhatsApp(String numeroInformado) {
        String numero = numeroInformado.replaceAll("[^0-9]", "");
        if (numero.isEmpty()) {
            avisoContato("Número de WhatsApp inválido");
            return;
        }
        // O link wa.me exige o número em formato internacional, sem +, espaços ou traços.
        if (!numero.startsWith("55")) numero = "55" + numero;
        abrirLink("https://wa.me/" + numero);
    }

    private void abrirInstagram(String perfil) {
        String link = perfil.trim();
        if (link.startsWith("@")) {
            link = "https://www.instagram.com/" + link.substring(1) + "/";
        } else if (!link.startsWith("https://") && !link.startsWith("http://")) {
            if (link.startsWith("instagram.com/")) link = "https://www." + link;
            else if (link.startsWith("www.instagram.com/")) link = "https://" + link;
            else link = "https://www.instagram.com/" + link + "/";
        }
        abrirLink(link);
    }

    private void abrirTelefone(String telefone) {
        String numero = telefone.trim();
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(numero))));
        } catch (Exception e) {
            avisoContato("Não foi possível abrir o discador");
        }
    }

    private void abrirEmail(String email) {
        try {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Uri.encode(email))));
        } catch (Exception e) {
            avisoContato("Não foi possível abrir um aplicativo de e-mail");
        }
    }

    private void abrirLink(String link) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {
            avisoContato("Não foi possível abrir este contato");
        }
    }

    private String textoSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private void avisoContato(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    // Leva à revisão do pedido; o próprio freelancer não pode contratar a si mesmo
    private void configurarContratar(Freelancer f) {
        View btnContratar = findViewById(R.id.btnContratar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().equals(f.getId())) {
            btnContratar.setVisibility(View.GONE);
            return;
        }

        btnContratar.setOnClickListener(v -> {
            Intent intent = new Intent(this, RevisaoEquipeActivity.class);
            intent.putExtra("formato", "freelancer");
            intent.putExtra("freelancer_id", f.getId());
            intent.putExtra("freelancer_nome", f.getNome());
            intent.putExtra("freelancer_descricao", f.getArea() + " · " + f.getCategoria());
            startActivity(intent);
        });
    }

    private int dp(int valor) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics()));
    }
}