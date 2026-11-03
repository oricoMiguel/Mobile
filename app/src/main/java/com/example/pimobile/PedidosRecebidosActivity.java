package com.example.pimobile;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(registro.getNome());

        dialog.setMessage(
                registro.getNome() + " quer contratar você.\n"
                        + registro.getDetalhe()
        );

        dialog.setNeutralButton("Ver perfil", (d, w) -> {

            String usuarioId = registro.getContratanteId();

            if (usuarioId == null || usuarioId.trim().isEmpty()) {
                Toast.makeText(
                        this,
                        "Não foi possível identificar o contratante",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    this,
                    PerfilActivity.class
            );

            intent.putExtra("usuarioId", usuarioId);
            startActivity(intent);
        });

        if ("Pendente".equals(registro.getBadge())) {

            dialog.setPositiveButton(
                    "Aceitar",
                    (d, w) -> responder(registro, true)
            );

            dialog.setNegativeButton(
                    "Recusar",
                    (d, w) -> responder(registro, false)
            );

        } else {

            dialog.setPositiveButton("Fechar", null);
        }

        dialog.show();
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
    @Override
    protected void aoErroCarregar(Exception e) {
        Toast.makeText(
                this,
                "Não foi possível carregar seus pedidos. Verifique sua conexão e permissões.",
                Toast.LENGTH_LONG
        ).show();
    }
}