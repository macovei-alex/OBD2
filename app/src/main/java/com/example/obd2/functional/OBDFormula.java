package com.example.obd2.functional;

import com.example.obd2.model.OBDData;

@FunctionalInterface
public interface OBDFormula {

    float apply(OBDData data);
}
