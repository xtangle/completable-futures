package com.rbc.rbcone.image;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.rbc.rbcone.image.TestUtils.createDaemonThreadPool;
import static com.rbc.rbcone.image.TestUtils.delay;
import static com.rbc.rbcone.image.TestUtils.getImageData;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class CompletableFutureTest {

    private List<String> imageNames = Arrays.asList(
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

    @Test
    public void testCompletableFuture() {
        CompletableFuture<ImageData> imageDataFuture = new CompletableFuture<>();

        new Thread(() -> {
            ImageData imageData = getImageData("Cat", 8000);
            imageDataFuture.complete(imageData);
        }).start();

        assertFalse(imageDataFuture.isDone());
        assertFalse(imageDataFuture.isCancelled());

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        try {
            ImageData imageData = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", imageData));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(imageDataFuture.isDone());
        assertFalse(imageDataFuture.isCancelled());

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_SupplyAsync() {
        CompletableFuture<ImageData> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getImageData("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        try {
            ImageData imageData = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", imageData));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_Complete() {
        CompletableFuture<ImageData> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getImageData("Cat", 8000));


        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        imageDataFuture.complete(new ImageData("Dog", "dog"));

        try {
            ImageData imageData = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", imageData));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_CompleteExceptionally() {
        CompletableFuture<ImageData> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getImageData("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        imageDataFuture.completeExceptionally(new TestUtils.CustomException("An exception has been thrown!"));

        assertTrue(imageDataFuture.isCompletedExceptionally());

        try {
            ImageData imageData = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", imageData));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_Join() {
        CompletableFuture<ImageData> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getImageData("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        imageDataFuture.completeExceptionally(new TestUtils.CustomException("An exception has been thrown!"));

        assertTrue(imageDataFuture.isCompletedExceptionally());

        ImageData imageData = imageDataFuture.join();
        System.out.println(String.format("Loaded image: %s", imageData));

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_LoadAll() {
        long start = System.nanoTime();

        List<CompletableFuture<ImageData>> imageDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getImageData(imageName)
                        ))
                        .collect(Collectors.toList());

        imageDataFutures.stream()
                .map(CompletableFuture::join)
                .map(imageData -> String.format("Loaded image: %s", imageData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        long totalTime = (end - start) / 1_000_000;
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", totalTime));
    }

    @Test
    public void testParallelStream_LoadAll() {
        long start = System.nanoTime();

        imageNames.parallelStream()
                .map(TestUtils::getImageData)
                .map(imageData -> String.format("Loaded image: %s", imageData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        long totalTime = (end - start) / 1_000_000;
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", totalTime));
    }

    @Test
    public void testCompletableFuture_LoadAll_UsingCustomThreadPool() {
        // N_threads = N_cpu * U_cpu * (1 + W/C)
        // int numThreads = Runtime.getRuntime().availableProcessors() * (1 + 99);
        Executor executor = createDaemonThreadPool(Math.min(imageNames.size(), 100));

        long start = System.nanoTime();

        List<CompletableFuture<ImageData>> imageDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getImageData(imageName),
                                executor
                        ))
                        .collect(Collectors.toList());

        imageDataFutures.stream()
                .map(CompletableFuture::join)
                .map(imageData -> String.format("Loaded image: %s", imageData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        long totalTime = (end - start) / 1_000_000;
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", totalTime));
    }

}
