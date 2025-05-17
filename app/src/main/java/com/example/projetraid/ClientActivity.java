package com.example.projetraid;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private LinearLayout layoutDevices;
    private TransferData transferData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client); // Crée ce layout

        layoutDevices = findViewById(R.id.linearLayoutDevices); // Défini dans activity_client.xml
        transferData = TransferData.getInstance(); // Singleton

        // Thread de réception de données du serveur
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytes;

                while ((bytes = transferData.getInputStream().read(buffer)) != -1) {
                    String received = new String(buffer, 0, bytes);
                    Log.d(TAG, "Message reçu : " + received);

                    runOnUiThread(() -> {
                        // Simule la création d’un device (à adapter selon le format reçu)
                        View deviceView = createDeviceView("Lampe", received, true);
                        layoutDevices.addView(deviceView);
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Erreur de lecture Bluetooth", e);
            }
        }).start();
    }

    private View createDeviceView(String name, String info, boolean isOn) {
        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        // Texte 1 : nom
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setId(View.generateViewId());

        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        nameParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        // Texte 2 : info
        TextView tvInfo = new TextView(this);
        tvInfo.setText(info);
        tvInfo.setId(View.generateViewId());

        RelativeLayout.LayoutParams infoParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        infoParams.addRule(RelativeLayout.BELOW, tvName.getId());

        // Bouton ON/OFF
        Button btn = new Button(this);
        btn.setText(isOn ? "OFF" : "ON");
        btn.setId(View.generateViewId());

        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);

        btn.setOnClickListener(v -> {
            String msg = name + ":" + (isOn ? "off" : "on");
            transferData.write(msg.getBytes());
            Toast.makeText(this, "Commande envoyée : " + msg, Toast.LENGTH_SHORT).show();
        });

        layout.addView(tvName, nameParams);
        layout.addView(tvInfo, infoParams);
        layout.addView(btn, btnParams);
        layout.setLayoutParams(params);

        return layout;
    }
}
