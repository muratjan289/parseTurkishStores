package com.turkish.turkishstores.service.general;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public  class Item {

    private String titleTr;
    private String category;

    @XmlElement(name = "priceRetail")
    private double itemFullPrice;
    @XmlElement(name = "discountPriceRetail")
    private double itemDiscountPrice;
    private String descriptionTr;
    private String importVendorCode;
    private String company;
    private String itemUrl;



    @XmlElementWrapper(name = "variants")
    @XmlElement(name = "variant")
    private List<SubProduct> subProduct = new ArrayList<>();


    public Item(String titleTr, String category, double itemFullPrice, double itemDiscountPrice, String descriptionTr, String importVendorCode, String company, String itemUrl, List<SubProduct> subProduct) {
        this.titleTr = titleTr;
        this.category = category;
        this.itemFullPrice = itemFullPrice;
        this.itemDiscountPrice = itemDiscountPrice;
        this.descriptionTr = descriptionTr;
        this.importVendorCode = importVendorCode;
        this.company = company;
        this.itemUrl = itemUrl;
        this.subProduct = subProduct;
    }

    public String getItemStockCode() {
        return importVendorCode;
    }

    public void setItemStockCode(String stockCode) {
        this.importVendorCode = stockCode;
    }

    public Item() {
    }



    @Override
    public String toString() {
        return "Item{" +
                "titleTr='" + titleTr + '\'' +
                ", category='" + category + '\'' +
                ", itemFullPrice=" + itemFullPrice +
                ", itemDiscountPrice=" + itemDiscountPrice +
                ", descriptionTr='" + descriptionTr + '\'' +
                ", importVendorCode='" + importVendorCode + '\'' +
                ", company='" + company + '\'' +
                ", itemUrl='" + itemUrl + '\'' +
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
        return titleTr;
    }

    public void setProductName(String titleTr) {
        this.titleTr = titleTr;
    }

    public String getCategoryName() {
        return category;
    }

    public void setCategoryName(String category) {
        this.category = category;
    }

    public String getDescription() {
        return descriptionTr;
    }

    public void setDescription(String descriptionTr) {
        this.descriptionTr = descriptionTr;
    }

    public void addSubProduct(SubProduct subProduct) {
        this.subProduct.add(subProduct);
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }


    public double getItemFullPrice() {
        return itemFullPrice;
    }

    public void setItemFullPrice(double itemFullPrice) {
        this.itemFullPrice = itemFullPrice;
    }

    public double getItemDiscountPrice() {
        return itemDiscountPrice;
    }

    public void setItemDiscountPrice(double itemDiscountPrice) {
        this.itemDiscountPrice = itemDiscountPrice;
    }



}
