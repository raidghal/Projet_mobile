package com.example.projetraid;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// =============================
// === Ancien code (commenté) ===
// =============================

/*
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

// =============================
// === Nouveau code corrigé ===
// =============================

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

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = inputStream.read(buffer);
                String message = new String(buffer, 0, bytes);
                Log.d("Bluetooth", "Message reçu : " + message);
                // Tu peux ici appeler un listener pour afficher côté UI si besoin
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

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
