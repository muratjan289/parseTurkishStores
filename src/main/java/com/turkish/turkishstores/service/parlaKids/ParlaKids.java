package com.turkish.turkishstores.service.parlaKids;

import com.turkish.turkishstores.service.general.Attribute;
import com.turkish.turkishstores.service.general.Item;
import com.turkish.turkishstores.service.general.SubProduct;
import com.turkish.turkishstores.service.general.WebDriverSingleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.turkish.turkishstores.service.general.WebDriverSingleton.getDriver;
import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXML;
import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXMLAndSaveToFile;

public class ParlaKids {



    private static WebDriver driver = getDriver();


    private static List<Item> productList = new ArrayList<>();
    private static By productName = By.xpath("//h1//span");
    private static String variantStockCodeXPath = "//span[@class='productcode']";
    private static String quantityXPath ="//span[@id='divUrunStokAdediIcerik']";

    private static String priceXPath= "//span[@id='fiyat']//span[@class='spanFiyat'] | //span[@id='fiyat2']//span[@class='spanFiyat']";
    static String textToRemove = "14 GÜN İÇİNDE İADE DEĞİŞİM YAPILMAKTADIR." +
            "INSTAGRAM YADA WHATSAPP 0544 699 54 51 NUMARALI HATTIMIZDAN BİZE ULAŞARAK İADE & DEĞİŞİM KODU ALIP TARAFIMIZA ANLAŞMALI KARGOMUZ İLE GÖNDERMENİZ GEREKMEKTEDİR.\n" +
            "DİĞER KARGO ŞİRKETLERİNDEN TARAFIMIZA KARŞI ÖDEMELİ GÖNDERİLEN KARGOLAR KABUL EDİLMEMEKTEDİR" +
            "YIKANMIŞ , KULLANILMIŞ VE TEKRAR SATIŞA UYGUN OLMAYAN ÜRÜNLERİN İADE VE DEĞİŞİMİ KABUL EDİLMEMEKTEDİR.";



    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {


        getDriver();
        driver.get("https://www.parlakids.com.tr/sitemap/products/0.xml");


        List<WebElement> links = driver.findElements(By.xpath("//a[@href]"));
        System.out.println(links.size());

        for (WebElement link : links) {
            System.out.println(link.getText());
        }
        List<String> linkUrls = new ArrayList<String>();

// Перебираем все найденные элементы
        for (WebElement link : links) {
            // Получаем значение атрибута href (URL) для каждой ссылки
            String href = link.getAttribute("href");
            // Добавляем URL в список строк
            linkUrls.add(href);
        }
        WebDriverSingleton.closeDriver();
//        WebDriver driver = WebDriverSingleton.getDriver();
//            driver.get("https://www.parlakids.com.tr/k%C4%B1z-%C3%A7ocuk-vanessa-bro%C5%9F-detayl%C4%B1-t%C3%BCvit-z%C3%BCmr%C3%BCt-uzun-kollu-elbise");
//        parseItemFull(driver);

        try {


        WebDriver driver = WebDriverSingleton.getDriver();


            for (String link : linkUrls) {

                driver.get(String.valueOf(link));
                System.out.println("Programma idet" + link.toString());
                parseItemFull(driver);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        removeItemsWithEmptyVariants();
                convertListToXML(productList);
                convertListToXMLAndSaveToFile(productList, "parlaKids.xml");





    }



    public static void parseItemFull(WebDriver driver) throws InterruptedException, UnsupportedEncodingException {

        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the Item class to store product details
        Item productParlakIds = new Item();



        // Extract the product name using XPath and set it in the product object
        var productName = doc.selectXpath("//h1//span");
        productParlakIds.setProductName(productName.text());

        // Extract the category name using XPath and set it in the product object
        var categoryName = doc.selectXpath("//li[@itemprop='itemListElement'][2]//a[@href]");
        productParlakIds.setCategoryName(categoryName.text());

        var price = doc.selectXpath(priceXPath);
        String testPrice = String.valueOf(extractNumber(price.text()));
        productParlakIds.setItemFullPrice(Double.parseDouble(testPrice));

        // Define a locator for the stock quantity element
        By elementLocator = By.xpath("//span[@id='divUrunStokAdediIcerik' and normalize-space(text())='0']");
        // Check if the element is present on the page
        List<WebElement> elements = driver.findElements(elementLocator);
        System.out.println(elements.size() + "                SIZE ARRAY");

        // If the list is empty, the element is not on the page, and we can continue with the loop
        //Элемент которорый подразумевается это элемент который ломает весь код
        if (elements.isEmpty()) {
            // Extract the product description using XPath and set it in the product object
            var description = doc.selectXpath("//div[@id='divTabOzellikler']");
            String descriptionStr = description.text();
// Использование статического метода для удаления текста
            descriptionStr = removeTextFromDescription(String.valueOf(description), textToRemove);

// Установка обновленного описания для продукта
            productParlakIds.setDescription(String.valueOf(description));

            // Set the company name as "Parla Kids"
            productParlakIds.setCompany("Parla Kids");


            // Retrieve the current URL, decode it, and set it in the product object
            String linkForItem = driver.getCurrentUrl();
            String decodedURL = URLDecoder.decode(linkForItem, "UTF-8");
            productParlakIds.setItemUrl(decodedURL);

            // Extract the stock code from the URL and set it in the product object
            String stockCode = removeBaseUrl(decodedURL);
            productParlakIds.setItemStockCode(stockCode);

            // Initialize a SubProduct object to store variant information
            SubProduct subProduct = parseVariantOfProduct(driver);

            // Find all the variant names available on the page
            List<WebElement> variatNames = driver.findElements(By.xpath("//div[@class='eksecenekLine kutuluvaryasyon']//span/span[contains(@class, 'size_box') and not(contains(@class, 'nostok'))]\n"));

            // Iterate over each variant name element, click on it, and add the parsed subproduct to the product object
            Set<String> existingVariantStockCodes = new HashSet<>();

            for (WebElement variantName : variatNames) {
                subProduct = parseVariantOfProduct(driver);  // Создаем новый объект SubProduct

                // Проверяем, существует ли уже variantStockCode
                if (!existingVariantStockCodes.contains(subProduct.getVariantStockCode())) {
                    // Если нет, добавляем SubProduct в список продуктов
                    productParlakIds.getSubProducts().add(subProduct);
                    // И добавляем variantStockCode в множество, чтобы контролировать уникальность
                    existingVariantStockCodes.add(subProduct.getVariantStockCode());
                } else {
                    // Если такой stockCode уже есть, пропускаем добавление этого SubProduct
                    System.out.println("SubProduct с таким stockCode уже существует: " + subProduct.getVariantStockCode());
                }
                // Симулируем клик по имени варианта, чтобы потенциально обновить содержимое страницы
                variantName.click();
            }
        } else {
            // The element is present on the page, no need to execute the loop
            System.out.println("Элемент присутствует на странице, цикл пропущен.");
        }
        // Add the fully parsed product object to the product list
        productList.add(productParlakIds);
        // Print the product object for verification
        System.out.println(productParlakIds);
    }


    public static SubProduct parseVariantOfProduct(WebDriver driver)throws InterruptedException{

        Document doc = Jsoup.parse(driver.getPageSource());

        SubProduct subProduct = new SubProduct();

        var variantStockCode = doc.selectXpath(variantStockCodeXPath);
        String stockCode = transformStockCode(variantStockCode.text());
        subProduct.setVariantStockCode(stockCode);

        var quantity = doc.selectXpath(quantityXPath);
        subProduct.setQuantity(Integer.parseInt(quantity.text()));


        var price = doc.selectXpath(priceXPath);
        String testPrice = String.valueOf(extractNumber(price.text()));
        subProduct.setFullPrice(Double.parseDouble(testPrice));


        List<String> images = parsPhotos(driver);
        subProduct.setPictureUrls(images);

        List<Attribute> attributeList = parseAndSetPropertiesAttribute(driver);
        subProduct.setAttributes(attributeList);



        return subProduct ;
    }

    public static List<Attribute> parseAndSetPropertiesAttribute(WebDriver driver) throws InterruptedException {
        Document doc = Jsoup.parse(driver.getPageSource());
        List<Attribute> attributeList = new ArrayList<>();
        Attribute sizeVariant = new Attribute();
        var variantSize = doc.selectXpath("//span[@class='size_box selected']");
        String stockCode = variantSize.text();
        String transformedStockCode = transformStockCode(stockCode);
        sizeVariant.setSize(transformedStockCode);
        System.out.println();

        // Добавляем sizeVariant в attributeList
        attributeList.add(sizeVariant);
        return attributeList;
    }

    public static List<String> parsPhotos(WebDriver driver) throws InterruptedException {
        List<String> imageUrls = new ArrayList<>();
        List<WebElement> allImages = driver.findElements(By.xpath("//div[@class='thumb-list']//div//img"));

        for (WebElement img : allImages) {
            String srcValue = img.getAttribute("src");
            srcValue = transformImageUrl(srcValue);  // тут происходит трансформация
            System.out.println(srcValue);
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

    public static String transformStockCode(String input) {
        // Удаляем символы () и "Stok Kodu"
        String result = input.replaceAll("[()]", "").replace("Stok Kodu", "").trim();
        return result;
    }

    public static double extractNumber(String input) {
        Pattern pattern = Pattern.compile("([-]?\\d+[,.]\\d+)"); // Регулярное выражение для числа с плавающей точкой или запятой
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String matched = matcher.group(1).replace(',', '.'); // Замена запятой на точку
            return Double.parseDouble(matched);
        } else {
            throw new IllegalArgumentException("No number found in the given string");
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

    public static String removeBaseUrl(String url) {
        // Удаляем базовый URL
        String result = url.replaceFirst("https://www.parlakids.com.tr/", "");

        // Обрезаем до 47 символов, если длина строки больше
        if (result.length() > 47) {
            result = result.substring(0, 47);
        }

        return result;
    }

    public static String removeTextFromDescription(String description, String textToRemove) {
        return description.replace(textToRemove, "");
    }

    }
