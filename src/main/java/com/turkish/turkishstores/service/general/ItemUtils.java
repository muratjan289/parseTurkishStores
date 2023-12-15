package com.turkish.turkishstores.service.general;

import java.util.Iterator;
import java.util.List;

public class ItemUtils {


    public static void removeItemsWithEmptyVariants(List<Item> productList) {
        Iterator<Item> iterator = productList.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (item.getSubProducts() == null || item.getSubProducts().isEmpty()) {
                iterator.remove();  // Удаляем элемент, если список subProducts пустой
            }
        }
    }



}
