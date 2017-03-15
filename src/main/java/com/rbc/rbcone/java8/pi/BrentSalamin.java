package com.rbc.rbcone.java8.pi;


import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;

public class BrentSalamin extends PiCalc {

    public BrentSalamin(int decimalDigits) {
        super(decimalDigits);
    }

    public BrentSalamin(int decimalDigits, boolean verbose) {
        super(decimalDigits, verbose);
    }

    /**
     * Uses the Brent-Salamin formula to calculate digits of pi
     * See https://en.wikipedia.org/wiki/Gauss%E2%80%93Legendre_algorithm
     */
    @Override
    BigDecimal computeImpl() {
        int precision = decimalDigits + 3;
        int n = numberOfIterations(decimalDigits);

        BigDecimal a = ONE;
        BigDecimal b = ONE.divide(sqrt(BigDecimal.valueOf(2), precision), precision, ROUND);
        BigDecimal t = BigDecimal.valueOf(0.25);
        BigDecimal p = ONE;

        BigDecimal a_i, b_i, t_i;

        for (int i = 1; i <= n; i++) {
            a_i = a.add(b).divide(TWO, precision, ROUND);
            b_i = sqrt(a.multiply(b), precision);
            t_i = t.subtract(p.multiply(a.subtract(a_i).pow(2)));

            a = a_i;
            b = b_i;
            t = t_i;
            p = p.add(p);
        }

        return (a.add(b).pow(2))
                .divide(BigDecimal.valueOf(4).multiply(t), precision, ROUND);
    }

    private int numberOfIterations(int d) {
        int n = 1;
        long m = 2;
        long pow = 2;
        while (d > m) {
            pow *= 2;
            m += pow;
            n += 1;
        }
        return n;
    }

}
