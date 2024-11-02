package com.example.obd2.model;


public abstract class OBDCommands {

    public static final OBDCommand ENGINE_TEMPERATURE = new OBDCommand(
            "engine temperature",
            -40,
            215,
            "°C",
            "0105\r",
            (data) -> data.a - 40
    );

    public static final OBDCommand RPM = new OBDCommand(
            "engine rpm",
            0,
            16384,
            "rpm",
            "010C\r",
            (data) -> ((data.a * 256) + data.b) / 4.0f
    );

    public static final OBDCommand SPEED = new OBDCommand(
            "vehicle speed",
            0,
            255,
            "km/h",
            "010D\r",
            (data) -> data.a
    );

    public static final OBDCommand MASS_AIRFLOW = new OBDCommand(
            "mass airflow",
            0,
            655.35f,
            "g/s",
            "0110\r",
            (data) -> ((data.a * 256) + data.b) / 100.0f
    );

    public static final OBDCommand THROTTLE_POSITION = new OBDCommand(
            "throttle position",
            0,
            100,
            "%",
            "0111\r",
            (data) -> data.a * 100 / 255.0f
    );

    public static final OBDCommand INSTANTANEOUS_FUEL_CONSUMPTION = new OBDCommand(
            "instantaneous fuel consumption",
            0,
            194.2f / 5,
            "l/h",
            MASS_AIRFLOW.getCommandString(),
            (data) -> {
                float massAirflow = MASS_AIRFLOW.applyFormula(data);
                float res = massAirflow * OBDConstants.SECONDS_IN_HOUR
                        / (OBDConstants.AIR_FUEL_RATIO_DIESEL * OBDConstants.FUEL_DENSITY_DIESEL);
                return res;
            }
    );

    public static final class Opel {

        public static final OBDCommand DISTANCE_SINCE_LAST_DPF_REGENERATION = new OBDCommand(
                "distance since last dpf regeneration",
                0,
                65535,
                "km",
                "223039\r",
                (data) -> (data.a * 256) + data.b
        );

        public static final OBDCommand DPF_DIFFERENTIAL_PRESSURE = new OBDCommand(
                "dpf differential pressure",
                -128,
                127,
                "kPa",
                "2220F4\r",
                (data) -> data.a
        );

        public static final OBDCommand CALCULATED_DPF_FLOW = new OBDCommand(
                "calculated dpf flow",
                0,
                2.55f,
                "kPa",
                "2220F5\r",
                (data) -> data.a / 100.0f
        );
    }


    private OBDCommands() {
    }
}