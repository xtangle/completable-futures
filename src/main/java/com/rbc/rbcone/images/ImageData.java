package com.rbc.rbcone.images;

public class ImageData {
    private String name;
    private String data;

    public ImageData(String name, String data) {
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
        return "ImageData{" +
                "name='" + name + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}