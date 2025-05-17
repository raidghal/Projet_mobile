package com.example.projetraid;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TransferData extends Thread {
    private static TransferData instance;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public static void init(BluetoothSocket socket) throws IOException {
        if (instance == null) {
            instance = new TransferData(socket);
        }
    }

    public static TransferData getInstance() {
        return instance;
    }

    private TransferData(BluetoothSocket socket) throws IOException {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void write(byte[] buffer) {
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            Log.e("TransferData", "Erreur d’écriture", e);
        }
    }
}
