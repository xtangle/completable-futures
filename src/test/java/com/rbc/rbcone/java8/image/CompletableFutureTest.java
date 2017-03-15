package com.rbc.rbcone.java8.image;

import com.rbc.rbcone.java8.util.CustomException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.rbc.rbcone.java8.image.ImageService.getRawImage;
import static com.rbc.rbcone.java8.util.ThreadUtils.createDaemonThreadPool;
import static com.rbc.rbcone.java8.util.ThreadUtils.delay;
import static com.rbc.rbcone.java8.util.ThreadUtils.getTimeSince;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class CompletableFutureTest {

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

    @Test
    public void testCompletableFuture() {
        CompletableFuture<RawImage> imageDataFuture = new CompletableFuture<>();

        new Thread(() -> {
            RawImage rawImage = getRawImage("Cat", 8000);
            imageDataFuture.complete(rawImage);
        }).start();

        assertFalse(imageDataFuture.isDone());
        assertFalse(imageDataFuture.isCancelled());

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        try {
            RawImage rawImage = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", rawImage));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(imageDataFuture.isDone());
        assertFalse(imageDataFuture.isCancelled());

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_SupplyAsync() {
        CompletableFuture<RawImage> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getRawImage("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        try {
            RawImage rawImage = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", rawImage));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_Complete() {
        CompletableFuture<RawImage> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getRawImage("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        imageDataFuture.complete(new RawImage("Dog", "dog"));

        try {
            RawImage rawImage = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", rawImage));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_CompleteExceptionally() {
        CompletableFuture<RawImage> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getRawImage("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        imageDataFuture.completeExceptionally(new CustomException("An exception has been thrown!"));

        assertTrue(imageDataFuture.isCompletedExceptionally());

        try {
            RawImage rawImage = imageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", rawImage));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_Exceptionally() {
        CompletableFuture<RawImage> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getRawImage("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        CompletableFuture<RawImage> newImageDataFuture =
                imageDataFuture.exceptionally(throwable -> new RawImage("Dog", "dog"));

        // imageDataFuture.completeExceptionally(new CustomException("Cats are not allowed!"));
        // assertTrue(newImageDataFuture.isDone());

        try {
            RawImage rawImage = newImageDataFuture.get();
            System.out.println(String.format("Loaded image: %s", rawImage));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_Join() {
        CompletableFuture<RawImage> imageDataFuture =
                CompletableFuture.supplyAsync(() -> getRawImage("Cat", 8000));

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        imageDataFuture.completeExceptionally(new CustomException("An exception has been thrown!"));

        assertTrue(imageDataFuture.isCompletedExceptionally());

        RawImage rawImage = imageDataFuture.join();
        System.out.println(String.format("Loaded image: %s", rawImage));

        System.out.println("Done");
    }

    @Test
    public void testCompletableFuture_LoadAll() {
        long start = System.nanoTime();

        List<CompletableFuture<RawImage>> imageDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getRawImage(imageName)
                        ))
                        .collect(Collectors.toList());

        imageDataFutures.stream()
                .map(CompletableFuture::join)
                .map(rawImage -> String.format("Loaded raw image: %s", rawImage))
                .forEach(System.out::println);

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testParallelStream_LoadAll() {
        long start = System.nanoTime();

        imageNames.parallelStream()
                .map(ImageService::getRawImage)
                .map(rawImage -> String.format("Loaded raw image: %s", rawImage))
                .forEach(System.out::println);

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testCompletableFuture_LoadAll_UsingCustomThreadPool() {
        // N_threads = N_cpu * U_cpu * (1 + W/C)
        // int numThreads = Runtime.getRuntime().availableProcessors() * (1 + 99);
        Executor executor = createDaemonThreadPool(Math.min(imageNames.size(), 100));

        long start = System.nanoTime();

        List<CompletableFuture<RawImage>> imageDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getRawImage(imageName),
                                executor
                        ))
                        .collect(Collectors.toList());

        imageDataFutures.stream()
                .map(CompletableFuture::join)
                .map(rawImage -> String.format("Loaded raw image: %s", rawImage))
                .forEach(System.out::println);

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

}
