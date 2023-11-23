package com.turkish.turkishstores.service.suvari;

import com.turkish.turkishstores.service.general.Attribute;
import com.turkish.turkishstores.service.general.Item;
import com.turkish.turkishstores.service.general.SubProduct;
import com.turkish.turkishstores.service.general.WebDriverSingleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.turkish.turkishstores.service.general.WebDriverSingleton.getDriver;
import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXMLAndSaveToFile;

public class Suvari {

    private static WebDriver driver = getDriver();
    private static List<Item> productList = new ArrayList<>();
    private static  List<String> suvariItemLinks = new ArrayList<>();
    private static LinkedList<String> links = new LinkedList<>();
    private static String productNameXPath = "//h1[@class='ProductHeader product-name']";
    private static String fullPriceXPath = "//span[@class='PriceOld old-price']";
    private static String discountPriceXpath = "//span[@class='Price sale-price']";
    private static By categoryXPath = By.xpath("//ul[@class='breadcrump-list']/li[position()=2 or position()=3]");
    private static By descriptionXPath = By.xpath( "//div[@class='information-item'][position()>=3 and position()<=22]");
    private static By elementVariantNameXPath = By.xpath("//div[@class='ColorOptionItem color-item '] | //div[@class='ColorOptionItem color-item active']");
    private static By sizeVariantNameXPath = By.xpath("//a[@class='size-item-text js-size-item-link']");
    private static final String nameSizeVariantXPath = "//div[@class='size-item active']";
   private static final String nameColorVariantXpath = "a.change-color[title]"; // CSS-селектор для выбора элемента <a> с атрибутом title
    private static final By linkForImages = By.xpath("//li[@class='image-item js-fancy-image-item']");


    public static void main(String[] args) throws InterruptedException, IOException {

        try {
            Document docSuvari = Jsoup.connect("https://www.suvari.com.tr/sitemap.xml").get();
            Elements locProductLinks = docSuvari.select("loc:containsOwn(productimages)");
            for (Element locELementLink : locProductLinks){
                String linkk = locELementLink.text();
                suvariItemLinks.add(linkk);
            }
            for (String link1 : suvariItemLinks){
            // Загрузка XML-файла
            Document doc = Jsoup.connect(link1).get();
            // Извлечение всех элементов <loc>
            Elements locElements = doc.select("loc");
            // Добавление каждой ссылки в LinkedList
            for (Element locElement : locElements) {
                String link = locElement.text();
                if (!link.endsWith(".webp")) {
                    links.add(link);
                }
            }
            }
            // Вывод ссылок
            for (String link : links) {
                System.out.println(link);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//            WebDriverSingleton.getDriver();
//            driver.get("https://www.suvari.com.tr/tr/siyah-regular-kalip-kapitone-mont_mn2008700281-s09-64");
//            parseItemFull(driver);

        for (String link : links) {

            WebDriverSingleton.getDriver();
            driver.get(link);
            System.out.println("Programma idet" + link.toString());

            Thread.sleep(300);
            parseItemFull(driver);


            String linktt = driver.getCurrentUrl();
            System.out.println(linktt);


        }


        WebDriverSingleton.closeDriver(); // Закрываем драйвер в блоке finally, чтобы убедиться, что он закроется

//        removeItemsWithEmptyVariants();
        convertListToXMLAndSaveToFile(productList, "Suvari.xml");

    }

    public static void parseItemFull(WebDriver driver) throws InterruptedException {

        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the ProductFariaHome class to store product details
        Item productSuvari = new Item();
        Thread.sleep(300);
        var productName = doc.selectXpath(productNameXPath);
        productSuvari.setProductName(productName.text());



        var discountPrice = doc.selectXpath(discountPriceXpath);
        double testDiscountPrice  = extractNumber(discountPrice.text());
        productSuvari.setItemDiscountPrice(testDiscountPrice);

        var fullPrice = doc.selectXpath(fullPriceXPath);
        double testFullPrice = extractNumber(fullPrice.text());
        if (testFullPrice == 0) {
            // Если полная цена равна 0, используем цену со скидкой
            productSuvari.setItemFullPrice(testDiscountPrice);
        } else {
            // Иначе используем полную цену
            productSuvari.setItemFullPrice(testFullPrice);
        }

        List<WebElement> variantNames = driver.findElements(categoryXPath);
        StringBuilder combinedText = new StringBuilder();
        for (WebElement element : variantNames) {
            combinedText.append((element.getText())).append("  ");
        }
        String finalText = combinedText.toString().trim(); // Преобразование StringBuilder в String и удаление лишних пробелов
        productSuvari.setCategoryName(finalText);

        productSuvari.setItemUrl(driver.getCurrentUrl());
        productSuvari.setCompany("Suvari");

        String stockCode = extractStockCode(driver.getCurrentUrl());
        productSuvari.setItemStockCode(stockCode);

        List<WebElement> description = driver.findElements(descriptionXPath);
        StringBuilder descriptionCombined = new StringBuilder();
        for(WebElement element: description){
            descriptionCombined.append((element.getText())).append(" ");
        }
        String finalDescription = descriptionCombined.toString().trim();
        productSuvari.setDescription(finalDescription);


            Thread.sleep(200);



            List<WebElement> sizeVariantName = driver.findElements(sizeVariantNameXPath);
            for(WebElement size : sizeVariantName){
                size.click();
                SubProduct subProduct = parseAndSetPropertiesSubproduct(driver);
                productSuvari.getSubProducts().add(subProduct);// Create a new SubProduct object



            }
            System.out.println(sizeVariantName.size() + " Amount Variants on the Page");
        productList.add(productSuvari);

    }


    public static SubProduct parseAndSetPropertiesSubproduct(WebDriver driver) throws InterruptedException {
        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the SubProduct class to store sub-product details
        SubProduct subProduct = new SubProduct();



        var discountPrice = doc.selectXpath(discountPriceXpath);
        double testDiscountPrice  = extractNumber(discountPrice.text());
        subProduct.setDiscountPrice(testDiscountPrice);

        var fullPrice = doc.selectXpath(fullPriceXPath);
        double testFullPrice = extractNumber(fullPrice.text());
        if (testFullPrice == 0) {
            // Если полная цена равна 0, используем цену со скидкой
            subProduct.setFullPrice(testDiscountPrice);
        } else {
            // Иначе используем полную цену
            subProduct.setFullPrice(testFullPrice);
        }

        List<String> images = sliderPhoto(driver);
        subProduct.setPictureUrls(images);

        var nameOfSize = doc.selectXpath(nameSizeVariantXPath);
        String stockCode = extractStockCode(driver.getCurrentUrl());
        subProduct.setVariantStockCode(stockCode + nameOfSize.text());

        subProduct.setQuantity(2);

        List<Attribute> variantList = parseAndSetPropertiesAttribute(driver);
        subProduct.setAttributes(variantList);

        return subProduct; // Return the populated subProduct instance

    }

    public static List<Attribute> parseAndSetPropertiesAttribute(WebDriver driver) throws InterruptedException {
        Document doc = Jsoup.parse(driver.getPageSource());
        List<Attribute> attributeList = new ArrayList<>();
        Attribute colorAttribute = new Attribute();
        Attribute sizeAttribute = new Attribute();

        Element colorElement = doc.select(nameColorVariantXpath).first(); // Выбираем первый элемент, соответствующий селектору

        if (colorElement != null) { // Проверяем, что элемент не равен null
            String colorName = colorElement.attr("title"); // Получаем значение атрибута title
            colorAttribute.setColor(colorName);
            attributeList.add(colorAttribute);
        } else {
            System.out.println("color = null");
        }

        var nameSize = doc.selectXpath(nameSizeVariantXPath);
        sizeAttribute.setSize(nameSize.text());
        attributeList.add(sizeAttribute);

    return attributeList;
    }

    public static List<String> sliderPhoto(WebDriver driver) throws InterruptedException {

        List<String> imageUrls = new ArrayList<>();
        List<WebElement> allImages = driver.findElements(linkForImages);

        System.out.println(allImages.size() + "       размер массива во такой");
        for (WebElement img : allImages) {
            String srcValue = img.getAttribute("href");
            Thread.sleep(50);
            imageUrls.add(srcValue);

        }
        System.out.println(imageUrls.size()+ "  Размер массива" + imageUrls.toString());
        return imageUrls;
    }

    public static double extractNumber(String input) {
        // Удаляем все нечисловые символы, кроме точек и запятых
        input = input.replaceAll("[^\\d,\\.]", "");

        // Заменяем точки на пустые строки (удаляем их)
        input = input.replace(".", "");

        // Заменяем запятые на точки
        input = input.replace(',', '.');

        try {
            return Double.parseDouble(input) * 1.35; // Преобразуем в double
        } catch (NumberFormatException e) {
            return 0; // В случае ошибки преобразования возвращаем 0
        }
    }


    public static void removeItemsWithEmptyVariants() {
        Iterator<Item> iterator = productList.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (item.getSubProducts() == null || item.getSubProducts().isEmpty()) {
                iterator.remove();  // Удаляем элемент, если список variants пустой
            }
        }
    }

    public static String extractStockCode(String url) {
        // Находим последний индекс символа '_'
        int lastUnderscoreIndex = url.lastIndexOf('_');

        // Проверяем, был ли найден символ '_'
        if (lastUnderscoreIndex != -1) {
            // Возвращаем подстроку, начиная с позиции следующей за '_'
            return url.substring(lastUnderscoreIndex + 1);
        }

        // В случае отсутствия '_', возвращаем пустую строку или сообщение об ошибке
        return "StockCode not found";
    }


}
