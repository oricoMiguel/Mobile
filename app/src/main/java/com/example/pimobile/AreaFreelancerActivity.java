package com.example.pimobile;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class AreaFreelancerActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Lista de freelancers"; }
    @Override protected String dicaBusca() { return "Buscar por nome ou especialidade"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getFreelancers(callback);
    }

    @Override
    protected void aoClicar(RegistroAdm registro) {
        new AlertDialog.Builder(this)
                .setTitle(registro.getNome())
                .setMessage(registro.getSubtitulo() + "\n" + registro.getDetalhe()
                        + "\nStatus atual: " + registro.getBadge())
                .setPositiveButton("Aprovar", (d, w) -> mudarStatus(registro, "aprovado"))
                .setNegativeButton("Recusar", (d, w) -> mudarStatus(registro, "recusado"))
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void mudarStatus(RegistroAdm registro, String status) {
        AreaAdmRepository.atualizarStatusFreelancer(registro.getId(), status, new Callback<Void>() {
            @Override
            public void onSucesso(Void resultado) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AreaFreelancerActivity.this,
                        "Perfil " + ("aprovado".equals(status) ? "aprovado" : "recusado"),
                        Toast.LENGTH_SHORT).show();
                recarregar();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AreaFreelancerActivity.this,
                        "Não foi possível atualizar o perfil", Toast.LENGTH_LONG).show();
            }
        });
    }
}