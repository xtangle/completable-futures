package com.rbc.rbcone.image;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rbc.rbcone.image.TestUtils.*;

@SuppressWarnings("Duplicates")
public class CompletableFutureTest2 {

    private final List<String> imageNames = Arrays.asList(
            "Cat",
            "Dog",
            "Elephant",
            "Fox",
            "Giraffe",
            "Horse",
            "Iguana",
            "Jackrabbit",
            "Kangaroo"
    );

    private final Executor executor =
            createDaemonThreadPool(Math.min(imageNames.size(), 100));

    @Test
    public void testSync_LoadAllTransformedData() {
        long start = System.nanoTime();

        imageNames.stream()
                .map(TestUtils::getImageData)
                .map(TestUtils::getTransformedData)
                .map(transformedData -> String.format("Loaded transformed image: %s\n", transformedData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        System.out.println(String.format("Finished loading all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testParallelStream_LoadAllTransformedData() {
        long start = System.nanoTime();

        imageNames.parallelStream()
                .map(TestUtils::getImageData)
                .map(TestUtils::getTransformedData)
                .map(transformedData -> String.format("Loaded transformed image: %s", transformedData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testCompletableFuture_LoadAllTransformedData() {
        long start = System.nanoTime();

        List<CompletableFuture<TransformedData>> transformedDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getImageData(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedData(imageData), executor)
                        ))
                        .collect(Collectors.toList());

        transformedDataFutures.stream()
                .map(CompletableFuture::join)
                .map(transformedData -> String.format("Loaded transformed image: %s", transformedData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testCompletableFuture_ResizeAllTransformedData() {
        long start = System.nanoTime();

        List<CompletableFuture<ResizedData>> transformedDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getImageData(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedData(imageData), executor)
                        ))
                        .map(transformedDataFuture -> transformedDataFuture.thenApply(
                                TestUtils::getResizedData
                        ))
                        .collect(Collectors.toList());

        transformedDataFutures.stream()
                .map(CompletableFuture::join)
                .map(resizedData -> String.format("Loaded resized image: %s", resizedData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testCompletableFuture_RenderAllResizedData() {
        long start = System.nanoTime();

        CompletableFuture[] futures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getImageData(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedData(imageData), executor)
                        ))
                        .map(transformedDataFuture -> transformedDataFuture.thenApply(
                                TestUtils::getResizedData
                        ))
                        .map(transformedDataFuture -> transformedDataFuture.thenAccept(
                                TestUtils::renderScaledImage
                        ))
                        .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished rendering all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testCompletableFuture_CombineTransformedData() {
        long start = System.nanoTime();

        CompletableFuture<Void> future =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getImageData(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedData(imageData), executor)
                        ))
                        .reduce(
                                CompletableFuture.completedFuture(new TransformedData(null, "")),
                                (tf1, tf2) -> tf1.thenCombineAsync(tf2, TestUtils::combineTransformedData, executor)
                        )
                        .thenApply(TestUtils::getResizedData)
                        .thenAccept(TestUtils::renderScaledImage);

        future.join();

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished rendering all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testCompletableFuture_CombineTransformedData_TriggerExample() {
        long start = System.nanoTime();

        CompletableFuture<TransformedData> closing = new CompletableFuture<>();
        Stream<String> imageNameStream = imageNames.stream();

        CompletableFuture<Void> future =
                imageNameStream
                        .onClose(() -> closing.complete(new TransformedData(null, "")))
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getImageData(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedData(imageData), executor)
                        ))
                        .reduce(
                                closing,
                                (tf1, tf2) -> tf1.thenCombine(tf2, TestUtils::combineTransformedData)
                        )
                        .thenApply(TestUtils::getResizedData)
                        .thenAccept(TestUtils::renderScaledImage);

        delay(8000);

        System.out.println("Closing the stream...");
        imageNameStream.close();

        future.join();

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished rendering all images. Total time: %dms", (end - start) / 1_000_000));
    }

}
