package com.turkish.turkishstores.service.fariaHome;

import com.turkish.turkishstores.service.Products;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FariaHome {
    private static WebDriver driver;

    public FariaHome(WebDriver driver) {
        this.driver = driver;
    }

   private static  By buttonClickForRightVariants  = By.xpath("//div[@class='mb-4']//div[@class='slick-arrow slick-next']");

    private static By sliderForNormalItems = By.xpath("//div[@class='slider']//div[@style='width: 120px; height: 120px; margin-bottom: 10px;'] | //div[@class='slider']//div[@style='width:120px;height:120px;margin-bottom:10px']");

    private static By amountPicturesOnWebPage = By.xpath("//div[@class='slider-container']//img");

    private  static By variant =  By.xpath("//span[@class='variant-name']");
    private static By outOfStock = By.xpath("//button[@class='add-to-cart flex-1  out-of-stock disabled  ']");


   private static List<Item> productList = new ArrayList<>();

    static LinkedList<String> links = new LinkedList<>();

    static List<String> proxies = Arrays.asList("116.202.103.205:12607", "23.88.101.136:12490", "49.12.208.116:12320"); // Замените на свои значения

    static long startTime = System.currentTimeMillis(); // время начала
    static long proxyChangeTime = 1 * 60 * 1000; // 5 минут в миллисекундах

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


//
//        setWebDriverProxy();
//        driver.get("https://faryahome.com/dreamy-uzun-kollu-desenli-dugmeli-yaka-pijama-takimi");

////        for (String proxy : proxies) {  потом доделаем как нибудь
//            try {


                setWebDriverProxy();

                boolean isFirstLink = true;

                for (String link : links) {
                    if (isFirstLink) {
                        isFirstLink = false;
                        continue;
                    }
                    driver.get(link);
                    System.out.println("Programma idet" + link.toString());
//                    if (System.currentTimeMillis() - startTime > proxyChangeTime) {
//                        executeVPNCommand(); // выходим из цикла ссылок, чтобы сменить прокси
//                    }
//              код для парсинга содержимого страницы
                    if (!driver.findElements(outOfStock).isEmpty()) {
                        continue;
                    }
//                }


                List<WebElement> colorVariantName = driver.findElements(By.xpath("//div[@class='mb-4']//div[@data-index]"));

                driver.findElement(By.xpath("//div[@class='cookie-bar is-left']//span")).click();

                System.out.println(colorVariantName.toString());


                parseItemFull(driver);

                String linktt = driver.getCurrentUrl();
                System.out.println(linktt);

//            }
//            catch (Exception e) {
//                System.out.println("Ошибка при использовании прокси " + proxy + ". Причина: " + e.getMessage());
//                continue;
//            }
                startTime = System.currentTimeMillis(); // обновляем время начала для следующего прокси

            }//открой для полного цикла



            System.out.println("driver is closed");
            convertListToXML(productList);
            convertListToXMLAndSaveToFile(productList, "file.xml");

        driver.close();

    }//div[@data-index]//div[@class='slide']//img[@src]





    public static void executeVPNCommand() {
        String command = "wg-quick up myvpn";
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void setWebDriverProxy() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
//        options.addArguments("--incognito=new");
//        options.addArguments("--proxy-server=" + proxy);

        if (driver != null) {
            driver.quit();
        }
        driver = new ChromeDriver(options);
//        driver.manage().window().maximize();
        driver.manage().window().setSize(new Dimension(1980, 1080));
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
        var productName = doc.selectXpath("//div[@class='product-name-main w-11/12']//h1[@class='product-name']");
        productFariaHome.setProductName(productName.text());

        // Extract the category name from the webpage
        var categoryName = doc.selectXpath("//div[@class='breadcrumbs mb-4 pt-4 px-4']//li//a");

        // Check if the product name contains the word "Erkek". If it does, prefix the category name with "erkek".
        if (productName.text().contains("Erkek")) {
            productFariaHome.setCategoryName("erkek " + categoryName.text());
        } else {
            productFariaHome.setCategoryName("kadin  " + categoryName.text());
        }

        // Extract the product's description from the webpage
        var description = doc.selectXpath("//div[@class='tab-content']");
        productFariaHome.setDescription(description.text());

        // Get the current page's URL and extract the stock code from it
        String url = driver.getCurrentUrl();
        String itemStockCode = extractStockCodeItemFromUrl(url);
        productFariaHome.setItemStockCode(itemStockCode);

        // Find all color variants for the product on the webpage
        List<WebElement> colorVariantName = driver.findElements(By.xpath("//div[@class='mb-4']//div[@data-index]"));
        for (int i = 1; i < colorVariantName.size(); i++) {
            if (i == 3) {
                try {
                    // Try clicking the button to view more color variants if available
                    driver.findElement(buttonClickForRightVariants).click();
                } catch (WebDriverException e) {
                    System.out.println("The element was not found on the page!");
                    continue;  // Skip to the next iteration of the loop
                }
            }
            // Extract detailed information about each color variant and store it in the SubProduct object
            SubProduct subProduct = parseAndSetPropertiesSubproduct(driver);  // Create a new SubProduct object
            productFariaHome.getSubProducts().add(subProduct);
            try {
                // Try clicking on the next color variant to extract its details
                colorVariantName.get(i).click();
            } catch(WebDriverException e) {
                System.out.println("Failed to click on the element due to an error: " + e.getMessage());
                continue;  // Skip to the next iteration of the loop
            }
        }

        // Print the total number of color variants found
        System.out.println(colorVariantName.size() + "  Size of the array with variants");

        // Extract details for the last color variant and store it in the SubProduct object
        SubProduct subProduct = parseAndSetPropertiesSubproduct(driver);
        productFariaHome.getSubProducts().add(subProduct);

        // Add the extracted product details to the productList
        productList.add(productFariaHome);
    }


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
        var fullPrice = doc.selectXpath("//div[@class='product-detail-page-detail-price-box flex items-center mt-4 ']//div[@class='discount-price-main flex flex-row']//div[@class='flex discount-price flex-col']//span[@style][1]");
        String fullPriceValue = extractPrice(fullPrice.text());
        subProduct.setFullPrice(fullPriceValue);

        // Extract the discount price (if available) of the sub-product from the webpage
        var discountPrice = doc.selectXpath("//div[@class='product-detail-page-detail-price-box flex items-center mt-4 ']//div[@class='discount-price-main flex flex-row']//div[@class='flex discount-price flex-col']//span[@style][2]");
        String discountPriceValue = extractPrice(discountPrice.text());
        subProduct.setDiscountPrice(discountPriceValue);

        // Extract the currency symbol from the full price
        String currency = extractCurrencySymbol(fullPrice.text());
        subProduct.setCurrency(currency);

        // Get the list of image URLs associated with this sub-product
        List<String> images = sliderPhoto(driver);
        subProduct.setPictureUrls(images);

        // Extract the list of variant attributes (like color, size, etc.) for the sub-product
        List<Variant> variantList = parseAndSetPropertiesAttribute(driver);
        subProduct.setVariants(variantList);

        // Get the current page's URL and extract the color variant from it
        String url = driver.getCurrentUrl();
        String titleColor = extractStockCodeVariantFromUrl(url);
        subProduct.setColor(titleColor);

        return subProduct; // Return the populated subProduct instance
    }






    public static List<Variant> parseAndSetPropertiesAttribute(WebDriver driver) throws InterruptedException {
        List<WebElement> sizeVariants = driver.findElements(variant);
        List<Variant> variantList = new ArrayList<>();

        for (int i = 0; i < sizeVariants.size(); i++) {
            try {
                sizeVariants.get(i).click();
            } catch(WebDriverException e) {
                System.out.println("Не удалось нажать на элемент из-за ошибки: " + e.getMessage());
                continue;  // продолжает следующую итерацию цикла
            }
            Thread.sleep(100); //  задержка, чтобы страница успела обновиться

            Document doc = Jsoup.parse(driver.getPageSource());
            var stockCode = doc.selectXpath("//div[@class='categories-detail mt-4']//span[@style][2]");
            var variantSize = doc.selectXpath("//div[@class='py-1 px-4 mr-2 mb-2 variant-types relative border-transparent border-2 cursor-pointer selected-circle  ']");

            Variant variant = new Variant();
//            variant.setStockCode(stockCode.text());
            String url = driver.getCurrentUrl();
            String titleColor = extractColorFromUrl(url);
            variant.setSize(variantSize.text());
            variant.setColor(titleColor);

            variantList.add(variant);
        }
        return variantList;
    }



    public static List<String> sliderPhoto(WebDriver driver) throws InterruptedException {
        List<WebElement> pictures;
        List<WebElement> amountPictures = driver.findElements(amountPicturesOnWebPage);
        System.out.println(amountPictures.size());
        int amount = amountPictures.size();

        List<String> imageUrls = new ArrayList<>();

        if (amount >= 2 && amount <= 4) {
            pictures = driver.findElements(sliderForNormalItems);
            for (int i = 0; i < pictures.size(); i++) {
                if (i >= 1) {
                    pictures.get(i).click();
                    Thread.sleep(330);
                }

                Document doc = Jsoup.parse(driver.getPageSource());
                var picture = doc.selectXpath("//div[@class='slick-slide slick-active slick-current']//div[@class='slick-slider-main relative product-detail-page-slider  crosshair']//img[@src]");
                String srcValue = picture.attr("src");
                System.out.println(srcValue);
                if (!imageUrls.contains(srcValue)) {
                    imageUrls.add(srcValue);
                }
            }
        } else if (amount > 4) {
            List<WebElement> allImages = driver.findElements(By.xpath("//div[@data-index]//div[@class='slide']//img[@src]"));

            for (WebElement img : allImages) {
                String srcValue = img.getAttribute("src");
                if (!imageUrls.contains(srcValue)&&(!srcValue.startsWith("data:image/gif;base64,"))) {
                    Thread.sleep(100);
                    imageUrls.add(srcValue);
                }
            }
        } else {
            List<WebElement> allImages = driver.findElements(By.xpath("//div[@class='slick-slider-main relative product-detail-page-slider  crosshair']//img "));

            for (WebElement img : allImages) {
                String srcValue = img.getAttribute("src");
                if (!imageUrls.contains(srcValue)) {
                    imageUrls.add(srcValue);
                }
            }
        }



        System.out.println(imageUrls.size()+ "  Размер массива" + imageUrls.toString());
        return imageUrls;
    }

    public void сloseDriver() {
        driver.close();
    }


    public static String convertListToXML(List<Item> list) {
        try {
            JAXBContext context = JAXBContext.newInstance(Products.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            Products products = new Products();
            products.setProductList(list);

            StringWriter writer = new StringWriter();
            marshaller.marshal(products, writer);

            System.out.println(writer);
            return writer.toString();
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void convertListToXMLAndSaveToFile(List<Item> list, String filePath) {
        String xmlContent = convertListToXML(list);
        if (xmlContent != null) {
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(xmlContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String extractColorFromUrl(String url) {
        Pattern pattern = Pattern.compile("renk=([^&]*)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    public static String extractStockCodeVariantFromUrl(String url) {
        Pattern pattern = Pattern.compile("com\\/(.*?)\\?.*?renk=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String part1 = matcher.group(1);  // dreamy-uzun-kollu-desenli-dugmeli-yaka-pijama-takimi
            String part2 = matcher.group(2);  // lacivert
            String stockCodeVariant = part1+"-" + part2;
            return stockCodeVariant;

        }
        return null;
    }

    public static String extractStockCodeItemFromUrl(String url) {
        Pattern pattern = Pattern.compile("https://faryahome.com/([^?]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String stockCodeItem = matcher.group(1);  // dreamy-uzun-kollu-desenli-dugmeli-yaka-pijama-takimi

            System.out.println(stockCodeItem + "  MEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
            return stockCodeItem;

        }
        return null;
    }

    public static String extractPrice(String input) {
        Pattern pattern = Pattern.compile("(\\d+\\.\\d{2})");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);  // 219.99
        }
        return null;
    }


    public static String extractCurrencySymbol(String input) {
        Pattern pattern = Pattern.compile("[^\\d.]+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return "TRY";  // Заменяем символ валюты на "TRY"
        }
        return input;
    }




//Теперь вы можете использовать метод convertListToXML, чтобы получить XML-строку из списка ProductFariaHome.

    //Примечание: Приведенные примеры представляют базовое использование JAXB. В зависимости от ваших потребностей вы можете настроить преобразование, добавив дополнительные аннотации или параметры.






}
//        var productName = doc.selectXpath("//div[@class='product-name-main w-11/12']//h1[@class='product-name']");
//        System.out.println(productName.text());
//        var fullPrice = doc.selectXpath("//div[@class='product-detail-page-detail-price-box flex items-center mt-4 ']//div[@class='discount-price-main flex flex-row']//div[@class='flex discount-price flex-col']//span[@style][1]");
//        System.out.println(fullPrice.text());
//        var discountPrice = doc.selectXpath("//div[@class='product-detail-page-detail-price-box flex items-center mt-4 ']//div[@class='discount-price-main flex flex-row']//div[@class='flex discount-price flex-col']//span[@style][2]");
//        System.out.println(discountPrice.text());
//        var categoryName = doc.selectXpath("//div[@class='breadcrumbs mb-4 pt-4 px-4']//li//a");
//        System.out.println(categoryName.text());
//        var description = doc.selectXpath("//div[@class='tab-content']");
//        System.out.println(description.text());
//        var amountVariants = doc.selectXpath("//div[@class='py-1 px-4 mr-2 mb-2 relative border-transparent cursor-pointer ']");
//        System.out.println();
//        sliderPhoto(driver);
//        parseAndSetPropertiesAttribute(driver);
//        var colorVariant = doc.selectXpath("//span[@style]//img[@title]");
//        String titleColor = colorVariant.attr("title");
//        System.out.println(titleColor);

