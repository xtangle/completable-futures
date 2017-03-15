package com.rbc.rbcone.pi;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rbc.rbcone.util.ThreadUtils.createDaemonThreadPool;
import static com.rbc.rbcone.util.ThreadUtils.getTimeSince;

public class PiCalcDemoTest {

    private final int DECIMAL_DIGITS = 100_000;

    private final Map<String, PiCalc> piCalcs =
            Stream.of(Machin.class, MachinParallel.class, Chudnovsky.class, BrentSalamin.class)
                    .map(PiCalcFactory::new)
                    .collect(Collectors.toMap(
                            PiCalcFactory::toString,
                            piCalcFactory -> piCalcFactory.create(DECIMAL_DIGITS, true)
                    ));

    private final Executor executor =
            createDaemonThreadPool(Math.min(piCalcs.size(), 100));

    @Test
    public void testRunAllInParallel_WaitForAllResults() {
        long start = System.nanoTime();

        CompletableFuture[] futures = piCalcs.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(
                        () -> new ImmutablePair<>(
                                entry.getKey(),
                                entry.getValue().compute()
                        ),
                        executor
                ))
                .map(resultFuture -> resultFuture.thenAccept(
                        result -> {
                            System.out.println(String.format("%s completed in %dms (pi = %s)",
                                    result.getLeft(),
                                    getTimeSince(start),
                                    result.getRight()
                            ));
                        }
                ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        System.out.println(String.format("\nAll pi calculators finished. Total execution time: %dms", getTimeSince(start)));
    }

    @Test
    public void testRunAllInParallel_GetFirstResult() throws InterruptedException {
        long start = System.nanoTime();

        CompletableFuture[] futures = piCalcs.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(
                        () -> new ImmutablePair<>(
                                entry.getKey(),
                                entry.getValue().compute()
                        ),
                        executor
                ))
                .map(resultFuture -> resultFuture.thenAccept(
                        result -> {
                            System.out.println(String.format("%s completed in %dms (pi = %s)",
                                    result.getLeft(),
                                    getTimeSince(start),
                                    result.getRight()
                            ));
                        }
                ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.anyOf(futures).join();

        Stream.of(futures).forEach(future -> future.cancel(true));

        System.out.println(String.format("\nFirst pi calculator finished. Total execution time: %dms", getTimeSince(start)));

        // Thread.sleep(20000);
    }
}
