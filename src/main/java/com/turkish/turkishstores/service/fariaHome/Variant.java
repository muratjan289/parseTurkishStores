package com.turkish.turkishstores.service.fariaHome;


public class Variant {



    private String color;


    private String size;



    public Variant() {

    }



    @Override
    public String toString() {
        return "Variant{" +
                ", size='" + size + '\'' +
                '}';
    }



    public Variant(String color, String size) {
        this.color = color;
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
