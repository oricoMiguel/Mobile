package com.example.pimobile;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TermosActivity extends AppCompatActivity {

    // TROQUE pelo e-mail de contato do projeto
    private static final String EMAIL_CONTATO = "seu-email@exemplo.com";
    private static final String ATUALIZACAO = "26 de outubro de 2026";

    private static final String INTRO_TERMOS =
            "Ao criar uma conta e usar o SERV+, você concorda com os termos abaixo. "
                    + "Leia com atenção.";

    private static final String[][] TERMOS = {
            {"1. Sobre o SERV+",
                    "O SERV+ é uma plataforma que conecta pessoas que precisam de serviços a "
                            + "freelancers e a equipes modulares, chamadas Skill Pods. No aplicativo "
                            + "você pode consultar freelancers, conhecer os Skill Pods e solicitar a "
                            + "montagem de uma equipe."},
            {"2. Conta e cadastro",
                    "Para usar o SERV+ você precisa criar uma conta com nome, e-mail e senha, "
                            + "escolhendo o tipo de conta: Usuário ou Freelancer. Você deve informar "
                            + "dados verdadeiros e mantê-los atualizados. A conta é pessoal e "
                            + "intransferível, e você é responsável por manter sua senha em segurança "
                            + "e por tudo o que for feito com ela."},
            {"3. Perfil de freelancer e aprovação",
                    "O perfil profissional de um freelancer (área de atuação, categoria, "
                            + "habilidades e disponibilidade) passa pela análise de um administrador "
                            + "antes de aparecer na lista de Serviços. Alterações em nome, área, "
                            + "categoria ou habilidades enviam o perfil para uma nova análise. O SERV+ "
                            + "pode recusar um perfil que contenha informações falsas, enganosas ou "
                            + "inadequadas."},
            {"4. Equipes e Skill Pods",
                    "Pedidos de equipe e pedidos de entrada em um Skill Pod ficam pendentes até "
                            + "serem aprovados ou recusados por um administrador. Você acompanha o "
                            + "resultado em Minhas equipes e nas notificações do aplicativo. Enviar um "
                            + "pedido não garante a sua aprovação."},
            {"5. Uso adequado",
                    "Você concorda em não:\n"
                            + "• informar dados falsos ou se passar por outra pessoa;\n"
                            + "• usar o aplicativo para fins ilegais ou para prejudicar terceiros;\n"
                            + "• tentar acessar dados ou áreas de outros usuários ou do sistema sem "
                            + "autorização;\n"
                            + "• prejudicar o funcionamento do aplicativo."},
            {"6. Responsabilidades",
                    "O SERV+ aproxima usuários e freelancers, mas não é parte dos acordos feitos "
                            + "entre eles e não garante a qualidade, o prazo ou o resultado de nenhum "
                            + "serviço. Nesta versão, o aplicativo não processa pagamentos. O SERV+ "
                            + "está em desenvolvimento e pode apresentar instabilidades ou mudanças."},
            {"7. Suspensão e encerramento",
                    "Contas que descumpram estes termos podem ser suspensas ou encerradas. Você "
                            + "pode pedir o encerramento da sua conta a qualquer momento pelo contato "
                            + "indicado abaixo."},
            {"8. Alterações nos termos",
                    "Estes termos podem ser atualizados. A data da última atualização aparece no "
                            + "topo desta tela, e continuar usando o aplicativo depois de uma mudança "
                            + "significa que você concorda com a nova versão."},
            {"9. Contato",
                    "Dúvidas sobre estes termos: {EMAIL}."}
    };

    private static final String INTRO_PRIVACIDADE =
            "Esta política explica quais dados o SERV+ coleta, para que os usamos e quais são "
                    + "os seus direitos, conforme a Lei Geral de Proteção de Dados (LGPD, "
                    + "Lei nº 13.709/2018).";

    private static final String[][] PRIVACIDADE = {
            {"1. Dados que coletamos",
                    "• Cadastro: nome, e-mail e tipo de conta (Usuário ou Freelancer).\n"
                            + "• Acesso: sua senha é gerenciada pelo serviço de autenticação do Google "
                            + "Firebase, e o SERV+ não tem acesso a ela.\n"
                            + "• Perfil profissional (freelancers): área de atuação, categoria, "
                            + "habilidades, disponibilidade e nota.\n"
                            + "• Uso do aplicativo: pedidos de equipe, pedidos de entrada em Skill "
                            + "Pods, notificações recebidas e as datas dessas ações."},
            {"2. Para que usamos seus dados",
                    "Usamos seus dados para criar e manter sua conta, exibir perfis de freelancers "
                            + "aprovados, processar e acompanhar pedidos de equipe e de Skill Pods, "
                            + "enviar avisos dentro do aplicativo e manter a segurança e a moderação "
                            + "da plataforma."},
            {"3. O que outras pessoas veem",
                    "O perfil profissional de um freelancer aprovado (nome, área, categoria, nota, "
                            + "habilidades, disponibilidade e mês de entrada) pode ser visto por "
                            + "outros usuários logados. Os nomes dos membros de um Skill Pod também "
                            + "aparecem na tela do pod. Seu e-mail não é exibido para outros usuários."},
            {"4. Com quem compartilhamos",
                    "Não vendemos seus dados. Eles ficam armazenados na infraestrutura do Google "
                            + "(Firebase Authentication e Cloud Firestore), que atua como operador de "
                            + "dados do SERV+. Os administradores acessam dados de contas e pedidos "
                            + "apenas para aprovar perfis, equipes e entradas em pods e para moderar "
                            + "a plataforma. Também podemos compartilhar dados se houver obrigação "
                            + "legal."},
            {"5. Segurança",
                    "Usamos autenticação e regras de acesso para que cada pessoa só consiga ler e "
                            + "alterar o que lhe pertence. Nenhum sistema é totalmente imune a falhas, "
                            + "por isso também pedimos que você proteja sua senha."},
            {"6. Por quanto tempo guardamos",
                    "Mantemos seus dados enquanto sua conta estiver ativa. Quando você pedir o "
                            + "encerramento da conta, eliminamos ou anonimizamos os dados, exceto os "
                            + "que precisarmos guardar por obrigação legal."},
            {"7. Seus direitos",
                    "Pela LGPD, você pode pedir:\n"
                            + "• confirmação de que tratamos seus dados e acesso a eles;\n"
                            + "• correção de dados incompletos ou desatualizados;\n"
                            + "• anonimização, bloqueio ou eliminação de dados desnecessários;\n"
                            + "• portabilidade dos dados;\n"
                            + "• informações sobre com quem compartilhamos seus dados;\n"
                            + "• revogação do consentimento.\n\n"
                            + "Você pode corrigir seu nome e seu perfil profissional em Perfil > "
                            + "Editar. Para os demais pedidos, use o contato abaixo."},
            {"8. Menores de idade",
                    "O SERV+ é destinado a maiores de 18 anos."},
            {"9. Alterações e contato",
                    "Esta política pode ser atualizada, e a data da última atualização aparece no "
                            + "topo desta tela. Para exercer seus direitos ou tirar dúvidas: {EMAIL}."}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termos);
        InsetsUtil.aplicar(this);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.txtAtualizacao))
                .setText("Última atualização: " + ATUALIZACAO);

        LinearLayout conteudo = findViewById(R.id.conteudoTermos);
        adicionarSecao(conteudo, "Termos de uso", INTRO_TERMOS, TERMOS);
        adicionarSecao(conteudo, "Política de privacidade", INTRO_PRIVACIDADE, PRIVACIDADE);
    }

    private void adicionarSecao(LinearLayout conteudo, String titulo, String intro,
                                String[][] itens) {
        conteudo.addView(criarTexto(titulo, 20, true, 32));
        conteudo.addView(criarTexto(intro, 14, false, 8));

        for (String[] item : itens) {
            conteudo.addView(criarTexto(item[0], 15, true, 18));
            conteudo.addView(criarTexto(item[1].replace("{EMAIL}", EMAIL_CONTATO), 14, false, 4));
        }
    }

    private TextView criarTexto(String texto, int tamanhoSp, boolean negrito, int margemTopDp) {
        TextView view = new TextView(this);
        view.setText(texto);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanhoSp);
        view.setTextColor(ContextCompat.getColor(this, R.color.eq_escuro));
        if (negrito) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        } else {
            view.setLineSpacing(dp(3), 1f);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(margemTopDp);
        view.setLayoutParams(lp);
        return view;
    }

    private int dp(int valor) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics()));
    }
}