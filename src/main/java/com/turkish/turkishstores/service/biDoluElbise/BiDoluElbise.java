package com.turkish.turkishstores.service.biDoluElbise;

import com.turkish.turkishstores.service.general.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.turkish.turkishstores.service.general.WebDriverSingleton.getDriver;
import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXMLAndSaveToFile;

public class BiDoluElbise {

    private static WebDriver driver = getDriver();
    private static List<Item> productList = new ArrayList<>();
    private static LinkedList<String> links = new LinkedList<>();

    private static String productNameXPath = "//h1//span";
    private static String fullPriceXPath = "//div[@class='Formline PiyasafiyatiContent']//span[@class='spanFiyat']";
    private static String discountPriceXPath ="//div[@class='Formline IndirimliFiyatContent']//span[@class='spanFiyat']";
    private static String categoryXPath = "//li[@itemprop='itemListElement'][2]";
    private static String descriptionXPath = "//div[@class='urunTabAlt']/ul";







    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {

        try {
            // Загрузка XML-файла
            Document doc = Jsoup.connect("https://bidoluelbise.com/sitemap/products/0.xml").get();
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

//
//            driver.get("https://bidoluelbise.com/kadin-asimetrik-kesim-kruvaze-yaka-uzun-kol-elbise-zumrut");
//            parseItemFull(driver);



        for (String link : links) {
            driver.get(link);
            System.out.println("Programma idet  " + link.toString());
            Thread.sleep(300);
            parseItemFull(driver);
            String linktt = driver.getCurrentUrl();
            System.out.println(linktt);
        }



        WebDriverSingleton.closeDriver(); // Закрываем драйвер в блоке finally, чтобы убедиться, что он закроется

        System.out.println("driver is closed");
        ItemUtils.removeItemsWithEmptyVariants(productList);
        convertListToXMLAndSaveToFile(productList, "BiDoluElbise.xml");


    }

    public static void parseItemFull(WebDriver driver) throws InterruptedException, UnsupportedEncodingException {

        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the Item class to store product details
        Item productBiDoluElbise = new Item();


        var productName = doc.selectXpath(productNameXPath);
        productBiDoluElbise.setProductName(productName.text());

        var category = doc.selectXpath(categoryXPath);
        productBiDoluElbise.setCategoryName(category.text());


        var discountPrice = doc.selectXpath(discountPriceXPath);
        double testDiscountPrice  = extractNumber(discountPrice.text());
        productBiDoluElbise.setItemDiscountPrice(testDiscountPrice);

        var fullPrice = doc.selectXpath(fullPriceXPath);
        double testFullPrice = extractNumber(fullPrice.text());
        productBiDoluElbise.setItemFullPrice(testFullPrice);

        var description = doc.selectXpath(descriptionXPath);
        productBiDoluElbise.setDescription(description.text());

       String testStockCode =  driver.getCurrentUrl();
       productBiDoluElbise.setItemStockCode(generateStockCode(testStockCode));

       productBiDoluElbise.setItemUrl(driver.getCurrentUrl());
       productBiDoluElbise.setCompany("BiDoluElbise");

        List<WebElement> sizeNames = driver.findElements(By.xpath("//div[@class='eksecenekLine kutuluvaryasyon']//span[@class='right_line']//span"));
        for (WebElement variantName : sizeNames) {
            variantName.click();
            Thread.sleep(150);
            SubProduct subProduct = parseVariantOfProduct(driver);  // Создаем новый объект SubProduct
            productBiDoluElbise.getSubProducts().add(subProduct);// Create a new SubProduct object

        }
       productList.add(productBiDoluElbise);


    }
    public static SubProduct parseVariantOfProduct(WebDriver driver)throws InterruptedException {

        Document doc = Jsoup.parse(driver.getPageSource());

        SubProduct subProduct = new SubProduct();

        var discountPrice = doc.selectXpath(discountPriceXPath);
        double testDiscountPrice  = extractNumber(discountPrice.text());
        subProduct.setDiscountPrice(testDiscountPrice);

        var fullPrice = doc.selectXpath(fullPriceXPath);
        double testFullPrice = extractNumber(fullPrice.text());
        subProduct.setFullPrice(testFullPrice);

        List<String> images = sliderPhoto(driver);
        subProduct.setPictureUrls(images);

        var nameSize = doc.selectXpath("//span[@class='size_box selected']");
        String size =nameSize.text();
        String testStockCode =  driver.getCurrentUrl();
        String sku = generateStockCode(testStockCode);
        subProduct.setVariantStockCode(sku + size);

        subProduct.setQuantity(2);

        List<Attribute> attributeList = parseAndSetPropertiesAttribute(driver);
        subProduct.setAttributes(attributeList);


        return subProduct;
    }

    public static List<Attribute> parseAndSetPropertiesAttribute(WebDriver driver) throws InterruptedException {
        Document doc = Jsoup.parse(driver.getPageSource());
        List<Attribute> attributeList = new ArrayList<>();

        Attribute attribute = new Attribute();

        WebElement element = driver.findElement(By.xpath("//span[@class='size_box selected']"));
        attribute.setSize(element.getText());
        System.out.println(element.getText() + "Название варианта вот такое смотри");
        // Добавляем sizeVariant в attributeList
        attributeList.add(attribute);
        return attributeList;
    }


    public static List<String> sliderPhoto(WebDriver driver) throws InterruptedException {
        List<String> imageUrls = new ArrayList<>();
        List<WebElement> allImages = driver.findElements(By.xpath("        //div[@class='AltImgCapSmallImg']//img"));

        for (WebElement img : allImages) {
            String srcValue = img.getAttribute("src");
            srcValue = transformImageUrl(srcValue);  // тут происходит трансформация
            if (!imageUrls.contains(srcValue)) {
                imageUrls.add(srcValue);
            }
        }
        return imageUrls;
    }

    public static String transformImageUrl(String url) {
        Pattern pattern = Pattern.compile("(width=)(\\d+)(,)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.replaceAll("$1" + "500" + "$3");
        }
        return url;
    }

        public static double extractNumber(String input) {
        // Удаляем все нечисловые символы, кроме точек и запятых
        input = input.replaceAll("[^\\d,\\.]", "");

        // Заменяем точки на пустые строки (удаляем их)
        input = input.replace(".", "");

        // Заменяем запятые на точки
        input = input.replace(',', '.');

        try {
            return Double.parseDouble(input) ; // Преобразуем в double
        } catch (NumberFormatException e) {
            return 0; // В случае ошибки преобразования возвращаем 0
        }
    }


    public static String generateStockCode(String url) {
        // Удаление базовой части URL
        url = url.replace("https://bidoluelbise.com", "").replace("http://bidoluelbise.com", "");
        String path = url.substring(url.indexOf('/'));

        // Получение последних трех слов
        String[] words = path.split("-");
        String lastThreeWords = String.join("-", Arrays.copyOfRange(words, Math.max(words.length - 3, 0), words.length));

        // Получение хэш-кода
        int hashCode = path.hashCode();

        // Формирование stockCode
        return lastThreeWords + "-" + hashCode;
    }

}
