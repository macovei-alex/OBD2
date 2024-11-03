package com.example.obd2.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.obd2.R;
import com.example.obd2.Utils;

import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView bluetoothDevicesView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothDevicesView = super.findViewById(R.id.lvBluetoothDevices);

        if (!Utils.checkBluetoothPermissions(this)) {
            final String[] permissions = {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
            requestPermissions(permissions, 100);
            return;
        }

        doStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Main activity destroyed");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Insufficient permissions 1");
                finish();
                return;
            }
        }

        doStart();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            listBluetoothDevices();
        }
    }


    private void listBluetoothDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!Utils.checkBluetoothPermissions(this)) {
            Log.i(TAG, "Insufficient permissions 2");
            return;
        }

        @SuppressLint("MissingPermission")
        BluetoothDevice[] devices = bluetoothAdapter.getBondedDevices()
                .stream()
                .sorted(Comparator.comparing(BluetoothDevice::getName))
                .toArray(BluetoothDevice[]::new);

        String[] deviceNames = Arrays.stream(devices)
                .map(BluetoothDevice::getName)
                .toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                deviceNames
        );

        bluetoothDevicesView.setAdapter(adapter);

        bluetoothDevicesView.setOnItemClickListener((parent, view, position, id) -> {
            @SuppressLint("MissingPermission")
            String deviceName = devices[position].getName();
            String deviceAddress = devices[position].getAddress();
            // Intent intent = new Intent(OBDActivity.this, OBDService.class);
            // intent.putExtra("address", itemAddr);
            // startService(intent);
            // ((TextView) OBDActivity.this.findViewById(R.id.status)).setText("Connecting ...");
            Log.d(TAG, deviceName + "/" + deviceAddress);

            Intent intent = new Intent(MainActivity.this, GaugeActivity.class);
            intent.putExtra("bluetoothDevice", devices[position]);
            startActivity(intent);
        });
    }


    @SuppressLint("MissingPermission")
    public void doStart() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported. Aborting.");
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            listBluetoothDevices();
            return;
        }

        // Prompt user to turn on Bluetooth
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (!Utils.checkBluetoothPermissions(this)) {
            Log.i(TAG, "Insufficient permissions 3");
            return;
        }

        super.startActivityForResult(enableBluetoothIntent, 101);
    }
}