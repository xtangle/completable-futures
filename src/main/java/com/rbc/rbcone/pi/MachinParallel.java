package com.rbc.rbcone.pi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class MachinParallel extends PiCalc {

    public MachinParallel(int decimalDigits) {
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

    private int numberOfTerms(int x, int d) {
        return (int) Math.ceil(d / (2 * Math.log10(x))) + 1;
    }

    /**
     * Recommend heap memory size ~ 20g and chunkSize ~ 24k for 1 mil digits
     * This can be set via the JVM option (eg: -Xmx20g)
     */
    private BigDecimal arctanRecip(int x, int n) {
        int precision = decimalDigits + 10;
        BigDecimal pow = BigDecimal.ONE.divide(new BigDecimal(x), precision, ROUND);
        if (n == 0) {
            return pow;
        }

        final int chunkSize = 24000;
        final int numberOfChunks = ((n - 1) / chunkSize) + 1;
        final BigDecimal bd_x = new BigDecimal(x);
        final BigDecimal sqr = bd_x.multiply(bd_x);
        final BigDecimal sqrPowChunkSize = sqr.pow(chunkSize);

        int start, end, dist;
        BigDecimal firstPow = bd_x.negate();
        List<BigDecimal> chunkSums = new ArrayList<>();
        BigDecimal nextPowFactor;
        SumTask sumTask;

        for (int chunk = 0; chunk < numberOfChunks; chunk++) {
            start = chunk * chunkSize + 1;
            end = Math.min(start + chunkSize - 1, n);
            dist = end - start + 1;
            nextPowFactor = dist == chunkSize ?
                    sqrPowChunkSize : sqr.pow(end - start + 1);
            sumTask = new SumTask(start, end, precision, sqr, firstPow);
            chunkSums.add(ForkJoinPool.commonPool().invoke(sumTask));

            firstPow = firstPow.divide(nextPowFactor, precision, ROUND);
            if ((dist & 1) != 0) {
                firstPow = firstPow.negate();
            }
        }

        Stream<BigDecimal> sumStream = (numberOfChunks > 24) ?
                chunkSums.parallelStream() : chunkSums.stream();
        return sumStream.reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private class SumTask extends RecursiveTask<BigDecimal> {

        static final int SEQUENTIAL_THRESHOLD = 3000;

        private int start;
        private int end;
        private int precision;
        private BigDecimal sqr;
        private BigDecimal firstPow;

        private SumTask(int start, int end, int precision, BigDecimal sqr, BigDecimal firstPow) {
            this.start = start;
            this.end = end;
            this.precision = precision;
            this.sqr = sqr;
            this.firstPow = firstPow;
        }

        private BigDecimal sumTerms(int start, int end, List<BigDecimal> pows) {
            return IntStream.rangeClosed(start, end)
                    .parallel()
                    .boxed()
                    .map(i -> pows.get(i - start)
                            .divide(new BigDecimal(i * 2 - 1), precision, ROUND))
                    .reduce(BigDecimal::add)
                    .orElse(null);
        }

        private List<BigDecimal> getPowers(int n, BigDecimal sqr, BigDecimal lastPow) {
            BigDecimal pow = lastPow;
            List<BigDecimal> pows = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                pow = pow.divide(sqr, precision, ROUND).negate();
                pows.add(pow);
            }
            return pows;
        }

        @Override
        protected BigDecimal compute() {
            if (end - start + 1 <= SEQUENTIAL_THRESHOLD) {
                return sumTerms(start, end, getPowers(end - start + 1, sqr, firstPow));
            } else {
                int mid = start - 1 + (end - start + 1) / 2;
                int dist = mid - start + 1;
                BigDecimal midPow = firstPow.divide(sqr.pow(dist), precision, ROUND);
                if ((dist & 1) != 0) {
                    midPow = midPow.negate();
                }
                SumTask left = new SumTask(start, mid, precision, sqr, firstPow);
                SumTask right = new SumTask(mid + 1, end, precision, sqr, midPow);
                left.fork();
                BigDecimal rightAns = right.compute();
                BigDecimal leftAns  = left.join();
                return leftAns.add(rightAns);
            }
        }
    }
}
