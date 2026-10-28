package com.example.pimobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;

public class EditarPerfilActivity extends AppCompatActivity {

    private static final String[] CATEGORIAS = {"Desenvolvimento", "Design", "Marketing", "Outros"};

    private EditText edtNome, edtArea, edtHabilidades, edtDisponibilidade;
    private EditText edtTelefoneUsuario, edtEmpresa, edtCidade, edtEstado;
    private EditText edtWhatsapp, edtInstagram, edtTelefone, edtEmailContato;
    private Spinner spinnerCategoria;
    private SwitchCompat switchDisponivel;
    private TextView txtStatus;
    private Button btnSalvar;
    private View secaoProfissional, secaoUsuario;

    private String nomeOriginal = "";
    private boolean ehFreelancer = false;
    private PerfilProfissional atual = null; // null = ainda não tem perfil profissional

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);
        InsetsUtil.aplicar(this);

        edtNome = findViewById(R.id.edtNome);
        edtArea = findViewById(R.id.edtArea);
        edtHabilidades = findViewById(R.id.edtHabilidades);
        edtDisponibilidade = findViewById(R.id.edtDisponibilidade);
        edtWhatsapp = findViewById(R.id.edtWhatsapp);
        edtInstagram = findViewById(R.id.edtInstagram);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtEmailContato = findViewById(R.id.edtEmailContato);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        switchDisponivel = findViewById(R.id.switchDisponivel);
        txtStatus = findViewById(R.id.txtStatus);
        btnSalvar = findViewById(R.id.btnSalvar);
        secaoProfissional = findViewById(R.id.secaoProfissional);
        secaoUsuario = findViewById(R.id.secaoUsuario);
        edtTelefoneUsuario = findViewById(R.id.edtTelefoneUsuario);
        edtEmpresa = findViewById(R.id.edtEmpresa);
        edtCidade = findViewById(R.id.edtCidade);
        edtEstado = findViewById(R.id.edtEstado);

        spinnerCategoria.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, CATEGORIAS));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // Só habilita o botão depois que os dados atuais chegarem
        btnSalvar.setEnabled(false);
        btnSalvar.setOnClickListener(v -> salvar());

        PerfilRepository.getPerfilAtual(new Callback<Perfil>() {
            @Override
            public void onSucesso(Perfil perfil) {
                if (isFinishing() || isDestroyed()) return;
                nomeOriginal = perfil.getNome();
                edtNome.setText(perfil.getNome());

                ehFreelancer = "Freelancer".equals(perfil.getTipoConta());
                if (ehFreelancer) {
                    secaoProfissional.setVisibility(View.VISIBLE);
                    secaoUsuario.setVisibility(View.GONE);
                    carregarPerfilProfissional();
                } else {
                    secaoUsuario.setVisibility(View.VISIBLE);
                    edtTelefoneUsuario.setText(perfil.getTelefone());
                    edtEmpresa.setText(perfil.getEmpresa());
                    edtCidade.setText(perfil.getCidade());
                    edtEstado.setText(perfil.getEstado());
                    btnSalvar.setEnabled(true);
                }
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(EditarPerfilActivity.this,
                        "Não foi possível carregar seu perfil", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void carregarPerfilProfissional() {
        FreelancerRepository.getMeuPerfil(new Callback<PerfilProfissional>() {
            @Override
            public void onSucesso(PerfilProfissional perfil) {
                if (isFinishing() || isDestroyed()) return;
                atual = perfil;

                if (perfil != null) {
                    edtArea.setText(perfil.getArea());
                    spinnerCategoria.setSelection(indiceCategoria(perfil.getCategoria()));
                    edtHabilidades.setText(TextUtils.join(", ", perfil.getHabilidades()));
                    switchDisponivel.setChecked(perfil.isDisponivel());
                    edtDisponibilidade.setText(perfil.getDisponibilidade());
                    edtWhatsapp.setText(perfil.getWhatsapp());
                    edtInstagram.setText(perfil.getInstagram());
                    edtTelefone.setText(perfil.getTelefone());
                    edtEmailContato.setText(perfil.getEmailContato());
                    txtStatus.setText(textoStatus(perfil.getStatus()));
                } else {
                    switchDisponivel.setChecked(true);
                    txtStatus.setText("Seu perfil será analisado por um administrador "
                            + "antes de aparecer em Serviços.");
                }
                btnSalvar.setEnabled(true);
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(EditarPerfilActivity.this,
                        "Não foi possível carregar seu perfil profissional",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void salvar() {
        String nome = edtNome.getText().toString().trim();
        if (nome.isEmpty()) {
            aviso("Digite seu nome");
            return;
        }
        if (nome.length() > 100) {
            aviso("O nome pode ter no máximo 100 caracteres");
            return;
        }

        if (!ehFreelancer) {
            String telefone = edtTelefoneUsuario.getText().toString().trim();
            String empresa = edtEmpresa.getText().toString().trim();
            String cidade = edtCidade.getText().toString().trim();
            String estado = edtEstado.getText().toString().trim();

            if (telefone.length() > 30) {
                aviso("O telefone pode ter no máximo 30 caracteres");
                return;
            }
            if (empresa.length() > 100) {
                aviso("O nome da empresa pode ter no máximo 100 caracteres");
                return;
            }
            if (cidade.length() > 80) {
                aviso("A cidade pode ter no máximo 80 caracteres");
                return;
            }
            if (estado.length() > 50) {
                aviso("O estado pode ter no máximo 50 caracteres");
                return;
            }

            btnSalvar.setEnabled(false);
            PerfilRepository.atualizarPerfilUsuario(nome, telefone, empresa, cidade, estado, new Callback<Void>() {
                @Override
                public void onSucesso(Void resultado) {
                    if (isFinishing() || isDestroyed()) return;
                    aviso("Perfil atualizado");
                    finish();
                }

                @Override
                public void onErro(Exception e) {
                    if (isFinishing() || isDestroyed()) return;
                    btnSalvar.setEnabled(true);
                    aviso("Não foi possível salvar. Tente novamente.");
                }
            });
            return;
        }

        String area = edtArea.getText().toString().trim();
        if (area.isEmpty()) {
            aviso("Informe sua área de atuação");
            return;
        }
        if (area.length() > 80) {
            aviso("A área pode ter no máximo 80 caracteres");
            return;
        }

        List<String> habilidades = lerHabilidades();
        if (habilidades.isEmpty()) {
            aviso("Informe pelo menos uma habilidade");
            return;
        }
        if (habilidades.size() > 10) {
            aviso("Use no máximo 10 habilidades");
            return;
        }

        boolean disponivel = switchDisponivel.isChecked();
        String disponibilidade = edtDisponibilidade.getText().toString().trim();
        if (disponibilidade.isEmpty()) {
            disponibilidade = disponivel ? "Disponível agora" : "Indisponível";
        }
        if (disponibilidade.length() > 60) {
            aviso("O texto de disponibilidade pode ter no máximo 60 caracteres");
            return;
        }

        String categoria = CATEGORIAS[spinnerCategoria.getSelectedItemPosition()];

        String whatsapp = edtWhatsapp.getText().toString().trim();
        String instagram = edtInstagram.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String emailContato = edtEmailContato.getText().toString().trim();

        if (whatsapp.length() > 30) {
            aviso("O WhatsApp pode ter no máximo 30 caracteres");
            return;
        }
        if (instagram.length() > 150) {
            aviso("O Instagram pode ter no máximo 150 caracteres");
            return;
        }
        if (telefone.length() > 30) {
            aviso("O telefone pode ter no máximo 30 caracteres");
            return;
        }
        if (emailContato.length() > 120) {
            aviso("O e-mail pode ter no máximo 120 caracteres");
            return;
        }
        if (!emailContato.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailContato).matches()) {
            aviso("Informe um e-mail de contato válido");
            return;
        }

        btnSalvar.setEnabled(false);
        FreelancerRepository.salvarMeuPerfil(nome, !nome.equals(nomeOriginal), area, categoria,
                habilidades, disponivel, disponibilidade, whatsapp, instagram, telefone,
                emailContato, atual, new Callback<Boolean>() {
                    @Override
                    public void onSucesso(Boolean precisaAprovacao) {
                        if (isFinishing() || isDestroyed()) return;
                        aviso(precisaAprovacao
                                ? "Perfil enviado para aprovação"
                                : "Perfil atualizado");
                        finish();
                    }

                    @Override
                    public void onErro(Exception e) {
                        if (isFinishing() || isDestroyed()) return;
                        btnSalvar.setEnabled(true);
                        aviso("Não foi possível salvar. Tente novamente.");
                    }
                });
    }

    // "React, JavaScript,  css" vira ["React", "JavaScript", "css"] (sem repetidas)
    private List<String> lerHabilidades() {
        List<String> resultado = new ArrayList<>();
        for (String parte : edtHabilidades.getText().toString().split(",")) {
            String habilidade = parte.trim();
            if (habilidade.isEmpty()) continue;
            if (habilidade.length() > 30) habilidade = habilidade.substring(0, 30);

            boolean repetida = false;
            for (String existente : resultado) {
                if (existente.equalsIgnoreCase(habilidade)) {
                    repetida = true;
                    break;
                }
            }
            if (!repetida) resultado.add(habilidade);
        }
        return resultado;
    }

    private int indiceCategoria(String categoria) {
        for (int i = 0; i < CATEGORIAS.length; i++) {
            if (CATEGORIAS[i].equals(categoria)) return i;
        }
        return CATEGORIAS.length - 1; // Outros
    }

    private String textoStatus(String status) {
        switch (status) {
            case "aprovado":
                return "Perfil aprovado: aparece na lista de Serviços.";
            case "recusado":
                return "Perfil recusado. Ajuste as informações e salve para enviar de novo.";
            default:
                return "Aguardando aprovação de um administrador.";
        }
    }

    private void aviso(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}