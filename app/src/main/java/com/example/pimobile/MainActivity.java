package com.example.pimobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<?> destino = FirebaseAuth.getInstance().getCurrentUser() != null
                ? HomeActivity.class
                : LoginActivity.class;
        startActivity(new Intent(this, destino));
        finish();
    }
}