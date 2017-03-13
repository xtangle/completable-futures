import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class FutureTest {

    private String getImageData(String imageName, long loadTime) throws InterruptedException {
        System.out.println(String.format("Started getting image data for %s", imageName));
        Thread.sleep(loadTime);
        System.out.println(String.format("Finished getting image data for %s", imageName));
        return "Image data for " + imageName;
    }

    private List<String> images = Arrays.asList(
            "Cat",
            "Dog",
            "Elephant",
            "Fox"
    );

    @Test
    public void testSync_LoadAll() {
        long start = System.nanoTime();

        images.forEach(image -> {
            try {
                String imageData = getImageData(image, 1000);
                System.out.println(String.format("Loaded image: %s", imageData));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        long end = System.nanoTime();
        long totalTime = (end - start) / 1_000_000;

        System.out.println(String.format("Finished loading all images. Total time: %dms", totalTime));
    }

    @Test
    public void testAsync() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<String> future = executor.submit(new Callable<String>() {
            public String call() throws Exception {
                return getImageData("Cat", 8000);
            }
        });

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        System.out.println("While waiting, we can do other stuff...");
        Thread.sleep(5000);
        System.out.println("Finished doing other stuff");

        try {
            System.out.println("Blocked until we get image data...");

            future.get();

            assertTrue(future.isDone());
            assertFalse(future.isCancelled());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCancel() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<String> future = executor.submit(() ->
                getImageData("Cat", 10000)
        );

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        Thread.sleep(5000);

        System.out.println("Cancelling task...");
        future.cancel(true);

        assertTrue(future.isDone());
        assertTrue(future.isCancelled());

        System.out.println("Done");

        Thread.sleep(6000);
    }

    @Test
    public void testException() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<String> future = executor.submit(() ->
            getImageData("Cat", -10000)
        );

        Thread.sleep(5000);

        try {
            future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());

        System.out.println("Done");
    }

    @Test
    public void testTimeout() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<String> future = executor.submit(() ->
            getImageData("Cat", 9999999999999L)
        );

        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        System.out.println("Done");
    }

    @Test
    public void testAsync_LoadAll() {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<String>> futures = new ArrayList<>();

        long start = System.nanoTime();

        images.forEach(image -> {
            Future<String> future = executor.submit(() ->
                getImageData(image, 1000)
            );
            futures.add(future);
        });

        futures.forEach(future -> {
            try {
                String imageData = future.get();
                System.out.println(String.format("Loaded image: %s", imageData));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        long end = System.nanoTime();
        long totalTime = (end - start) / 1_000_000;
        System.out.println(String.format("Finished loading all images. Total time: %dms", totalTime));
    }

}
