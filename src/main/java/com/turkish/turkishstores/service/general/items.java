package com.turkish.turkishstores.service.general;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

    @XmlRootElement
    public class items {
        private List<Item> productList;


        @XmlElement(name = "item")
        public List<Item> getProductList() {
            return productList;
        }

        public void setProductList(List<Item> productList) {
            this.productList = productList;
        }
    }

