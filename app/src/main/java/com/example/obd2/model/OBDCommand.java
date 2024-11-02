package com.example.obd2.model;


import com.example.obd2.functional.OBDFormula;

public class OBDCommand {

    private static int currentId = 0;

    private final int id;
    private final float minValue;
    private final float maxValue;
    private final String name;
    private final String unitOfMeasure;
    private final String commandString;
    private final OBDFormula formula;


    public OBDCommand(String name, float minValue, float maxValue, String unitOfMeasure, String commandString, OBDFormula formula) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unitOfMeasure = unitOfMeasure;
        this.commandString = commandString;
        this.formula = formula;
        this.id = currentId;
        currentId++;
    }


    public String getCommandString() {
        return commandString;
    }


    public float getMinValue() {
        return minValue;
    }


    public float getMaxValue() {
        return maxValue;
    }


    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }


    public float applyFormula(OBDData data) {
        return formula.apply(data);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OBDCommand)) {
            return false;
        }

        OBDCommand other = (OBDCommand) obj;
        return name.equals(other.name)
                && unitOfMeasure.equals(other.unitOfMeasure)
                && commandString.equals(other.commandString);
    }


    @Override
    public int hashCode() {
        return id;
    }
}
