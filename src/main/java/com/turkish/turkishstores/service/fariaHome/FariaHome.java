package com.turkish.turkishstores.service.fariaHome;

import com.turkish.turkishstores.service.general.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.turkish.turkishstores.service.general.WebDriverSingleton.getDriver;
import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXMLAndSaveToFile;

public class FariaHome {

 private static WebDriver driver = getDriver();
    private static  By buttonClickForRightVariantsXPath  = By.xpath("//div[@class='mb-4']//div[@class='slick-arrow slick-next']");
    private static By amountPicturesOnWebPage = By.xpath("//div[@class='slider-container']//img");
    private  static By variant =  By.xpath("//span[@class='variant-name']");
    private static By justVariantOnThePageWhereNotColor = By.xpath("//div[@class='mb-4']//span[@class='variant-name']");
    private static String productNameXPath = "//div[@class='product-name-main w-11/12']//h1[@class='product-name']";
    private static String categoryNameXPath = "//div[@class='breadcrumbs mb-4 pt-4 px-4']//li//a";
    private static String descriptionXPath = "//div[@class='tab-content']";
    private static String discountPriceXPath = "//div[@class='product-detail-page-detail-price-box flex items-center mt-4 ']//div[@class='discount-price-main flex flex-row']//div[@class='flex discount-price flex-col']//span[@style][2]";
    private static String fullPriceXPath = "//div[@class='product-detail-page-detail-price-box flex items-center mt-4 ']//div[@class='discount-price-main flex flex-row']//div[@class='flex discount-price flex-col']//span[@style][1]";
    private static String variantStockCodeXPath = "//div[@class='categories-detail mt-4']//span[@style][2]";
    private static By outOfStock = By.xpath("//button[@class='add-to-cart flex-1  out-of-stock disabled  ']");
    private static List<Item> productList = new ArrayList<>();

    static LinkedList<String> links = new LinkedList<>();


    public static void main(String[] args) throws InterruptedException, IOException {

        try {
            // Загрузка XML-файла
            Document doc = Jsoup.connect("https://faryahome.com/products.xml").get();
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


//            getDriver();
//            driver.get("https://www.parlakids.com.tr/kiz-cocuk-berlin-sari-pamuklu-firfirli-baglama-detayli-tulum-salopet-toka-takim-33");
            try {


                WebDriverSingleton.getDriver();

                boolean isFirstLink = true;

                for (String link : links) {
                    if (isFirstLink) {
                        isFirstLink = false;
                        continue;
                    }
                    driver.get(link);
                    System.out.println("Programma idet" + link.toString());
//              код для парсинга содержимого страницы
                    if (!driver.findElements(outOfStock).isEmpty()) {
                        continue;
                    }
//                }


                List<WebElement> colorVariantName = driver.findElements(By.xpath("//div[@class='mb-4']//div[@data-index]"));
//                    driver.findElement(By.xpath("//div[@class='cookie-bar is-left']//span")).click();
                    System.out.println(colorVariantName.toString());


                parseItemFull(driver);

                String linktt = driver.getCurrentUrl();
                System.out.println(linktt);

            }
//
            }//открой для полного цикла
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        ItemUtils.removeItemsWithEmptyVariants(productList);
        convertListToXMLAndSaveToFile(productList, "FaryaHome.xml");
            WebDriverSingleton.closeDriver(); // Закрываем драйвер в блоке finally, чтобы убедиться, что он закроется


        System.out.println("driver is closed");


    }

    /**
     * This method extracts detailed information about a product from a webpage.
     *
     * @param driver - A WebDriver instance to interact with the web page.
     */
    public static void parseItemFull(WebDriver driver) throws InterruptedException {

        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the ProductFariaHome class to store product details
        Item productFariaHome = new Item();


        // Extract the product's name from the webpage
        var productName = doc.selectXpath(productNameXPath);
        productFariaHome.setProductName(productName.text());

        // Extract the category name from the webpage
        var categoryName = doc.selectXpath(categoryNameXPath);

        // Check if the product name contains the word "Erkek". If it does, prefix the category name with "erkek".
        if (productName.text().contains("Erkek")) {
            productFariaHome.setCategoryName("erkek >" + categoryName.text());
        } else {
            productFariaHome.setCategoryName("kadin > " + categoryName.text());
        }

        // Extract the product's description from the webpage
        var description = doc.selectXpath(descriptionXPath);
        productFariaHome.setDescription(description.text());

        var fullPrice = doc.selectXpath(fullPriceXPath);
        Double fullPriceValue = Double.valueOf(extractPrice(fullPrice.text()));
        productFariaHome.setItemFullPrice(Double.parseDouble(String.valueOf(fullPriceValue)));

        // Extract the discount price (if available) of the sub-product from the webpage
        var discountPrice = doc.selectXpath(discountPriceXPath);
        String discountPriceValue = String.valueOf(extractPrice(discountPrice.text()));
        productFariaHome.setItemDiscountPrice(Double.parseDouble(discountPriceValue));


        // Get the current page's URL and extract the stock code from it
        String url = driver.getCurrentUrl();
        String itemStockCode = extractStockCodeItemFromUrl(url);
        productFariaHome.setItemStockCode(itemStockCode);

        productFariaHome.setItemUrl(url);

        productFariaHome.setCompany("Farya Home");
        // Find all color variants for the product on the webpage

        Set<String> processedStockCodes = new HashSet<>();
            List<WebElement> colorVariantName = driver.findElements(By.xpath("//div[@class='mb-4']//div//img[@title]"));

        if (colorVariantName.isEmpty()) {
            // Код для работы с размерами, если элементы цвета не найдены
            List<WebElement> sizeVariants = driver.findElements(justVariantOnThePageWhereNotColor);
            for (WebElement sizeVariant : sizeVariants) {


                try {
                    sizeVariant.click();
                } catch (WebDriverException e) {
                    System.out.println("Не удалось нажать на элемент из-за ошибки: " + e.getMessage());
                    continue;
                }
                SubProduct subProduct = parseAndSetPropertiesSubproduct(driver);
                productFariaHome.getSubProducts().add(subProduct);
                Thread.sleep(1000);
            }
        } else {




//            for (int i = 1; i < colorVariantName.size(); i++) {
            for (WebElement colorButton : colorVariantName) {

//                if (i == 3) {
//                    try {
//                        // Try clicking the button to view more color variants if available
//                        driver.findElement(buttonClickForRightVariantsXPath).click();
//                    } catch (WebDriverException e) {
//                        System.out.println("The element was not found on the page!");
//                        continue;  // Skip to the next iteration of the loop
//                    }
//                }

                try {
                    // Try clicking on the next color variant to extract its details
                    colorButton.click();
                } catch (WebDriverException e) {
                    System.out.println("Failed to click on the element due to an error: " + e.getMessage());
//                    continue;  // Skip to the next iteration of the loop
                }

                List<WebElement> sizeVariants = driver.findElements(variant);

                for (WebElement sizeVariant : sizeVariants) {
                    try {
                        sizeVariant.click();
                        doc = Jsoup.parse(driver.getPageSource());

                    } catch (WebDriverException e) {

                        System.out.println("Не удалось нажать на элемент из-за ошибки: " + e.getMessage());
                        continue;  // продолжает следующую итерацию цикла
                    }


                    var variantStockCode = doc.selectXpath(variantStockCodeXPath);

// Проверьте, присутствует ли variantStockCode в processedStockCodes
                    if (processedStockCodes.contains(variantStockCode.text())) {
                        // Если variantStockCode уже присутствует, пропустите этот цветовой вариант
                        continue;
                    } else {
                        // Если variantStockCode не присутствует, добавьте его в processedStockCodes и продолжите обработку
                        processedStockCodes.add(variantStockCode.text());
                        SubProduct subProduct = parseAndSetPropertiesSubproduct(driver);  // Create a new SubProduct object
                        productFariaHome.getSubProducts().add(subProduct);
                    }


                    Thread.sleep(290); // задержка, чтобы страница успела обновиться
                    // Extract detailed information about each color variant and store it in the SubProduct object

                }
            }
        }

        // Print the total number of color variants found
        System.out.println(colorVariantName.size() + "  Size of the array with variants");

        // Extract details for the last color variant and store it in the SubProduct object

        // Add the extracted product details to the productList
        productList.add(productFariaHome);
    }


    /**
     * This method extracts detailed sub-product information from a webpage.
     *
     * @param driver - A WebDriver instance to interact with the web page.
     * @return SubProduct - An instance containing details of the sub-product.
     */
    /**
     * This method extracts detailed sub-product information from a webpage.
     *
     * @param driver - A WebDriver instance to interact with the web page.
     * @return SubProduct - An instance containing details of the sub-product.
     */
    public static SubProduct parseAndSetPropertiesSubproduct(WebDriver driver) throws InterruptedException {
        // Parse the current webpage's content using Jsoup
        Document doc = Jsoup.parse(driver.getPageSource());

        // Create an instance of the SubProduct class to store sub-product details
        SubProduct subProduct = new SubProduct();

        // Extract the full price of the sub-product from the webpage
        var fullPrice = doc.selectXpath(fullPriceXPath);
        String fullPriceValue = String.valueOf(extractPrice(fullPrice.text()));
        subProduct.setFullPrice(Double.parseDouble(fullPriceValue));

        // Extract the discount price (if available) of the sub-product from the webpage
        var discountPrice = doc.selectXpath(discountPriceXPath);
        String discountPriceValue = String.valueOf(extractPrice(discountPrice.text()));
        subProduct.setDiscountPrice(Double.parseDouble(discountPriceValue));

        // Extract the currency symbol from the full price
        String currency = extractCurrencySymbol(fullPrice.text());
        subProduct.setCurrency(currency);

        // Get the list of image URLs associated with this sub-product
        List<String> images = sliderPhoto(driver);
        subProduct.setPictureUrls(images);

        // Extract the list of variant attributes (like color, size, etc.) for the sub-product
        List<Attribute> variantList = parseAndSetPropertiesAttribute(driver);
        subProduct.setAttributes(variantList);

        // Get the current page's URL and extract the color variant from it
        var variantStockCode = doc.selectXpath(variantStockCodeXPath);
        subProduct.setVariantStockCode(variantStockCode.text());
        subProduct.setQuantity(2);

        return subProduct; // Return the populated subProduct instance
    }






    public static List<Attribute> parseAndSetPropertiesAttribute(WebDriver driver) throws InterruptedException {



        List<WebElement> sizeVariants = driver.findElements(variant);
        List<Attribute> attributeList = new ArrayList<>();


            Document doc = Jsoup.parse(driver.getPageSource());
            var variantSize = doc.selectXpath("//div[@class='py-1 px-4 mr-2 mb-2 variant-types relative border-transparent border-2 cursor-pointer selected-circle  ']");

            String url = driver.getCurrentUrl();


        WebElement colorName = null;
        try {
            colorName = driver.findElement(By.xpath("//div[@class='mb-4']//div[@class='py-1 px-4 mr-2 mb-2 relative border-transparent cursor-pointer first-image-circle']//img[@title]"));
        } catch (NoSuchElementException e) {
            System.out.println("Элемент с цветом не найден");
        }

        if (colorName != null) {
            String titleColor = colorName.getAttribute("title");

            Attribute colorVariant = new Attribute();
            colorVariant.setColor(titleColor);

            // Добавляем цвет, только если его еще нет в списке
            if (!containsAttribute(attributeList, "color", titleColor)) {
                System.out.println(titleColor + "CCCCCCCCCCCCCCCCCCCCC");
                attributeList.add(colorVariant);
            }
        }


            Attribute sizeVariant = new Attribute();
            sizeVariant.setSize(variantSize.text());

            // Добавляем размер, только если его еще нет в списке
            if (!containsAttribute(attributeList, "size", variantSize.text())) {
                attributeList.add(sizeVariant);
            }


            // Добавляем цвет, только если его еще нет в списке


        return attributeList;
    }



    public static List<String> sliderPhoto(WebDriver driver) throws InterruptedException {
        List<WebElement> pictures;
        List<WebElement> amountPictures = driver.findElements(amountPicturesOnWebPage);
        System.out.println(amountPictures.size());
        int amount = amountPictures.size();

        List<String> imageUrls = new ArrayList<>();

        List<WebElement> allImages = driver.findElements(By.xpath("        //img[@alt='Image']"));

            for (WebElement img : allImages) {
                String srcValue = img.getAttribute("src");
                if (!imageUrls.contains(srcValue)&&(!srcValue.startsWith("data:image/gif;base64,"))) {
                    Thread.sleep(100);
                    imageUrls.add(srcValue);
                }
            }
        System.out.println(imageUrls.size()+ "  Размер массива" + imageUrls.toString());
        return imageUrls;
    }

    public void сloseDriver() {
        driver.close();
    }





    public static String extractStockCodeItemFromUrl(String url) {
        Pattern pattern = Pattern.compile("https://faryahome.com/([^?]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String stockCodeItem = matcher.group(1);  // dreamy-uzun-kollu-desenli-dugmeli-yaka-pijama-takimi

            // Обрезаем строку до 47 символов, если она длиннее
            if (stockCodeItem.length() > 47) {
                stockCodeItem = stockCodeItem.substring(0, 47);
            }

            return stockCodeItem;
        }
        return null;
    }



    public static double extractPrice(String input) {
        Pattern pattern = Pattern.compile("([-]?\\d+[,.]\\d+)"); // Регулярное выражение для числа с плавающей точкой или запятой
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String matched = matcher.group(1).replace(',', '.'); // Замена запятой на точку
            return Double.parseDouble(matched )*1.15 + 50 ;
        } else {
            return 0; // Возвращаем 0, если число не найдено
        }
    }



    public static String extractCurrencySymbol(String input) {
        Pattern pattern = Pattern.compile("[^\\d.]+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return "TRY";  // Заменяем символ валюты на "TRY"
        }
        return input;
    }


    private static boolean containsAttribute(List<Attribute> variants, String name, String value) {
        return variants.stream()
                .anyMatch(variant -> variant.getName().equals(name) && variant.getValue().equals(value));
    }

//Теперь вы можете использовать метод convertListToXML, чтобы получить XML-строку из списка ProductFariaHome.

    //Примечание: Приведенные примеры представляют базовое использование JAXB. В зависимости от ваших потребностей вы можете настроить преобразование, добавив дополнительные аннотации или параметры.


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