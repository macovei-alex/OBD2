package com.example.obd2.model;

import com.example.obd2.exceptions.OBDSessionException;

public interface OBDSession {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean tryOpen();

    void close();

    boolean isOpen();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    void write(String string) throws OBDSessionException;

    String read() throws OBDSessionException;
}
