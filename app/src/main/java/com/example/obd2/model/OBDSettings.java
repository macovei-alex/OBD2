package com.example.obd2.model;


public enum OBDSettings {

    DISABLE_ECHO("ATE0\r"),
    ENABLE_ECHO("ATE1\r"),
    DISABLE_LINEFEED("ATL0\r"),
    ENABLE_LINEFEED("ATL1\r"),
    DISABLE_SPACES("ATS0\r"),
    ENABLE_SPACES("ATS1\r"),
    DISABLE_HEADERS("ATH0\r"),
    ENABLE_HEADERS("ATH1\r"),
    PROTOCOL_6("ATSP6\r");


    private final String code;


    OBDSettings(String code) {
        this.code = code;
    }


    public String getCode() {
        return code;
    }
}
