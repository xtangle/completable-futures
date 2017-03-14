package com.rbc.rbcone.image;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

class TestUtils {

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
        return getImageData(imageName, 1000);
    }

    static ImageData getImageData(String imageName, long millis) {
        System.out.println(String.format("Getting image data for %s...", imageName));
        delay(millis);
        ImageData imageData = new ImageData(imageName, imageName.toLowerCase());
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
}
