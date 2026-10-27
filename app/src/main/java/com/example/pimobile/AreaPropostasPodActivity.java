package com.example.pimobile;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

public class AreaPropostasPodActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Propostas de pods"; }
    @Override protected String dicaBusca() { return "Buscar por pod ou autor"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getPropostasPod(callback);
    }

    @Override
    protected void aoClicar(RegistroAdm registro) {
        String descricao = registro instanceof PropostaPodRegistro
                ? ((PropostaPodRegistro) registro).getDescricao() : "";

        new AlertDialog.Builder(this)
                .setTitle(registro.getNome())
                .setMessage(registro.getSubtitulo() + "\n" + registro.getDetalhe()
                        + "\n\n" + descricao
                        + "\n\nStatus atual: " + registro.getBadge())
                .setPositiveButton("Aprovar", (d, w) -> mudarStatus(registro, "aprovada"))
                .setNegativeButton("Recusar", (d, w) -> mudarStatus(registro, "recusada"))
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void mudarStatus(RegistroAdm registro, String status) {
        AreaAdmRepository.atualizarStatusPropostaPod(registro.getId(), status, new Callback<Void>() {
            @Override
            public void onSucesso(Void resultado) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AreaPropostasPodActivity.this,
                        "aprovada".equals(status) ? "Pod criado e publicado" : "Proposta recusada",
                        Toast.LENGTH_SHORT).show();
                recarregar();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                boolean jaDecidida = e instanceof FirebaseFirestoreException
                        && ((FirebaseFirestoreException) e).getCode()
                        == FirebaseFirestoreException.Code.FAILED_PRECONDITION;
                Toast.makeText(AreaPropostasPodActivity.this,
                        jaDecidida ? "Esta proposta já foi decidida"
                                : "Não foi possível atualizar a proposta",
                        Toast.LENGTH_LONG).show();
                if (jaDecidida) recarregar();
            }
        });
    }
}
