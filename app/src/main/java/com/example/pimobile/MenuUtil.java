package com.example.pimobile;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MenuUtil {

    public static void configurar(Activity activity) {
        DrawerLayout drawer = activity.findViewById(R.id.drawerLayout);
        ImageView btnMenu = activity.findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.END));

        ligar(activity, drawer, R.id.menuInicio, HomeActivity.class);
        ligar(activity, drawer, R.id.menuServicos, ServicoActivity.class);
        ligar(activity, drawer, R.id.menuSkillPods, SkillPodsActivity.class);
        ligar(activity, drawer, R.id.menuAdm, AdmActivity.class);
        ligar(activity, drawer, R.id.menuMonteEquipe, MontarEquipeActivity.class);
        ligar(activity, drawer, R.id.menuEntrar, LoginActivity.class);
        ligar(activity, drawer, R.id.menuCadastrar, CadastroActivity.class);
    }

    private static void ligar(Activity activity, DrawerLayout drawer, int id, Class<?> destino) {
        activity.findViewById(id).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.END);
            if (!activity.getClass().equals(destino)) {
                Intent intent = new Intent(activity, destino);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        });
    }
}