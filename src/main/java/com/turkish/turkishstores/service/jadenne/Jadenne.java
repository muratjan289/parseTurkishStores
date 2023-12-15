package com.turkish.turkishstores.service.jadenne;

import com.turkish.turkishstores.service.general.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.turkish.turkishstores.service.general.WebDriverSingleton.getDriver;
import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXMLAndSaveToFile;

public class Jadenne {


    private static WebDriver driver = getDriver();
    private static List<Item> productList = new ArrayList<>();
    private static LinkedList<String> links = new LinkedList<>();

    private static String productNameXPath = "//h1";

    private static By discountPriceXPath = By.xpath("//div[@class='product-price-container']//span[@class='price']//span[@class='amount discounted'] | //div[@class='product-price-container']//span[@class='price']//span[@class='amount ']");
    private static By fullPriceXPath = By.xpath("//div[@class='product-price-container']//span[@class='price']//span[@class='amount'] | //div[@class='product-price-container']//span[@class='price']//span[@class='amount ']");
    private static By descriptionXPath = By.xpath("//div[@class='product-short-description rte']//p[1] | //div[@class='product-short-description rte']");
    private static By photoXPath = By.xpath("    //div[@class='product-thumbnail is-initial-selected']//img |  //div[@class='product-thumbnail ']//img |  //div[@class='product-thumbnail']//img");

    private static String currentSize;


    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {

        try {
            // Загрузка XML-файла
            Document doc = Jsoup.connect("https://jadenne.com/sitemap_products_1.xml?from=8428029247803&to=8953063375163").get();
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



        WebDriverSingleton.getDriver();


//            driver.get("https://jadenne.com/products/bebe-boy-carti-er-bileklik-bilezik");
//            parseItemFull(driver);

        boolean isFirstLink = true;
        int totalLinks = links.size();
        int linksProcessed = 0;

        for (String link : links) {
            if (isFirstLink) {
                isFirstLink = false;
                continue;
            }
            driver.get(link);
            System.out.println("Программа идет: " + link);
            Thread.sleep(200);
            parseItemFull(driver);

            String linktt = driver.getCurrentUrl();
            System.out.println(linktt);

            linksProcessed++;
            int linksRemaining = totalLinks - linksProcessed;
            System.out.println("Ссылок обработано: " + linksProcessed);
            System.out.println("Ссылок осталось: " + linksRemaining);
        }


        WebDriverSingleton.closeDriver(); // Закрываем драйвер в блоке finally, чтобы убедиться, что он закроется

        System.out.println("driver is closed");
        ItemUtils.removeItemsWithEmptyVariants(productList);
        convertListToXMLAndSaveToFile(productList, "Jadenne.xml");


    }


    public static void parseItemFull(WebDriver driver) throws InterruptedException {

        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the ProductFariaHome class to store product details
        Item productJadenne = new Item();


        var productName = doc.selectXpath(productNameXPath);
        productJadenne.setProductName(productName.text());



        WebElement discountPrice = driver.findElement(discountPriceXPath);
        double testDiscountPrice  = extractNumber(discountPrice.getText());
        productJadenne.setItemDiscountPrice(testDiscountPrice);

        WebElement fullPrice = driver.findElement(fullPriceXPath);
        double testFullPrice = extractNumber(fullPrice.getText());
        productJadenne.setItemFullPrice(testFullPrice);

        WebElement description = driver.findElement(descriptionXPath);
        productJadenne.setDescription(description.getText());


        String productNameJadenne = productJadenne.getProductName();
        String category = "другая категория"; // Категория по умолчанию

        System.out.println(productNameJadenne);

        if (productNameJadenne.contains("Bileklik")) {
            category = "bileklik";
        } else if (productNameJadenne.contains("Halhal")) {
            category = "halhal";
        } else if (productNameJadenne.contains("Kolye")) {
            category = "kolye";
        }else if (productNameJadenne.contains("Küpe")) {
            category = "Küpe";
        }else if (productNameJadenne.contains("Yüzük")) {
            category = "Yüzük";
        }else if (productNameJadenne.contains("Set")) {
            category = "set";
        }
        productJadenne.setCategoryName(category);

        productJadenne.setCompany("Jadenne");

        productJadenne.setItemUrl(driver.getCurrentUrl());

        productJadenne.setItemStockCode(generateStockCode(driver.getCurrentUrl()));



        List<WebElement> sizeNames = driver.findElements(By.xpath("//div[@class='product-information']//div[@class='variations']//label//span"));
        for (WebElement variantName : sizeNames) {
            try {
                variantName.click();
                currentSize = variantName.getText(); // Сохраняем выбранный размер
                Thread.sleep(150);
                SubProduct subProduct = parseVariantOfProduct(driver);  // Создаем новый объект SubProduct
                productJadenne.getSubProducts().add(subProduct); // Добавляем SubProduct в список
            }
            catch (WebDriverException e) {
                System.out.println("Failed to click on the element due to an error: " + e.getMessage());
                continue;  // Пропускаем текущую итерацию и переходим к следующему элементу
            }
        }

        if (sizeNames.size()==0){
            SubProduct subProduct = parseVariantOfProduct(driver);  // Создаем новый объект SubProduct
            productJadenne.getSubProducts().add(subProduct);

        }
        productList.add(productJadenne);

}

    public static SubProduct parseVariantOfProduct(WebDriver driver)throws InterruptedException {

        Document doc = Jsoup.parse(driver.getPageSource());

        SubProduct subProduct = new SubProduct();

        WebElement discountPrice = driver.findElement(discountPriceXPath);
        double testDiscountPrice  = extractNumber(discountPrice.getText());
        subProduct.setDiscountPrice(testDiscountPrice);

        WebElement fullPrice = driver.findElement(fullPriceXPath);
        double testFullPrice = extractNumber(fullPrice.getText());
        subProduct.setFullPrice(testFullPrice);

        subProduct.setVariantStockCode(generateStockCode(driver.getCurrentUrl()) + extractVariant(driver.getCurrentUrl()));

        subProduct.setQuantity(1);

        List<String> images = sliderPhoto(driver);
        subProduct.setPictureUrls(images);

        List<Attribute> attributeList = parseAndSetPropertiesAttribute(driver);
        subProduct.setAttributes(attributeList);


        return subProduct;
    }

    public static List<Attribute> parseAndSetPropertiesAttribute (WebDriver driver) throws InterruptedException {
        Document doc = Jsoup.parse(driver.getPageSource());
        List<Attribute> attributeList = new ArrayList<>();

        Attribute attribute = new Attribute();

        System.out.println("Current size ==" + currentSize);
        if (currentSize == null){
            attribute.setSize("STD");
        }else {
            attribute.setSize(currentSize); // Устанавливаем сохраненный размер
        }  // Добавляем sizeVariant в attributeList
        attributeList.add(attribute);
        currentSize = null;
        return attributeList;
    }





    public static List<String> sliderPhoto(WebDriver driver) throws InterruptedException {
        List<String> imageUrls = new ArrayList<>();
        List<WebElement> allImages = driver.findElements(photoXPath);

        for (WebElement img : allImages) {
            String srcValue = img.getAttribute("src");
            srcValue = transformImageUrl(srcValue);  // тут происходит трансформация
            System.out.println("Link for images" + srcValue);
            if (!imageUrls.contains(srcValue)) {
                imageUrls.add(srcValue);
            }
        }
        return imageUrls;
    }




    public static String transformImageUrl(String url) {
        return url.replace("_20", "_900");
    }


    //div[@class='product-thumbnail is-initial-selected'] |  //div[@class='product-thumbnail '] |  //div[@class='product-thumbnail']
    public static double extractNumber(String input) {
        // Удаляем все нечисловые символы, кроме точек, запятых и пробелов
        input = input.replaceAll("[^\\d,.]", "");

        // Определяем, является ли последний символ перед числовым значением точкой или запятой
        char decimalSeparator = ',';
        if (input.lastIndexOf('.') > input.lastIndexOf(',')) {
            decimalSeparator = '.';
        }

        // Удаляем все символы, кроме последнего разделителя (он становится десятичным разделителем)
        input = input.replaceAll("[.,](?=.*[" + decimalSeparator + "])", "");

        // Заменяем десятичный разделитель на точку, если это необходимо
        if (decimalSeparator == ',') {
            input = input.replace(',', '.');
        }

        try {
            return Double.parseDouble(input) * 1.20 + 50; // Преобразуем в double
        } catch (NumberFormatException e) {
            return 0; // В случае ошибки преобразования возвращаем 0
        }



    }


    public static String generateStockCode(String url) {
        // Удаление базовой части URL
        url = url.replace("https://jadenne.com/", "").replace("products/", "");

        // Обрезка строки до вопросительного знака, если он присутствует
        int questionMarkIndex = url.indexOf('?');
        if (questionMarkIndex != -1) {
            url = url.substring(0, questionMarkIndex);
        }

        // Получение хэш-кода
        int hashCode = url.hashCode();

        // Формирование stockCode
        return  "-" + hashCode;
    }



    public static String extractVariant(String url) {
        String variantPrefix = "variant=";
        int variantIndex = url.indexOf(variantPrefix);

        if (variantIndex != -1) {
            // Извлекаем подстроку, начиная с позиции после "variant="
            return url.substring(variantIndex + variantPrefix.length());
        }
        return ""; // Возвращаем пустую строку, если "variant=" не найден
    }



}
