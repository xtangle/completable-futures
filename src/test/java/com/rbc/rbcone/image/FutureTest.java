package com.rbc.rbcone.image;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.rbc.rbcone.image.ImageService.getRawImage;
import static com.rbc.rbcone.util.ThreadUtils.delay;
import static com.rbc.rbcone.util.ThreadUtils.getTimeSince;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class FutureTest {

    private final List<String> imageNames = Arrays.asList(
            "Cat",
            "Dog",
            "Elephant",
            "Fox"
    );

    @Test
    public void testSync_LoadAll() {
        long start = System.nanoTime();

        imageNames.forEach(imageName -> {
                RawImage rawImage = getRawImage(imageName);
                System.out.println(String.format("Loaded raw image: %s\n", rawImage));
        });

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testFuture() {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<RawImage> future = executor.submit(new Callable<RawImage>() {
            public RawImage call() throws Exception {
                return getRawImage("Cat", 8000);
            }
        });

        // assertFalse(future.isDone());
        // assertFalse(future.isCancelled());

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

        RawImage rawImage = null;
        try {
            rawImage = future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // assertTrue(future.isDone());
        // assertFalse(future.isCancelled());

        System.out.println(String.format("Done. Image data: %s", rawImage));
    }

    @Test
    public void testCancel() {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<RawImage> future = executor.submit(() ->
                getRawImage("Cat", 8000)
        );

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        delay(4000);

        System.out.println("Cancelling task...");
        future.cancel(false);

        assertTrue(future.isDone());
        assertTrue(future.isCancelled());

        System.out.println("Done");

        delay(6000);
    }

    @Test
    public void testException() {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<RawImage> future = executor.submit(() ->
                getRawImage("Cat", -10000)
        );

        delay(4000);

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());

        System.out.println("Done");
    }

    @Test
    public void testTimeout() {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<RawImage> future = executor.submit(() ->
                getRawImage("Cat", 9999999999999L)
        );

        try {
            future.get(4, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        System.out.println("Done");
    }

    @Test
    public void testAsync_LoadAll() {
        ExecutorService executor = Executors.newCachedThreadPool();

        long start = System.nanoTime();

        List<Future<RawImage>> futures =
                imageNames.stream()
                    .map(imageName -> executor.submit(
                            () -> getRawImage(imageName)
                    ))
                    .collect(Collectors.toList());

        futures.forEach(future -> {
            try {
                RawImage rawImage = future.get();
                System.out.println(String.format("Loaded image: %s", rawImage));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testParallelStream_LoadAll() {
        long start = System.nanoTime();

        imageNames.parallelStream()
                .map(ImageService::getRawImage)
                .map(rawImage -> String.format("Loaded image: %s", rawImage))
                .forEach(System.out::println);

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

}
