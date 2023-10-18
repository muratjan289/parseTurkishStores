package com.turkish.turkishstores.service.fariaHome;

import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class SubProduct {

  private String variantStockCode;
    private String fullPrice;
    private String discountPrice;
    private String currency;
    private int quantity =2;
    @XmlElementWrapper(name = "pictures")
    @XmlElement(name = "picture")
    private List<String> pictureUrls;
    @XmlElementWrapper(name = "attributes")
    @XmlElement(name = "attribute")
    List<Variant> variant = new LinkedList<>();


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getColor() {
        return variantStockCode;
    }

    public void setColor(String variantStockCode) {
        this.variantStockCode = variantStockCode;
    }

    public String getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(String fullPrice) {
        this.fullPrice = fullPrice;
    }

    public String getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(String discountPrice) {
        this.discountPrice = discountPrice;
    }

    public List<String> getPictureUrls() {
        return pictureUrls;
    }

    public void setPictureUrls(List<String> pictureUrls) {
        this.pictureUrls = pictureUrls;
    }

    public SubProduct(String variantStockCode, String fullPrice, String discountPrice, String currency, List<String> pictureUrls, List<Variant> variant) {
        this.variantStockCode = variantStockCode;
        this.fullPrice = fullPrice;
        this.discountPrice = discountPrice;
        currency = currency;
        this.pictureUrls = pictureUrls;
        this.variant = variant;
    }


    public SubProduct() {
    }

    public List<Variant> getVariants() {
        return variant;
    }

    public void setVariants(List<Variant> variant) {
        this.variant = variant;
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
                ", variant=" + variant +
                '}';
    }

    public void addVariant(Variant variant) {
        this.variant.add(variant);
    }

}
