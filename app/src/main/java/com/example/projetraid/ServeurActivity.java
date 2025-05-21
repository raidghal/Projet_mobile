package com.example.projetraid;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;



public class ServeurActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Rediriger immédiatement vers la messagerie
        Intent intent = new Intent(ServeurActivity.this, MessageActivity.class);
        startActivity(intent);

        // Fermer cette activité pour ne pas revenir dessus avec le bouton retour
        finish();
    }
}
