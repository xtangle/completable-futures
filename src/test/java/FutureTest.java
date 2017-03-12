import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class FutureTest {

    class Image {
        private String name;
        private long loadTime;

        Image(String name, long loadTime) {
            this.name = name;
            this.loadTime = loadTime;
        }

        void load() throws InterruptedException, IllegalArgumentException {
            System.out.println(String.format("Started loading image: %s", this.name));
            Thread.sleep(this.loadTime);
            System.out.println(String.format("Finished loading image: %s", this.name));
        }
    }

    private void loadAll(Consumer<List<Image>> loadMethod) {
        long start = System.nanoTime();
        loadMethod.accept(IMAGES);
        long end = System.nanoTime();
        long totalTime = (end - start) / 1_000_000;
        System.out.println(String.format("Finished loading all images. Total time: %dms", totalTime));
    }

    private final List<Image> IMAGES = new ArrayList<>();

    @Before
    public void setUp() {
        IMAGES.clear();
        Arrays.stream(new Image[]{
                new Image("Cat", 1000),
                new Image("Dog", 1000),
                new Image("Elephant", 1000),
                new Image("Fox", 1000)
        }).forEach(IMAGES::add);
    }

    @Test
    public void testSync_LoadAll() {
        loadAll(images ->
                images.forEach(image -> {
                    try {
                        image.load();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
        );
    }

    @Test
    public void testAsync() throws InterruptedException {
        Image image = new Image("Cat", 10000);
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<Void> future = executor.submit(new Callable<Void>() {
            public Void call() throws Exception {
                image.load();
                return null;
            }
        });

        assertFalse(future.isDone());

        System.out.println("While image is loading, we can do other stuff...");
        Thread.sleep(5000);

        try {
            System.out.println("Now waiting for image to finish loading...");
            future.get();
            assertTrue(future.isDone());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    @Test
    public void testCancel() throws InterruptedException {
        Image image = new Image("Cat", 10000);
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<Void> future = executor.submit(() -> {
            image.load();
            return null;
        });

        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        Thread.sleep(8000);

        System.out.println("Cancelling task...");
        future.cancel(true);

        assertTrue(future.isDone());
        assertTrue(future.isCancelled());

        System.out.println("Done");

        Thread.sleep(5000);
    }

    @Test
    public void testException() throws InterruptedException {
        Image image = new Image("Cat", -10000);
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<Void> future = executor.submit(() -> {
            image.load();
            return null;
        });

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
        Image image = new Image("Cat", 9999999999999L);
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<Void> future = executor.submit(() -> {
            image.load();
            return null;
        });

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

        loadAll(images -> {
            List<Future<Void>> futures = new ArrayList<>();

            images.forEach(image -> {
                Future<Void> future = executor.submit(() -> {
                    image.load();
                    return null;
                });
                futures.add(future);
            });

            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
    }

}
