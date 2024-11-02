package com.example.obd2.model;


import androidx.annotation.NonNull;

import java.util.Locale;

public class OBDData {

    public int a;
    public int b;
    public int c;
    public int d;


    public OBDData() {
        a = 0;
        b = 0;
        c = 0;
        d = 0;
    }


    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "OBDData{ a=%d, b=%d, c=%d, d=%d }", a, b, c, d);
    }
}
