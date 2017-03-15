package com.rbc.rbcone.image;

public class ScaledImage extends Image {
    public ScaledImage() {
        super();
    }

    public ScaledImage(String name, String data) {
        super(name, data);
    }

    @Override
    public String toString() {
        return "ScaledImage{" +
                "name='" + name + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
