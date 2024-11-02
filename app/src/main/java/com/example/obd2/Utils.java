package com.example.obd2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.Manifest;

import androidx.core.app.ActivityCompat;

public abstract class Utils {

    private static final String TAG = Utils.class.getSimpleName();


    public static String unescape(String s) {
        return s.replace("\r", "\\r")
                .replace("\n", "\\n");
    }


    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread interrupted", e);
        }
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkBluetoothPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }


    public static <T> boolean contains(T[] array, T value) {
        for (T t : array) {
            if (t.equals(value)) {
                return true;
            }
        }
        return false;
    }


    public static <T> int indexOf(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }


    public static <T> int reverseIndexOf(T[] array, T value) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }


    private Utils() {}
}
