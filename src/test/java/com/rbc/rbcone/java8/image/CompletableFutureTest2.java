package com.rbc.rbcone.java8.image;

import static com.rbc.rbcone.java8.image.ImageService.getRawImage;
import static com.rbc.rbcone.java8.image.ImageService.getTransformedImage;
import static com.rbc.rbcone.java8.util.ThreadUtils.createDaemonThreadPool;
import static com.rbc.rbcone.java8.util.ThreadUtils.delay;
import static com.rbc.rbcone.java8.util.ThreadUtils.getTimeSince;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

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
    public void testSync_LoadAllTransformedImages() {
        long start = System.nanoTime();

        imageNames.stream()
                .map(ImageService::getRawImage)
                .map(ImageService::getTransformedImage)
                .map(transformedImage -> String.format("Loaded transformed image: %s\n", transformedImage))
                .forEach(System.out::println);

        System.out.println(String.format("Finished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testParallelStream_LoadAllTransformedImages() {
        long start = System.nanoTime();

        imageNames.parallelStream()
                .map(ImageService::getRawImage)
                .map(ImageService::getTransformedImage)
                .map(transformedImage -> String.format("Loaded transformed image: %s", transformedImage))
                .forEach(System.out::println);

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testCompletableFuture_LoadAllTransformedImages() {
        long start = System.nanoTime();

        List<CompletableFuture<TransformedImage>> transformedDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getRawImage(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedImage(imageData), executor)
                        ))
                        .collect(Collectors.toList());

        transformedDataFutures.stream()
                .map(CompletableFuture::join)
                .map(transformedImage -> String.format("Loaded transformed image: %s", transformedImage))
                .forEach(System.out::println);

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testCompletableFuture_ScaleAllTransformedImages() {
        long start = System.nanoTime();

        List<CompletableFuture<ScaledImage>> transformedDataFutures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getRawImage(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedImage(imageData), executor)
                        ))
                        .map(transformedDataFuture -> transformedDataFuture.thenApply(
                                ImageService::getScaledImage
                        ))
                        .collect(Collectors.toList());

        transformedDataFutures.stream()
                .map(CompletableFuture::join)
                .map(scaledImage -> String.format("Loaded scaled image: %s", scaledImage))
                .forEach(System.out::println);

        System.out.println(String.format("\nFinished loading all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testCompletableFuture_RenderAllScaledImages() {
        long start = System.nanoTime();

        CompletableFuture[] futures =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getRawImage(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedImage(imageData), executor)
                        ))
                        .map(transformedDataFuture -> transformedDataFuture.thenApply(
                                ImageService::getScaledImage
                        ))
                        .map(transformedDataFuture -> transformedDataFuture.thenAccept(
                                ImageService::renderImage
                        ))
                        .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        System.out.println(String.format("\nFinished rendering all images. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testCompletableFuture_CombineAllTransformedImages() {
        long start = System.nanoTime();

        CompletableFuture<Void> future =
                imageNames.stream()
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getRawImage(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedImage(imageData), executor)
                        ))
                        .reduce(
                                CompletableFuture.completedFuture(new TransformedImage()),
                                (tf1, tf2) -> tf1.thenCombineAsync(tf2, ImageService::combineTransformedImages, executor)
                        )
                        .thenApply(ImageService::getScaledImage)
                        .thenAccept(ImageService::renderImage);

        future.join();

        System.out.println(String.format("\nFinished rendering final image. Total time: %dms", getTimeSince(start)));
    }

    @Test
    public void testCompletableFuture_CombineAllTransformedImagesWhenTriggered() {
        long start = System.nanoTime();

        CompletableFuture<TransformedImage> closing = new CompletableFuture<>();
        Stream<String> imageNameStream = imageNames.stream();

        CompletableFuture<Void> future =
                imageNameStream
                        .onClose(() -> closing.complete(new TransformedImage()))
                        .map(imageName -> CompletableFuture.supplyAsync(
                                () -> getRawImage(imageName),
                                executor
                        ))
                        .map(imageDataFuture -> imageDataFuture.thenCompose(
                                imageData -> CompletableFuture.supplyAsync(() -> getTransformedImage(imageData), executor)
                        ))
                        .reduce(
                                closing,
                                (tf1, tf2) -> tf1.thenCombine(tf2, ImageService::combineTransformedImages)
                        )
                        .thenApply(ImageService::getScaledImage)
                        .thenAccept(ImageService::renderImage);

        delay(8000);

        System.out.println("Closing the stream...");
        imageNameStream.close();

        future.join();

        System.out.println(String.format("\nFinished rendering final image. Total time: %dms", getTimeSince(start)));
    }

}
