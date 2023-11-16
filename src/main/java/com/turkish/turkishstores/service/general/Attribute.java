package com.turkish.turkishstores.service.general;


import javax.xml.bind.annotation.*;


public class Attribute {
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String value;

    @XmlValue
    private String text;

    // Конструкторы, геттеры, сеттеры и т.д.

    public void setColor(String color) {
        this.name = "color";
        this.value = color;
        this.text = color;
    }

    public void setSize(String size) {
        this.name = "size";
        this.value = size;
        this.text = size;
    }


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public Attribute() {

    }



}
