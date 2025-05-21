package com.example.projetraid;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class MessageActivity extends AppCompatActivity {
    private TextView textChat;
    private EditText inputMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        textChat = findViewById(R.id.text_chat);
        inputMessage = findViewById(R.id.input_message);
        btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(v -> {
            String msg = inputMessage.getText().toString();
            if (!msg.isEmpty()) {
                MainActivity.transferData.write(msg.getBytes());
                textChat.append("\nMoi: " + msg);
                inputMessage.setText("");
            }
        });

        // ✅ Remplacement ici : utilisation du getter
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            try {
                InputStream in = MainActivity.transferData.getInputStream(); // ✅ CORRECTION ICI

                while ((bytes = in.read(buffer)) != -1) {
                    String received = new String(buffer, 0, bytes);
                    runOnUiThread(() -> textChat.append("\nAutre: " + received));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
