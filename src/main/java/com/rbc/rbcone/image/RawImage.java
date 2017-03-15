package com.rbc.rbcone.image;

public class RawImage extends Image {
    public RawImage() {
        super();
    }

    public RawImage(String name, String data) {
        super(name, data);
    }

    @Override
    public String toString() {
        return "RawImage{" +
                "name='" + name + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}