package com.rbc.rbcone.image;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static com.rbc.rbcone.image.TestUtils.delay;
import static com.rbc.rbcone.image.TestUtils.getImageData;
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
                ImageData imageData = getImageData(imageName);
                System.out.println(String.format("Loaded image: %s\n", imageData));
        });

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testFuture() {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<ImageData> future = executor.submit(new Callable<ImageData>() {
            public ImageData call() throws Exception {
                return getImageData("Cat", 8000);
            }
        });

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        System.out.println("Do other stuff...");
        delay(4000);
        System.out.println("Waiting for image data");

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
    public void testCancel() {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<ImageData> future = executor.submit(() ->
                getImageData("Cat", 8000)
        );

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        delay(4000);

        System.out.println("Cancelling task...");
        future.cancel(true);

        assertTrue(future.isDone());
        assertTrue(future.isCancelled());

        System.out.println("Done");

        delay(6000);
    }

    @Test
    public void testException() {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<ImageData> future = executor.submit(() ->
                getImageData("Cat", -10000)
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

        Future<ImageData> future = executor.submit(() ->
                getImageData("Cat", 9999999999999L)
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
        List<Future<ImageData>> futures = new ArrayList<>();

        long start = System.nanoTime();

        imageNames.forEach(imageName -> {
            Future<ImageData> future = executor.submit(() ->
                    getImageData(imageName)
            );
            futures.add(future);
        });

        futures.forEach(future -> {
            try {
                ImageData imageData = future.get();
                System.out.println(String.format("Loaded image: %s", imageData));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", (end - start) / 1_000_000));
    }

    @Test
    public void testParallelStream_LoadAll() {
        long start = System.nanoTime();

        imageNames.parallelStream()
                .map(TestUtils::getImageData)
                .map(imageData -> String.format("Loaded image: %s", imageData))
                .forEach(System.out::println);

        long end = System.nanoTime();
        System.out.println(String.format("\nFinished loading all images. Total time: %dms", (end - start) / 1_000_000));
    }

}
