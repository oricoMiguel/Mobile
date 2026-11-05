
package com.example.pimobile;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.noties.markwon.Markwon;

public class ChatbotActivity extends AppCompatActivity {

    private static final String API_URL =
            "https://serv-plus-chatbot.miguell-oricoo.workers.dev/chat";

    private EditText edtMensagem;
    private TextView btnEnviar;
    private LinearLayout layoutMensagens;
    private ScrollView scrollMensagens;

    private Markwon markwon;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private boolean enviando = false;

    private JSONArray historico = ChatbotSession.historico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        markwon = Markwon.create(this);

        TextView btnVoltar = findViewById(R.id.btnVoltarChat);
        edtMensagem = findViewById(R.id.edtMensagemChat);
        btnEnviar = findViewById(R.id.btnEnviarChat);
        layoutMensagens = findViewById(R.id.layoutMensagens);
        scrollMensagens = findViewById(R.id.scrollMensagens);

        btnVoltar.setOnClickListener(v -> finish());
        btnEnviar.setOnClickListener(v -> enviarMensagem());

        restaurarConversa();
    }

    private void restaurarConversa() {

        layoutMensagens.removeAllViews();

        if (historico.length() == 0) {

            adicionarMensagem(
                    "Olá! Sou o assistente virtual do SERV+. Como posso ajudar?",
                    false
            );

            return;
        }

        for (int i = 0; i < historico.length(); i++) {

            JSONObject item = historico.optJSONObject(i);

            if (item == null) continue;

            String role = item.optString("role");
            String content = item.optString("content");

            if (role.equals("user")) {
                adicionarMensagem(content, true);

            } else if (role.equals("assistant")) {
                adicionarMensagem(content, false);
            }
        }

        if (ChatbotSession.aguardandoReenvio) {

            adicionarMensagem(
                    "Não foi possível enviar sua mensagem. Verifique sua conexão e tente novamente.",
                    false
            );

            adicionarBotaoTentarNovamente();
        }
    }

    private void enviarMensagem() {

        if (enviando || ChatbotSession.aguardandoReenvio) return;

        String mensagem = edtMensagem.getText().toString().trim();

        if (mensagem.isEmpty()) return;

        edtMensagem.setText("");

        adicionarMensagem(mensagem, true);

        enviando = true;
        btnEnviar.setEnabled(false);

        TextView digitando = criarIndicadorCarregamento();

        enviarParaWorker(mensagem, true, digitando);
    }

    private TextView criarIndicadorCarregamento() {

        TextView digitando = adicionarMensagem("●  ●  ●", false);

        Animation animacao = AnimationUtils.loadAnimation(
                this,
                R.anim.chat_pulse
        );

        digitando.startAnimation(animacao);

        return digitando;
    }

    private void enviarParaWorker(
            String mensagem,
            boolean adicionarAoHistorico,
            TextView digitando
    ) {

        executor.execute(() -> {

            try {

                if (adicionarAoHistorico) {

                    historico.put(new JSONObject()
                            .put("role", "user")
                            .put("content", mensagem));
                }

                JSONObject resposta = consultarWorker();

                if (!resposta.optBoolean("success")) {

                    throw new Exception(
                            resposta.optString(
                                    "error",
                                    "O servidor não conseguiu responder."
                            )
                    );
                }

                String textoResposta = resposta.optString("reply");

                historico.put(new JSONObject()
                        .put("role", "assistant")
                        .put("content", textoResposta));

                ChatbotSession.aguardandoReenvio = false;

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) return;

                    digitando.clearAnimation();
                    layoutMensagens.removeView(digitando);

                    adicionarMensagem(textoResposta, false);

                    finalizarEnvio();
                });

            } catch (Exception e) {

                e.printStackTrace();

                ChatbotSession.aguardandoReenvio = true;

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) return;

                    digitando.clearAnimation();
                    layoutMensagens.removeView(digitando);

                    adicionarMensagem(
                            "Não foi possível enviar sua mensagem. Verifique sua conexão e tente novamente.",
                            false
                    );

                    adicionarBotaoTentarNovamente();

                    finalizarEnvio();
                });
            }
        });
    }

    private void adicionarBotaoTentarNovamente() {

        TextView botao = new TextView(this);

        botao.setText("↻  Tentar novamente");
        botao.setTextSize(14);
        botao.setTextColor(Color.parseColor("#553C9A"));
        botao.setPadding(12, 8, 12, 8);
        botao.setClickable(true);
        botao.setFocusable(true);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.gravity = Gravity.START;
        parametros.bottomMargin = 12;

        botao.setLayoutParams(parametros);

        botao.setOnClickListener(v -> {

            if (enviando || !ChatbotSession.aguardandoReenvio) {
                return;
            }

            layoutMensagens.removeView(botao);

            ChatbotSession.aguardandoReenvio = false;

            enviando = true;
            btnEnviar.setEnabled(false);

            TextView digitando = criarIndicadorCarregamento();

            enviarParaWorker(null, false, digitando);
        });

        layoutMensagens.addView(botao);
    }

    private JSONObject consultarWorker() throws Exception {

        HttpURLConnection conexao = null;

        try {

            URL url = new URL(API_URL);
            conexao = (HttpURLConnection) url.openConnection();

            conexao.setRequestMethod("POST");
            conexao.setConnectTimeout(15000);
            conexao.setReadTimeout(90000);
            conexao.setDoOutput(true);

            conexao.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            // O histórico completo fica no aplicativo.
            // O Worker recebe somente as 12 mensagens mais recentes.

            JSONArray mensagensRecentes = new JSONArray();

            int inicio = Math.max(0, historico.length() - 12);

            for (int i = inicio; i < historico.length(); i++) {
                mensagensRecentes.put(historico.getJSONObject(i));
            }

            JSONObject corpo = new JSONObject();
            corpo.put("messages", mensagensRecentes);

            byte[] dados = corpo.toString()
                    .getBytes(StandardCharsets.UTF_8);

            try (OutputStream output = conexao.getOutputStream()) {
                output.write(dados);
            }

            int codigo = conexao.getResponseCode();

            InputStream input = codigo >= 200 && codigo < 300
                    ? conexao.getInputStream()
                    : conexao.getErrorStream();

            if (input == null) {
                throw new Exception("Resposta vazia do servidor.");
            }

            StringBuilder resultado = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {

                String linha;

                while ((linha = reader.readLine()) != null) {
                    resultado.append(linha);
                }
            }

            if (codigo < 200 || codigo >= 300) {
                throw new Exception(
                        "Erro HTTP: " + codigo +
                                " | Resposta do servidor: " + resultado
                );
            }

            return new JSONObject(resultado.toString());

        } finally {

            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    private TextView adicionarMensagem(
            String mensagem,
            boolean usuario
    ) {

        TextView texto = new TextView(this);

        if (usuario) {
            texto.setText(mensagem);
        } else {
            markwon.setMarkdown(texto, mensagem);
        }

        texto.setTextSize(15);
        texto.setTextColor(Color.parseColor("#382052"));
        texto.setPadding(16, 12, 16, 12);

        texto.setMaxWidth(
                (int) (getResources()
                        .getDisplayMetrics().widthPixels * 0.75f)
        );

        GradientDrawable fundo = new GradientDrawable();

        fundo.setColor(Color.parseColor(
                usuario ? "#E8DDF7" : "#FFFFFF"
        ));

        fundo.setCornerRadius(20);

        texto.setBackground(fundo);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.gravity = usuario
                ? Gravity.END
                : Gravity.START;

        parametros.bottomMargin = 12;

        texto.setLayoutParams(parametros);

        layoutMensagens.addView(texto);

        scrollMensagens.post(() ->
                scrollMensagens.fullScroll(View.FOCUS_DOWN)
        );

        return texto;
    }

    private void finalizarEnvio() {

        enviando = false;

        btnEnviar.setEnabled(!ChatbotSession.aguardandoReenvio);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (enviando) {
            ChatbotSession.aguardandoReenvio = true;
        }

        executor.shutdownNow();
    }
}
