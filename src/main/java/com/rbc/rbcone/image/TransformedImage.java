package com.rbc.rbcone.image;

public class TransformedImage extends Image {
    public TransformedImage() {
        super();
    }

    public TransformedImage(String name, String data) {
        super(name, data);
    }

    @Override
    public String toString() {
        return "TransformedImage{" +
                "name='" + name + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
