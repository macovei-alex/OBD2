package com.example.obd2.model;

import java.util.UUID;

public abstract class OBDConstants {

    public static final UUID SPP_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String RESET_ADAPTER_AT = "ATZ\r";
    public static final String SET_CUSTOM_HEADER_AT = "ATSH7E0\r";
    public static final float AIR_FUEL_RATIO_DIESEL = 14.7f;
    public static final float FUEL_DENSITY_DIESEL = 832;
    public static final int SECONDS_IN_HOUR = 3600;


    private OBDConstants() {}
}
