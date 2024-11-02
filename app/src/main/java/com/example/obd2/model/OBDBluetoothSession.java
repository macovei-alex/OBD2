package com.example.obd2.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.obd2.Utils;
import com.example.obd2.exceptions.OBDSessionException;

import java.io.IOException;

public class OBDBluetoothSession implements OBDSession {

    private static final String TAG = OBDBluetoothSession.class.getSimpleName();
    private static final char END_STREAM_CHAR = '>';

    private final Context context;
    private final BluetoothDevice device;
    private BluetoothSocket socket = null;
    private final OBDSettings[] settings;


    public OBDBluetoothSession(Context context, BluetoothDevice device, OBDSettings[] settings) {
        this.context = context;
        this.device = device;
        this.settings = settings;
    }


    @Override
    public boolean tryOpen() {
        if (socket != null) {
            return true;
        }

        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Socket could not be initialized: insufficient permissions");
                close();
                return false;
            }

            socket = device.createRfcommSocketToServiceRecord(OBDConstants.SPP_SERVICE_UUID);
            socket.connect();
            setSettings();
        } catch (IOException | OBDSessionException e) {
            Log.e(TAG, "Failed to create socket", e);
            close();
            return false;
        }

        return true;
    }


    private void setSettings() throws OBDSessionException {
        write(OBDConstants.RESET_ADAPTER_AT);
        String response = read();

        for (OBDSettings setting : settings) {
            write(setting.getCode());
            response = read();
        }
    }


    @Override
    public void close() {
        if (socket == null) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close socket", e);
        } finally {
            socket = null;
        }
    }


    @Override
    public boolean isOpen() {
        return socket != null;
    }


    @Override
    public void write(String string) throws OBDSessionException {
        try {
            socket.getOutputStream().write(string.getBytes());
            Log.d(TAG, "Sent: " + Utils.unescape(string));
        } catch (IOException e) {
            throw new OBDSessionException(TAG, "Failed to write to socket: " + string, e);
        }
    }


    @Override
    public String read() {
        try {
            StringBuilder sb = new StringBuilder();
            int b;
            while ((b = socket.getInputStream().read()) > -1) {
                if (b == END_STREAM_CHAR) {
                    break;
                }

                sb.append((char) b);
            }

            final String str = sb.toString();

            Log.d(TAG, "Received: " + Utils.unescape(str));
            return str;

        } catch (IOException e) {
            Log.e(TAG, "Failed to read from socket", e);
            return "";
        }
    }
}
