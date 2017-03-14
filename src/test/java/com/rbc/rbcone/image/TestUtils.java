package com.rbc.rbcone.image;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

class TestUtils {

    static private final long DEFAULT_DELAY = 1000;

    static class CustomException extends Exception {
        CustomException(String message) {
            super(message);
        }
    }

    static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static ImageData getImageData(String imageName) {
        return getImageData(imageName, DEFAULT_DELAY);
    }

    static ImageData getImageData(String imageName, long millis) {
        System.out.println(String.format("Getting image data for %s...", imageName));
        delay(millis);
        ImageData imageData = new ImageData(imageName, StringUtils.lowerCase(imageName));
        System.out.println(String.format("Finished getting image data for %s", imageName));
        return imageData;
    }

    static Executor createDaemonThreadPool(int numThreads) {
        return Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }

    static TransformedData getTransformedData(ImageData imageData) {
        String name = imageData.getName();
        System.out.println(String.format("Transforming image data for %s...", name));
        delay(DEFAULT_DELAY);
        TransformedData transformedData = new TransformedData(name, StringUtils.reverse(imageData.getData()));
        System.out.println(String.format("Finished transforming image data for %s", name));
        return transformedData;
    }

    static ResizedData getResizedData(TransformedData transformedData) {
        String name = transformedData.getName();
        System.out.println(String.format("Resized image data for %s", name));
        return new ResizedData(name, StringUtils.upperCase(transformedData.getData()));
    }

    static void renderScaledImage(ResizedData resizedData) {
        String name = resizedData.getName();
        String data = resizedData.getData();
        System.out.println(String.format("Rendering image data for %s...", name));
        delay((long) ((0.5 + Math.random()) * DEFAULT_DELAY));
        System.out.println(String.format("[ %s ]", StringUtils.center(data, Math.max(24, StringUtils.length(data)))));
    }

    static TransformedData combineTransformedData(TransformedData td1, TransformedData td2) {
        String name1 = td1.getName();
        String name2 = td2.getName();
        System.out.println(String.format("Combining transformed data for %s and %s...", name1, name2));
        delay((long) (0.1 * DEFAULT_DELAY));
        return new TransformedData(
                StringUtils.capitalize(StringUtils.join(
                        StringUtils.substring(name1, 0, 1),
                        StringUtils.substring(name2, 0, 1)
                )),
                StringUtils.join(td1.getData(), td2.getData(), ' ')
        );
    }

}
