package com.turkish.turkishstores.service.elbeyButik;

import com.turkish.turkishstores.service.general.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXMLAndSaveToFile;

public class ElbeyButik {


    public static WebDriver driver = WebDriverSingleton.getDriver();
    private static LinkedList<String> links = new LinkedList<>();
    private static List<Item> productList = new ArrayList<>();
    private static String productNameXPath ="//h1[@class='title']";
    private static String  categoryNameXPath = "(//a[@itemprop='item']//span[@itemprop='name'])[2]";
    private static String itemStockCodeXPath = "//li[contains(text(), 'Ürün Kodu')]//span";
    private static By itemDescriptionXPath = By.xpath("(//div[@class='p-g-mod-body  ']//div[@class='raw-content'])[1] |(//ul[@class='detail-desc-list'])[1] ");
    private static String variantSizeElementXpath = "//a[@class='active']/text()";
    private static String quantityXPath ="//span[@class='text-success']";

    private static String fullPriceXPath= " //div[@class='list-price sale-list-price']";
    private static String discountPriceXPath = "//div[@class='sale-price sale-variant-price ']";

    private static String sizeAttributeName = "//span[@class='size_box selected']";



    //div[@class='carousel-item']//a//img


    public static void main(String[] args) throws InterruptedException, IOException {

        try {
            // Загрузка XML-файла
            Document doc = Jsoup.connect("https://www.elbeybutik.com/sitemap/products/1.xml").get();
            // Извлечение всех элементов <loc>
            Elements locElements = doc.select("loc");
            // Добавление каждой ссылки в LinkedList
            for (Element locElement : locElements) {
                String link = locElement.text();
                links.add(link);
            }

            // Вывод ссылок
            for (String link : links) {
                System.out.println(link);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
            try {


       WebDriverSingleton.getDriver();
//                driver.get("https://www.elbeybutik.com/elbey-balikci-yaka-kaskorse-croptop-3717");
//                parseItemFull(driver);

                int totalLinks = links.size();
                int linksProcessed = 0;

                for (String link : links) {
                    driver.get(link);
                    System.out.println("Программа идет: " + link);
                    Thread.sleep(300);
                    parseItemFull(driver);

                    String linktt = driver.getCurrentUrl();
                    System.out.println(linktt);

                    linksProcessed++;
                    int linksRemaining = totalLinks - linksProcessed;
                    System.out.println("Ссылок обработано: " + linksProcessed);
                    System.out.println("Ссылок осталось: " + linksRemaining);
                }

    }//открой для полного цикла
            catch (InterruptedException e) {
        throw new RuntimeException(e);
    }


       finally {
        WebDriverSingleton.closeDriver(); // Закрываем драйвер в блоке finally, чтобы убедиться, что он закроется
    }

        System.out.println("driver is closed");
        ItemUtils.removeItemsWithEmptyVariants(productList);
    convertListToXMLAndSaveToFile(productList, "ElbeyButik.xml");
}

    public static void parseItemFull(WebDriver driver) throws InterruptedException {

        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the ProductFariaHome class to store product details
        Item productElbeyButik = new Item();

        var productName = doc.selectXpath(productNameXPath);
        productElbeyButik.setProductName(productName.text());

        var categoryName = doc.selectXpath(categoryNameXPath);
        productElbeyButik.setCategoryName(categoryName.text());
        System.out.println(categoryName.text());

        var fullPrice = doc.selectXpath(fullPriceXPath);
        double testFullPrice = Double.parseDouble(String.valueOf(extractNumber(fullPrice.text())));
        productElbeyButik.setItemFullPrice(testFullPrice);



        var discountPrice = doc.selectXpath(discountPriceXPath);
        double testDiscountPrice = Double.parseDouble(String.valueOf((extractNumber(discountPrice.text()))));
        if (testDiscountPrice==0){
            productElbeyButik.setItemDiscountPrice(testFullPrice);
        }
        else {
            productElbeyButik.setItemDiscountPrice(Double.parseDouble(String.valueOf(testDiscountPrice)));
        }
        var itemStockCode = doc.selectXpath(itemStockCodeXPath);
        productElbeyButik.setItemStockCode(itemStockCode.text());

        WebElement itemDescription = driver.findElement(itemDescriptionXPath);
        productElbeyButik.setDescription(itemDescription.getText());



        productElbeyButik.setItemUrl(driver.getCurrentUrl());

        productElbeyButik.setCompany("Elbey Butik");
        List<SubProduct> subProducts = new ArrayList<>(); // Создаем список для хранения SubProducts

        List<WebElement> variantNames = driver.findElements(By.xpath("//select[@data-variant-name='Beden']/following-sibling::div[@class='options']//a"));
        for (WebElement variantName : variantNames) {
            if (!variantName.getAttribute("class").contains("sold-out ")) {
                variantName.click();
                SubProduct subProduct = parseVariantOfProduct(driver);  // Создаем новый объект SubProduct

                // Проверяем, существует ли уже SubProduct с таким же variantStockCode
                boolean alreadyExists = subProducts.stream()
                        .anyMatch(existingSubProduct -> existingSubProduct.getVariantStockCode().equals(subProduct.getVariantStockCode()));

                if (!alreadyExists) {
                    subProducts.add(subProduct); // Добавляем SubProduct, если он уникален
                }
            }
        }

        // Добавляем список subProducts к productElbeyButik
        productElbeyButik.setSubProducts(subProducts);

        productList.add(productElbeyButik);

        System.out.println(productElbeyButik);
    }

    public static SubProduct parseVariantOfProduct(WebDriver driver)throws InterruptedException {

        Document doc = Jsoup.parse(driver.getPageSource());

        SubProduct subProduct = new SubProduct();



        var quantity = doc.selectXpath(quantityXPath);
        subProduct.setQuantity(parseQuantity(quantity.text()));


        var fullPrice = doc.selectXpath(fullPriceXPath);
        double testFullPrice = Double.parseDouble(String.valueOf(extractNumber(fullPrice.text())));
        subProduct.setFullPrice(testFullPrice);

            var discountPrice = doc.selectXpath(discountPriceXPath);
            double testDiscountPrice = Double.parseDouble(String.valueOf((extractNumber(discountPrice.text()))));
            if (testDiscountPrice==0){
                subProduct.setDiscountPrice(testFullPrice);
            }
            else {
            subProduct.setDiscountPrice(Double.parseDouble(String.valueOf(testDiscountPrice)));
            }

        var itemStockCode = doc.selectXpath(itemStockCodeXPath);
        WebElement element = driver.findElement(By.xpath("//a[@class='active']"));
        String elbeyButikVariantStockCode = itemStockCode.text() + element.getText();
        subProduct.setVariantStockCode(elbeyButikVariantStockCode);

        List<String> images = sliderPhoto(driver);
        subProduct.setPictureUrls(images);


        List<Attribute> attributeList = parseAndSetPropertiesAttribute(driver);
        subProduct.setAttributes(attributeList);



        return subProduct; // Return the populated subProduct instance

    }




        public static List<Attribute> parseAndSetPropertiesAttribute(WebDriver driver) throws InterruptedException {
        Document doc = Jsoup.parse(driver.getPageSource());
        List<Attribute> attributeList = new ArrayList<>();


        Attribute attribute = new Attribute();



        WebElement element = driver.findElement(By.xpath("//a[@class='active']"));
        attribute.setSize(element.getText());
        System.out.println(element.getText() + "Название варианта вот такое смотри");
        // Добавляем sizeVariant в attributeList
        attributeList.add(attribute);
        return attributeList;
    }
    public static List<String> sliderPhoto(WebDriver driver) throws InterruptedException {

        List<String> imageUrls = new ArrayList<>();
        List<WebElement> allImages = driver.findElements(By.xpath("//div[@class='carousel-inner']//div[contains(@class, 'carousel-item')]//a[@href]"));

        System.out.println(allImages.size() + "       размер массива во такой");
        for (WebElement img : allImages) {
            String srcValue = img.getAttribute("href");
                Thread.sleep(100);
                imageUrls.add(srcValue);

        }
        System.out.println(imageUrls.size()+ "  Размер массива" + imageUrls.toString());
        return imageUrls;
    }



    public static double extractNumber(String input) {
        Pattern pattern = Pattern.compile("([-]?\\d+[,.]\\d+)"); // Регулярное выражение для числа с плавающей точкой или запятой
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String matched = matcher.group(1).replace(',', '.'); // Замена запятой на точку
            double extraPara =  Double.parseDouble(matched);
            double testPrice = (extraPara) * 1.15 + 60.00;

            return testPrice;
        } else {
            return 0; // Возвращаем 0, если число не найдено
        }
    }


    public static int parseQuantity(String input) {
        if (input == null || input.isEmpty()) {
            return 0; // Возвращает 0, если строка пуста или null
        }

        String numericOnly = input.replaceAll("[^\\d]", ""); // Удаляет все, кроме цифр
        try {
            return Integer.parseInt(numericOnly); // Преобразование в int
        } catch (NumberFormatException e) {
            return 0; // Возвращает 0 в случае исключения
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



    }
