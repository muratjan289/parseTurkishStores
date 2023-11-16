package com.turkish.turkishstores.service.general;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.LinkedList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class SubProduct {


    @XmlElement(name = "sku")
    private String variantStockCode;
    @XmlElement(name = "priceRetail")
    private double fullPrice;
    @XmlElement(name = "discountPriceRetail")
    private double discountPrice;
    private String currency = "TRY";
   @XmlElement(name = "unitCount")
    private int quantity;
    @XmlElementWrapper(name = "images")
    @XmlElement(name = "image")
    private List<String> pictureUrls;
    @XmlElementWrapper(name = "attributes",  nillable = false)
    @XmlElement(name = "attribute")
    List<Attribute> attribute = new LinkedList<>();


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public void setVariantStockCode(String variantStockCode) {
        this.variantStockCode = variantStockCode;
    }


    public double getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(double fullPrice) {
        this.fullPrice = fullPrice;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public List<String> getPictureUrls() {
        return pictureUrls;
    }

    public void setPictureUrls(List<String> pictureUrls) {
        this.pictureUrls = pictureUrls;
    }

    public SubProduct(String variantStockCode, double fullPrice, double discountPrice, String currency, int quantity, List<String> pictureUrls, List<Attribute> attribute) {
        this.variantStockCode = variantStockCode;
        this.fullPrice = fullPrice;
        this.discountPrice = discountPrice;
        this.currency = currency;
        this.quantity = quantity;
        this.pictureUrls = pictureUrls;
        this.attribute = attribute;
    }

    public SubProduct() {
    }

    public List<Attribute> getVariants() {
        return attribute;
    }

    public void setAttributes(List<Attribute> attribute) {
        this.attribute = attribute;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "SubProduct{" +
                "variantStockCode='" + variantStockCode + '\'' +
                ", fullPrice='" + fullPrice + '\'' +
                ", discountPrice='" + discountPrice + '\'' +
                ", currency='" + currency + '\'' +
                ", pictureUrls=" + pictureUrls +
                ", attribute=" + attribute +
                '}';
    }

    public void addVariant(Attribute attribute) {
        this.attribute.add(attribute);
    }

    public String getVariantStockCode() {
        return variantStockCode;
    }

    public List<Attribute> getVariant() {
        return attribute;
    }
}
