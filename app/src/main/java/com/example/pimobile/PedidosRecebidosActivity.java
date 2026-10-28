package com.example.pimobile;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class PedidosRecebidosActivity extends AreaAdmBaseActivity {

    @Override protected String tituloLista() { return "Pedidos recebidos"; }
    @Override protected String dicaBusca() { return "Buscar por cliente"; }

    @Override
    protected void carregarRegistros(Callback<List<RegistroAdm>> callback) {
        EquipeRepository.getPedidosRecebidos(callback);
    }

    @Override
    protected void aoClicar(RegistroAdm registro) {
        // Pedido já respondido: só informa
        if (!"Pendente".equals(registro.getBadge())) {
            Toast.makeText(this, "Pedido " + registro.getBadge().toLowerCase(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(registro.getNome())
                .setMessage(registro.getNome() + " quer contratar você.\n" + registro.getDetalhe())
                .setPositiveButton("Aceitar", (d, w) -> responder(registro, true))
                .setNegativeButton("Recusar", (d, w) -> responder(registro, false))
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void responder(RegistroAdm registro, boolean aceitar) {
        EquipeRepository.responderPedido(registro.getId(), aceitar, new Callback<Void>() {
            @Override
            public void onSucesso(Void resultado) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(PedidosRecebidosActivity.this,
                        aceitar ? "Pedido aceito" : "Pedido recusado",
                        Toast.LENGTH_SHORT).show();
                recarregar();
            }

            @Override
            public void onErro(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(PedidosRecebidosActivity.this,
                        "Não foi possível responder o pedido", Toast.LENGTH_LONG).show();
            }
        });
    }
}