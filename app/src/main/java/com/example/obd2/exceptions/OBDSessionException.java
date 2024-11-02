package com.example.obd2.exceptions;

@SuppressWarnings("unused")
public class OBDSessionException extends RuntimeException {

    private final String sourceTage;


    public OBDSessionException(String sourceTage, String message, Exception e) {
        super(message, e);
        this.sourceTage = sourceTage;
    }


    public String getSourceTage() {
        return sourceTage;
    }


    public OBDSessionException(String message) {
        this("", message, null);
    }


    public OBDSessionException(Exception e) {
        this("", e.getMessage(), e);
    }


    public OBDSessionException(String message, Exception e) {
        this("", message, e);
    }


    public OBDSessionException(String sourceTage, String message) {
        this(sourceTage, message, null);
    }
}
