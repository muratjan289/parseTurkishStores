package com.turkish.turkishstores.service.fariaHome;

import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public  class Item {
    private String name;
    private String category;
    private String description;
    private String stockCode;

    @XmlElementWrapper(name = "variants")
    @XmlElement(name = "variant")
    private List<SubProduct> subProduct = new ArrayList<>();


    public Item(String name, String category, String description, String stockCode, List<SubProduct> subProduct) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.stockCode = stockCode;
        this.subProduct = subProduct;
    }

    public String getItemStockCode() {
        return stockCode;
    }

    public void setItemStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public Item() {
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", subProduct=" + subProduct +
                '}';
    }

    public List<SubProduct> getSubProducts() {
        return subProduct;
    }

    public void setSubProducts(List<SubProduct> subProduct) {
        this.subProduct = subProduct;
    }

    public String getProductName() {
        return name;
    }

    public void setProductName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return category;
    }

    public void setCategoryName(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addSubProduct(SubProduct subProduct) {
        this.subProduct.add(subProduct);
    }




}
