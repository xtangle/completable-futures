package com.rbc.rbcone.pi;

import java.math.BigDecimal;

public class Machin extends PiCalc {

    public Machin(int decimalDigits) {
        super(decimalDigits);
    }

    @SuppressWarnings("Duplicates")
    @Override
    BigDecimal computeImpl() {
        int n_5 = numberOfTerms(5, decimalDigits);
        int n_239 = numberOfTerms(239, decimalDigits);
        BigDecimal bd_4 = new BigDecimal(4);
        BigDecimal term_1 = arctanRecip(5, n_5);
        BigDecimal term_2 = arctanRecip(239, n_239);
        return bd_4.multiply(bd_4.multiply(term_1).subtract(term_2));
    }

    /**
     * Determines, approximately, how many terms of the power series expansion
     * of arctan(x) we need to calculate to achieve accuracy of up to n decimal
     * digits.
     */
    private int numberOfTerms(int x, int d) {
        return (int) Math.ceil(d / (2 * Math.log10(x))) + 1;
    }

    /**
     * Computes arctan(1/x) using the formula, up to n terms:
     * arctan(1/x) = 1/x - 1/3x^3 + 1/5x^5 - 1/7x^7 + ...
     */
    private BigDecimal arctanRecip(int x, int n) {
        int precision = decimalDigits + 10;
        BigDecimal bd_x = new BigDecimal(x);
        BigDecimal pow = BigDecimal.ONE.divide(bd_x, precision, ROUND);
        if (n <= 1) {
            return pow;
        }
        BigDecimal sqr = bd_x.multiply(bd_x);
        BigDecimal result = pow;
        int c = 1;
        for (int i = 2; i <= n; i++) {
            c += 2;
            pow = pow.divide(sqr, precision, ROUND).negate();
            result = result.add(pow.divide(new BigDecimal(c), precision, ROUND));
        }
        return result;
    }
}
