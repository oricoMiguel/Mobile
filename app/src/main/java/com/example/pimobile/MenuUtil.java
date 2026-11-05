
package com.example.pimobile;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import android.graphics.drawable.Drawable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MenuUtil {

    private static final String TAG = "SIDEBAR_FOTO";

    // Cache para evitar consultas desnecessárias
    private static String cacheUid = null;
    private static Boolean cacheAdmin = null;

    public static void configurar(Activity activity) {

        DrawerLayout drawer = activity.findViewById(R.id.drawerLayout);
        ImageView btnMenu = activity.findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v ->
                drawer.openDrawer(GravityCompat.END));

        ligar(activity, drawer, R.id.menuInicio, HomeActivity.class);
        ligar(activity, drawer, R.id.menuServicos, ServicoActivity.class);
        ligar(activity, drawer, R.id.menuSkillPods, SkillPodsActivity.class);
        ligar(activity, drawer, R.id.menuAdm, AdmActivity.class);
        ligar(activity, drawer, R.id.menuMonteEquipe, MontarEquipeActivity.class);
        ligar(activity, drawer, R.id.menuPerfil, PerfilActivity.class);
        ligar(activity, drawer, R.id.menuPrecisaAjuda, ChatbotActivity.class);

        configurarSair(activity, drawer);
        carregarCabecalho(activity);
    }

    private static void ligar(Activity activity,
                              DrawerLayout drawer,
                              int id,
                              Class<?> destino) {

        View item = activity.findViewById(id);

        item.setSelected(activity.getClass().equals(destino));

        item.setOnClickListener(v -> {

            drawer.closeDrawer(GravityCompat.END);

            if (!activity.getClass().equals(destino)) {

                Intent intent = new Intent(activity, destino);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        });
    }

    private static void configurarSair(Activity activity,
                                       DrawerLayout drawer) {

        activity.findViewById(R.id.menuSair).setOnClickListener(v -> {

            drawer.closeDrawer(GravityCompat.END);

            FirebaseAuth.getInstance().signOut();

            cacheUid = null;
            cacheAdmin = null;

            Intent intent = new Intent(activity, LoginActivity.class);

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            activity.startActivity(intent);
        });
    }

    private static void carregarCabecalho(Activity activity) {

        View adm = activity.findViewById(R.id.menuAdm);
        View secaoAdm = activity.findViewById(R.id.menuSecaoAdm);

        TextView txtNome = activity.findViewById(R.id.menuNome);
        TextView txtEmail = activity.findViewById(R.id.menuEmail);
        TextView txtPapel = activity.findViewById(R.id.menuPapel);

        ImageView imgAvatar = activity.findViewById(R.id.menuAvatarFoto);
        TextView txtAvatar = activity.findViewById(R.id.menuAvatarIniciais);

        adm.setVisibility(View.GONE);
        secaoAdm.setVisibility(View.GONE);

        PerfilRepository.getPerfilAtual(new Callback<Perfil>() {

            @Override
            public void onSucesso(Perfil perfil) {

                if (activity.isFinishing() || activity.isDestroyed()) return;

                txtNome.setText(perfil.getNome());
                txtEmail.setText(perfil.getEmail());

                String fotoUrl = perfil.getFotoUrl();

                Log.d(TAG, "Nome: " + perfil.getNome());
                Log.d(TAG, "URL: " + fotoUrl);
                Log.d(TAG, "Public ID: " + perfil.getFotoPublicId());

                if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {

                    Log.d(TAG, "Tentando carregar imagem do Cloudinary...");

                    // Esconde as iniciais enquanto tenta carregar a foto
                    txtAvatar.setVisibility(View.GONE);
                    imgAvatar.setVisibility(View.VISIBLE);

                    Glide.with(activity)
                            .load(fotoUrl.trim())
                            .centerCrop()
                            .listener(new RequestListener<Drawable>() {

                                @Override
                                public boolean onLoadFailed(
                                        GlideException e,
                                        Object model,
                                        Target<Drawable> target,
                                        boolean isFirstResource) {

                                    Log.e(TAG, "ERRO AO CARREGAR FOTO", e);

                                    if (activity.isFinishing()
                                            || activity.isDestroyed()) {
                                        return false;
                                    }

                                    imgAvatar.setVisibility(View.GONE);

                                    txtAvatar.setText(perfil.getIniciais());
                                    txtAvatar.setVisibility(View.VISIBLE);

                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(
                                        Drawable resource,
                                        Object model,
                                        Target<Drawable> target,
                                        DataSource dataSource,
                                        boolean isFirstResource) {

                                    Log.d(TAG, "FOTO CARREGADA COM SUCESSO!");
                                    Log.d(TAG, "Origem: " + dataSource);

                                    if (activity.isFinishing()
                                            || activity.isDestroyed()) {
                                        return false;
                                    }

                                    txtAvatar.setVisibility(View.GONE);
                                    imgAvatar.setVisibility(View.VISIBLE);

                                    return false;
                                }
                            })
                            .into(imgAvatar);

                } else {

                    Log.w(TAG, "URL da foto vazia. Exibindo iniciais.");

                    imgAvatar.setImageDrawable(null);
                    imgAvatar.setVisibility(View.GONE);

                    txtAvatar.setText(perfil.getIniciais());
                    txtAvatar.setVisibility(View.VISIBLE);
                }

                // Não sobrescreve Admin
                if (!Boolean.TRUE.equals(cacheAdmin)) {
                    txtPapel.setText(perfil.getTipoConta());
                }
            }

            @Override
            public void onErro(Exception e) {

                Log.e(TAG, "Erro ao buscar perfil no Firestore", e);
            }
        });

        verificarAdmin(activity, adm, secaoAdm, txtPapel);
    }

    private static void verificarAdmin(Activity activity,
                                       View adm,
                                       View secaoAdm,
                                       TextView txtPapel) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        // Utiliza o cache quando disponível
        if (user.getUid().equals(cacheUid) && cacheAdmin != null) {

            aplicarAdmin(adm, secaoAdm, txtPapel, cacheAdmin);
            return;
        }

        AdmRepository.ehAdmin(new Callback<Boolean>() {

            @Override
            public void onSucesso(Boolean ehAdmin) {

                if (activity.isFinishing() || activity.isDestroyed()) return;

                cacheUid = user.getUid();
                cacheAdmin = ehAdmin;

                aplicarAdmin(adm, secaoAdm, txtPapel, ehAdmin);
            }

            @Override
            public void onErro(Exception e) {

                Log.e(TAG, "Erro ao verificar administrador", e);
            }
        });
    }

    private static void aplicarAdmin(View adm,
                                     View secaoAdm,
                                     TextView txtPapel,
                                     boolean ehAdmin) {

        adm.setVisibility(ehAdmin ? View.VISIBLE : View.GONE);
        secaoAdm.setVisibility(ehAdmin ? View.VISIBLE : View.GONE);

        if (ehAdmin) {
            txtPapel.setText("Admin");
        }
    }
}
