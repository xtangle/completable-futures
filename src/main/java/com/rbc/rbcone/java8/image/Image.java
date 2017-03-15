package com.rbc.rbcone.java8.image;

public abstract class Image {
    protected String name;
    protected String data;

    public Image() {
        this(null, null);
    }

    public Image(String name, String data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Image{" +
                "name='" + name + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
