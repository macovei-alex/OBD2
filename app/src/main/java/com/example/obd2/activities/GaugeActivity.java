package com.example.obd2.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.obd2.R;
import com.example.obd2.functional.RunnableExceptionCallback;
import com.example.obd2.model.OBDBluetoothSession;
import com.example.obd2.model.OBDCommand;
import com.example.obd2.model.OBDCommands;
import com.example.obd2.model.OBDCommunicationRunnable;
import com.example.obd2.model.OBDParser;
import com.example.obd2.model.OBDSession;
import com.example.obd2.model.OBDSettings;
import com.example.obd2.view.GaugeView;
import com.example.obd2.view.OBDCommandView;

import java.util.Map;

public class GaugeActivity extends AppCompatActivity {

    private static final String TAG = GaugeActivity.class.getSimpleName();

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private OBDSession session = null;
    private OBDCommunicationRunnable communicationRunnable;

    private Map<OBDCommand, OBDCommandView> commandToViewMap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.gauge_activity);

        initGauges();

        final Thread communicationThread = makeCommunicationThread();
        if (communicationThread == null) {
            Log.e(TAG, "Failed to create communication thread");
            finish();
            return;
        }
        communicationThread.start();
    }


    @Override
    protected void onDestroy() {
        if (communicationRunnable != null) {
            communicationRunnable.stop();
        }
        if (session != null) {
            session.close();
        }
        super.onDestroy();
        Log.d(TAG, "Gauge activity destroyed");
    }


    private void initGauges() {
        commandToViewMap = Map.of(
                OBDCommands.SPEED, findViewById(R.id.gvSpeed),
                OBDCommands.RPM, findViewById(R.id.gvRPM),
                OBDCommands.ENGINE_TEMPERATURE, findViewById(R.id.gvEngineTemperature),
                OBDCommands.THROTTLE_POSITION, findViewById(R.id.gvThrottlePosition),
                OBDCommands.MASS_AIRFLOW, findViewById(R.id.gvMassAirflow),
                OBDCommands.INSTANTANEOUS_FUEL_CONSUMPTION, findViewById(R.id.gvInstantFuelConsumption)
        );
        commandToViewMap.forEach((command, view) -> {
            if (!(view instanceof GaugeView)) {
                return;
            }
            GaugeView gauge = (GaugeView) view;
            gauge.setMinValue(command.getMinValue());
            gauge.setMaxValue(command.getMaxValue());
            gauge.setUnitOfMeasure(command.getUnitOfMeasure());
        });
    }


    private @Nullable Thread makeCommunicationThread() {
        if (session != null && session.isOpen()) {
            return null;
        }

        final Intent intent = getIntent();
        final BluetoothDevice device = intent.getParcelableExtra("bluetoothDevice");
        if (device == null) {
            Log.e(TAG, "No Bluetooth device found in intent");
            return null;
        }

        final OBDSettings[] settings = new OBDSettings[]{
                OBDSettings.DISABLE_ECHO,
                OBDSettings.DISABLE_LINEFEED,
                OBDSettings.DISABLE_SPACES,
                OBDSettings.DISABLE_HEADERS,
                OBDSettings.PROTOCOL_6
        };

        session = new OBDBluetoothSession(this, device, settings);
        if (!session.tryOpen()) {
            Log.e(TAG, "Failed to open session");
            session = null;
            return null;
        }
        Log.i(TAG, "Session opened successfully");

        final OBDParser parser = new OBDParser(settings);

        final RunnableExceptionCallback callback = (t) -> {
            Log.e(TAG, "Uncaught exception in communication thread", t);
            final String text = "Error in communication with the adapter";
            uiHandler.post(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
            uiHandler.postDelayed(this::finish, 2000);
        };

        communicationRunnable = new OBDCommunicationRunnable(session, commandToViewMap, parser,
                callback, 0);
        final Thread communicationThread = new Thread(communicationRunnable);

        return communicationThread;
    }
}
