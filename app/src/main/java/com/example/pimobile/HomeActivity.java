package com.example.pimobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        InsetsUtil.aplicar(this);
        MenuUtil.configurar(this);

        Button btnMonteEquipe = findViewById(R.id.btnMonteEquipe);
        Button btnVerSkillPods = findViewById(R.id.btnVerSkillPods);

        btnMonteEquipe.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MontarEquipeActivity.class)));

        btnVerSkillPods.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SkillPodsActivity.class)));

        findViewById(R.id.txtVerTodos).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SkillPodsActivity.class)));

        findViewById(R.id.btnNotificacoes).setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Sem notificações", Toast.LENGTH_SHORT).show());

        configurarVideo();

        carregarSkillPodsPopulares();
    }

    private void configurarVideo() {
        VideoView videoView = findViewById(R.id.videoEmpresa);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_empresa);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
            videoView.start();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoView videoView = findViewById(R.id.videoEmpresa);
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoView videoView = findViewById(R.id.videoEmpresa);
        videoView.start();
    }

    private void carregarSkillPodsPopulares() {
        LinearLayout lista = findViewById(R.id.listaPodsPopulares);
        lista.removeAllViews();

        List<SkillPodResumo> pods = SkillPodRepository.getPopulares();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (SkillPodResumo pod : pods) {
            View card = inflater.inflate(R.layout.item_skill_pod_popular, lista, false);

            FrameLayout iconeContainer = card.findViewById(R.id.iconeContainer);
            ImageView imgIcone = card.findViewById(R.id.imgIcone);
            TextView txtNome = card.findViewById(R.id.txtNome);
            TextView txtQuantidade = card.findViewById(R.id.txtQuantidade);

            iconeContainer.setBackgroundResource(pod.getFundoIconeRes());
            imgIcone.setImageResource(pod.getIconeRes());
            txtNome.setText(pod.getNome());
            txtQuantidade.setText(pod.getQuantidadeEspecialistas() + " especialistas");

            card.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, SkillPodsActivity.class)));

            lista.addView(card);
        }
    }
}