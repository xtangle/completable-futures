package com.rbc.rbcone.java8.pi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Chudnovsky extends PiCalc {

    private static final BigInteger C = BigInteger.valueOf(640320);
    private static final BigInteger C3_OVER_24 = C.pow(3).divide(BigInteger.valueOf(24));
    private static final double DIGITS_PER_TERM =
            Math.log10(C3_OVER_24.longValueExact()) - Math.log10(72);

    public Chudnovsky(int decimalDigits) {
        super(decimalDigits);
    }

    public Chudnovsky(int decimalDigits, boolean verbose) {
        super(decimalDigits, verbose);
    }

    /**
     * Uses Chudnovsky's formula to calculate digits of pi
     * See http://www.craig-wood.com/nick/articles/pi-chudnovsky/
     */
    @Override
    BigDecimal computeImpl() {
        int precision = decimalDigits + 1;
        int n = numberOfTerms(decimalDigits);
        Triple<BigInteger> s = ForkJoinPool.commonPool()
                .invoke(new BinarySplit(0, n));
        BigDecimal D = BigDecimal.valueOf(426880)
                .multiply(sqrt(BigDecimal.valueOf(10005), precision));
        return new BigDecimal(s.q).multiply(D)
                .divide(new BigDecimal(s.t), precision, ROUND);
    }

    private int numberOfTerms(int d) {
        int n = (int) (d / DIGITS_PER_TERM) + 1;
        return (n < 8) ? 8 : n;
    }

    private class BinarySplit extends RecursiveTask<Triple<BigInteger>> {

        private static final int SEQUENTIAL_THRESHOLD = 128;
        private int a;
        private int b;

        private BinarySplit(int a, int b) {
            this.a = a;
            this.b = b;
        }

        private Triple<BigInteger> bs(int a, int b) {
            if (b - a == 1) {
                BigInteger p, q, t;
                long al = (long) a;
                if (a == 0) {
                    p = q = BigInteger.ONE;
                } else {
                    p = BigInteger.valueOf((6 * al - 5) * (2 * al - 1) * (6 * al - 1));
                    q = BigInteger.valueOf(al * al * al).multiply(C3_OVER_24);
                }
                t = p.multiply(BigInteger.valueOf(13591409 + 545140134 * al));
                if ((a & 1) != 0) {
                    t = t.negate();
                }
                return new Triple<>(p, q, t);
            } else {
                int m = (a + b) / 2;
                Triple<BigInteger> l = bs(a, m);
                Triple<BigInteger> r = bs(m, b);
                return combine(l, r);
            }
        }

        private Triple<BigInteger> combine(Triple<BigInteger> l, Triple<BigInteger> r) {
            return new Triple<>(
                    l.p.multiply(r.p),
                    l.q.multiply(r.q),
                    r.q.multiply(l.t).add(l.p.multiply(r.t))
            );
        }

        @Override
        protected Triple<BigInteger> compute() {
            if (b - a <= SEQUENTIAL_THRESHOLD) {
                return bs(a, b);
            } else {
                int m = (a + b) / 2;
                BinarySplit left = new BinarySplit(a, m);
                BinarySplit right = new BinarySplit(m, b);
                right.fork();
                Triple<BigInteger> l = left.compute();
                Triple<BigInteger> r = right.join();
                return combine(l, r);
            }
        }
    }

    private static class Triple<T> {
        private T p;
        private T q;
        private T t;

        private Triple(T p, T q, T t) {
            this.p = p;
            this.q = q;
            this.t = t;
        }
    }

}
