package com.rbc.rbcone.image;

import org.apache.commons.lang3.StringUtils;

import static com.rbc.rbcone.util.ThreadUtils.delay;

public class ImageLoader {

    private static final long DEFAULT_DELAY = 1000;

    public static RawImage getRawImage(String imageName) {
        return getRawImage(imageName, DEFAULT_DELAY);
    }

    public static RawImage getRawImage(String imageName, long millis) {
        System.out.println(String.format("Getting raw image data for %s...", imageName));
        if (!delay(millis)) {
            return null;
        }
        RawImage rawImage = new RawImage(imageName, StringUtils.lowerCase(imageName));
        System.out.println(String.format("Finished getting raw image data for %s", imageName));
        return rawImage;
    }

    public static TransformedImage getTransformedImage(RawImage rawImage) {
        String name = rawImage.getName();
        System.out.println(String.format("Transforming image data for %s...", name));
        if (!delay(DEFAULT_DELAY)) {
            return null;
        }
        TransformedImage transformedImage = new TransformedImage(name, StringUtils.reverse(rawImage.getData()));
        System.out.println(String.format("Finished transforming image data for %s", name));
        return transformedImage;
    }

    public static ScaledImage getScaledImage(TransformedImage transformedImage) {
        String name = transformedImage.getName();
        System.out.println(String.format("Resized image data for %s", name));
        return new ScaledImage(name, StringUtils.upperCase(transformedImage.getData()));
    }

    public static void renderImage(ScaledImage scaledImage) {
        String name = scaledImage.getName();
        String data = scaledImage.getData();
        System.out.println(String.format("Rendering image data for %s...", name));
        delay((long) ((0.5 + Math.random()) * DEFAULT_DELAY));
        System.out.println(String.format("[ %s ]", StringUtils.center(data, Math.max(24, StringUtils.length(data)))));
    }

    public static TransformedImage combineTransformedImages(TransformedImage ti1, TransformedImage ti2) {
        String name1 = ti1.getName();
        String name2 = ti2.getName();
        System.out.println(String.format("Combining transformed data for %s and %s...", name1, name2));
        if (!delay((long) (0.1 * DEFAULT_DELAY))) {
            return null;
        }
        return new TransformedImage(
                StringUtils.capitalize(StringUtils.join(
                        StringUtils.substring(name1, 0, 1),
                        StringUtils.substring(name2, 0, 1)
                )),
                StringUtils.join(ti1.getData(), ti2.getData(), ' ')
        );
    }
}
