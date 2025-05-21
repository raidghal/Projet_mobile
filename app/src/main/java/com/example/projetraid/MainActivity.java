package com.example.projetraid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    public static TransferData transferData;

    public static boolean isServer;

    private static final UUID MY_UUID = UUID.fromString("8b5a1316-e19e-11ec-8fea-0242ac120002");
    private static final int REQUEST_BT_PERMISSIONS = 1000;
    private static final String SERVER_MAC_ADDRESS = "18:87:40:78:8d:d9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothPermissions();

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

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Bluetooth requise", Toast.LENGTH_SHORT).show();
                return;
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            BluetoothDevice targetDevice = null;

            for (BluetoothDevice device : pairedDevices) {
                Log.d("Bluetooth", "Appairé : " + device.getName() + " (" + device.getAddress() + ")");
                if (device.getAddress().equalsIgnoreCase(SERVER_MAC_ADDRESS)) {
                    targetDevice = device;
                    Log.d("Bluetooth", "Appareil serveur trouvé : " + device.getName());
                    break;
                }
            }

            if (targetDevice != null) {
                new ClientThread(targetDevice).start();
                isServer = false;
            } else {
                attentetxt.setText("Appareil serveur non trouvé !");
                Log.e("Bluetooth", "Aucun appareil appairé avec l'adresse MAC " + SERVER_MAC_ADDRESS);
            }
        });
    }

    private void checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN
                    }, REQUEST_BT_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BT_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Bluetooth accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Bluetooth refusée. L'application ne peut pas fonctionner.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public ServerThread() {
            BluetoothServerSocket tmp = null;
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                try {
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyServer", MY_UUID);
                    Log.d("Bluetooth", "Socket serveur prêt.");
                } catch (IOException e) {
                    Log.e("Bluetooth", "Erreur lors de la création du socket serveur.", e);
                }
            } else {
                Log.e("Bluetooth", "Permission refusée pour créer le socket serveur");
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
                    transferData.start(); // Important : démarre le thread
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                        startActivity(intent);
                    });

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
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                try {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                    Log.d("Bluetooth", "Socket client créé.");
                } catch (IOException e) {
                    Log.e("Bluetooth", "Erreur lors de la création du socket client.", e);
                }
            } else {
                Log.e("Bluetooth", "Permission refusée pour créer le socket client");
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Optionnel : demande dynamique pour API 31+
                return;
            }

            bluetoothAdapter.cancelDiscovery(); // Toujours annuler la découverte avant une connexion

            try {
                mmSocket.connect();
                Log.d("Bluetooth", "Connexion réussie avec le serveur.");

                // Création du thread de transfert
                transferData = new TransferData(mmSocket);
                transferData.start(); // Démarre le thread (lecture des messages)

                // Lancer l'activité de messagerie
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                    startActivity(intent);
                });


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
    /*
    // Ancienne version interne de TransferData (à ne plus utiliser)
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
    */

}
