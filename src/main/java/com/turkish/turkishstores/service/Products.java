package com.turkish.turkishstores.service;

import com.turkish.turkishstores.service.fariaHome.Item;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

    @XmlRootElement
    public class Products {
        private List<Item> productList;


        @XmlElement(name = "product")
        public List<Item> getProductList() {
            return productList;
        }

        public void setProductList(List<Item> productList) {
            this.productList = productList;
        }
    }

