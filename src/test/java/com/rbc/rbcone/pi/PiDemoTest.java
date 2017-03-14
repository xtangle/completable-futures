package com.rbc.rbcone.pi;

import com.rbc.rbcone.pi.TestUtils.PiCalcFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PiDemoTest {

    private final int DECIMAL_DIGITS = 100_000;

    private final Map<String, PiCalc> piCalcs =
            Stream.of(Machin.class, MachinParallel.class, Chudnovsky.class, BrentSalamin.class)
                    .map(PiCalcFactory::new)
                    .collect(Collectors.toMap(
                            PiCalcFactory::toString,
                            piCalcFactory -> piCalcFactory.create(DECIMAL_DIGITS)
                    ));

    @Test
    public void testRunAllInParallel_WaitForAllResults() {
        long start = System.nanoTime();

        CompletableFuture[] futures = piCalcs.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(
                        () -> new ImmutablePair<>(
                                entry.getKey(),
                                entry.getValue().compute()
                        )
                ))
                .map(resultFuture -> resultFuture.thenAccept(
                        result -> {
                            System.out.println(String.format("%s completed in %dms (%s)",
                                    result.getLeft(),
                                    (System.nanoTime() - start) / 1_000_000,
                                    result.getRight()
                            ));
                        }
                ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        long end = System.nanoTime();
        System.out.println(String.format("\nAll pi calculators finished. Total execution time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testRunAllInParallel_GetFirstResult() throws InterruptedException {
        long start = System.nanoTime();

        CompletableFuture[] futures = piCalcs.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(
                        () -> new ImmutablePair<>(
                                entry.getKey(),
                                entry.getValue().compute()
                        )
                ))
                .map(resultFuture -> resultFuture.thenAccept(
                        result -> {
                            System.out.println(String.format("%s completed in %dms (%s)",
                                    result.getLeft(),
                                    (System.nanoTime() - start) / 1_000_000,
                                    result.getRight()
                            ));
                        }
                ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.anyOf(futures).join();

        Stream.of(futures).forEach(future -> future.cancel(true));

        long end = System.nanoTime();
        System.out.println(String.format("\nFirst pi calculator finished. Total execution time: %dms", (end - start) / 1_000_000));

        Thread.sleep(20000);
    }
}
