 package com.example.projetraid;


import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    private TransferData transferData;
    public static boolean isServer;

    private static final UUID MY_UUID = UUID.fromString("8b5a1316-e19e-11ec-8fea-0242ac120002");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Button clientBtn = findViewById(R.id.clientBtn);
        Button serverBtn = findViewById(R.id.serveurBtn);
        TextView attentetxt = findViewById(R.id.attenteTxt);
        attentetxt.setText("");

        serverBtn.setOnClickListener(v -> {
            clientBtn.setVisibility(View.INVISIBLE);
            serverBtn.setText("serveur en attente");
            serverBtn.setClickable(false);
            attentetxt.setText("*Attente de connexion d'un client*");

            Log.d("Bluetooth", "Mode serveur activé. En attente de connexion...");

            new ServerThread().start();
            isServer = true;
        });

        clientBtn.setOnClickListener(v -> {
            serverBtn.setVisibility(View.INVISIBLE);
            clientBtn.setText("client en attente");
            clientBtn.setClickable(false);
            attentetxt.setText("*Connexion à un serveur...*");

            Log.d("Bluetooth", "Mode client activé. Recherche d’un serveur...");

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            BluetoothDevice device = null;
            if (!pairedDevices.isEmpty()) {
                device = pairedDevices.iterator().next();
                Log.d("Bluetooth", "Appareil appairé trouvé : " + device.getName());
            }

            if (device != null) {
                new ClientThread(device).start();
                isServer = false;
            } else {
                attentetxt.setText("Aucun appareil appairé trouvé.");
                Log.e("Bluetooth", "Erreur : aucun appareil appairé trouvé.");
            }
        });
    }

    private class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public ServerThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyServer", MY_UUID);
                Log.d("Bluetooth", "Socket serveur prêt.");
            } catch (IOException e) {
                Log.e("Bluetooth", "Erreur lors de la création du socket serveur.", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    Log.d("Bluetooth", "Client connecté !");
                } catch (IOException e) {
                    Log.e("Bluetooth", "Erreur lors de l’acceptation de la connexion.", e);
                    break;
                }

                if (socket != null) {
                    transferData = new TransferData(socket);
                    Intent intent = new Intent(MainActivity.this, ServeurActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        }
    }

    private class ClientThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ClientThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d("Bluetooth", "Socket client créé.");
            } catch (IOException e) {
                Log.e("Bluetooth", "Erreur lors de la création du socket client.", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d("Bluetooth", "Connexion réussie avec le serveur.");
                transferData = new TransferData(mmSocket);
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(intent);
            } catch (IOException connectException) {
                Log.e("Bluetooth", "Échec de la connexion.", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("Bluetooth", "Erreur lors de la fermeture du socket client.", closeException);
                }
            }
        }
    }

    public class TransferData extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public TransferData(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d("Bluetooth", "Flux de données établis.");
            } catch (IOException e) {
                Log.e("Bluetooth", "Erreur lors de la récupération des flux.", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String message = new String(buffer, 0, bytes);
                    Log.d("Bluetooth", "Message reçu : " + message);
                } catch (IOException e) {
                    Log.e("Bluetooth", "Erreur de lecture.", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                Log.d("Bluetooth", "Message envoyé.");
            } catch (IOException e) {
                Log.e("Bluetooth", "Erreur d’écriture.", e);
            }
        }
    }
}