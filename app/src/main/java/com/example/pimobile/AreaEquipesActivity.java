package com.example.pimobile;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class AreaEquipesActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Pedidos de equipes"; }
    @Override protected String dicaBusca() { return "Buscar por pod ou solicitante"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        AreaAdmRepository.getEquipes(callback);
    }

    @Override
    protected String[] rotulosCards() {
        return new String[]{"Total de pedidos", "Pendentes", "Aprovadas", "Recusadas"};
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
        AreaAdmRepository.atualizarStatusEquipe(registro.getId(), status, new Callback<Void>() {
            @Override
            public void onSucesso(Void resultado) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AreaEquipesActivity.this,
                        "Pedido " + ("aprovada".equals(status) ? "aprovado" : "recusado"),
                        Toast.LENGTH_SHORT).show();
                recarregar();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AreaEquipesActivity.this,
                        "Não foi possível atualizar o pedido", Toast.LENGTH_LONG).show();
            }
        });
    }
}