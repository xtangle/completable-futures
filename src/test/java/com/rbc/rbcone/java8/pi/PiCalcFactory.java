package com.rbc.rbcone.java8.pi;

import java.lang.reflect.InvocationTargetException;

public class PiCalcFactory {

    private Class<? extends PiCalc> piCalcClass;

    PiCalcFactory(Class<? extends PiCalc> clazz) {
        piCalcClass = clazz;
    }

    PiCalc create(int decimalDigits) {
        return this.create(decimalDigits, false);
    }

    PiCalc create(int decimalDigits, boolean verbose) {
        PiCalc piCalc = null;
        try {
            piCalc = piCalcClass.getConstructor(int.class, boolean.class)
                    .newInstance(decimalDigits, verbose);
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return piCalc;
    }

    @Override
    public String toString() {
        return piCalcClass.getSimpleName();
    }

}
