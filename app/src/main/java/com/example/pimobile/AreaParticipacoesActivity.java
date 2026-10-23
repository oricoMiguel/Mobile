package com.example.pimobile;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

public class AreaParticipacoesActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Pedidos de pods"; }
    @Override protected String dicaBusca() { return "Buscar por freelancer ou pod"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getParticipacoes(callback);
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de pedidos", "Pendentes", "Aprovados", "Recusados"};
    }

    @Override
    protected String[] valoresCards(List<RegistroAdm> lista) {
        return new String[]{
                String.valueOf(lista.size()),
                String.valueOf(contar(lista, 1)),
                String.valueOf(contar(lista, 0)),
                String.valueOf(contar(lista, 2))
        };
    }

    @Override
    protected void aoClicar(RegistroAdm registro) {
        new AlertDialog.Builder(this)
                .setTitle(registro.getNome())
                .setMessage(registro.getSubtitulo() + "\n" + registro.getDetalhe()
                        + "\nStatus atual: " + registro.getBadge())
                .setPositiveButton("Aprovar", (d, w) -> mudarStatus(registro, "aprovada"))
                .setNegativeButton("Recusar", (d, w) -> mudarStatus(registro, "recusada"))
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void mudarStatus(RegistroAdm registro, String status) {
        AreaAdmRepository.atualizarStatusParticipacao(registro.getId(), status, new Callback<Void>() {
            @Override
            public void onSucesso(Void resultado) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AreaParticipacoesActivity.this,
                        "Pedido " + ("aprovada".equals(status) ? "aprovado" : "recusado"),
                        Toast.LENGTH_SHORT).show();
                recarregar();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                boolean jaDecidido = e instanceof FirebaseFirestoreException
                        && ((FirebaseFirestoreException) e).getCode()
                        == FirebaseFirestoreException.Code.FAILED_PRECONDITION;
                Toast.makeText(AreaParticipacoesActivity.this,
                        jaDecidido ? "Este pedido já foi decidido por outro admin"
                                : "Não foi possível atualizar o pedido",
                        Toast.LENGTH_LONG).show();
                if (jaDecidido) recarregar();
            }
        });
    }
}