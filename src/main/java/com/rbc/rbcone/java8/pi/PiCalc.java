package com.rbc.rbcone.java8.pi;

import java.math.BigDecimal;

abstract class PiCalc {

    static final BigDecimal TWO = BigDecimal.valueOf(2);
    static final int ROUND = BigDecimal.ROUND_FLOOR;

    int decimalDigits;
    boolean verbose;

    PiCalc(int decimalDigits) {
        this(decimalDigits, false);
    }

    PiCalc(int decimalDigits, boolean verbose) {
        if (decimalDigits < 0) {
            throw new IllegalArgumentException("Number of decimal digits must not be negative.");
        }
        this.decimalDigits = decimalDigits;
        this.verbose = verbose;
    }

    BigDecimal compute() {
        String className = null;
        if (this.verbose) {
            className = this.getClass().getSimpleName();
            System.out.println(String.format("%s has started computing...", className));
        }
        BigDecimal result = this.computeImpl().setScale(decimalDigits, ROUND);
        if (this.verbose) {
            System.out.println(String.format("%s has finished computing", className));
        }
        return result;
    }

    abstract BigDecimal computeImpl();

    /**
     * Computes the square root of a BigDecimal to the given scale using the
     * Babylonian method (https://en.wikipedia.org/wiki/Methods_of_computing_square_roots)
     */
    static BigDecimal sqrt(BigDecimal a, final int scale) {
        BigDecimal x0 = BigDecimal.ZERO;
        BigDecimal x1 = new BigDecimal(Math.sqrt(a.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = a.divide(x0, scale, BigDecimal.ROUND_HALF_UP)
                    .add(x0)
                    .divide(TWO, scale, BigDecimal.ROUND_HALF_UP);
        }
        return x1;
    }

}
