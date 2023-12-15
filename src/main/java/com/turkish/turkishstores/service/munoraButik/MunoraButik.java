package com.turkish.turkishstores.service.munoraButik;

import com.turkish.turkishstores.service.general.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.turkish.turkishstores.service.xmlMethods.CreateXml.convertListToXMLAndSaveToFile;

public class MunoraButik {


    public static WebDriver driver = WebDriverSingleton.getDriver();
    private static LinkedList<String> links = new LinkedList<>();
    private static List<Item> productList = new ArrayList<>();

    private static  List<String> munoraLinks= new ArrayList<>();



    private static String productNameXPath = "//div[@class='ProductName']//h1//span";
    private static String discountPriceXPath = "//div[@class='sPric'] | //div[@id='pnlFiyatlar']//div[@id='divIndirimliFiyat']//span[@class='right_line indirimliFiyat']//span[@class='spanFiyat']";
    private static String fullPriceXPath = "//div[@id='pnlFiyatlar']//div[@class='Formline PiyasafiyatiContent']//span[@class='spanFiyat'] | //div[@id='pnlFiyatlar']//div[@class='Formline'][1]//span[@class='spanFiyat']";
    private static String descriptionXPath = "//div[@id='divTabOzellikler']";
    private static String categoryNameXPath = "//div[@class='proCategoryTitle categoryTitleText']//li[@itemprop='itemListElement'][position()=2 or position()=3]//span[@itemprop='name']";
    private static By photoXPath = By.xpath("//div[@class='thumb-list']//div[@class='thumb-item']//img");
    private static By selectedXPath = By.xpath("//span[@class='size_box selected']");

    public static void main(String[] args) throws InterruptedException, IOException {


            try {
                Document docSuvari = Jsoup.connect("https://munora.com.tr/sitemap.xml").get();
                Elements locProductLinks = docSuvari.select("loc:containsOwn(product)");
                for (Element locELementLink : locProductLinks) {
                    String linkk = locELementLink.text();
                    munoraLinks.add(linkk);
                }
                for (String link1 : munoraLinks){
                    // Загрузка XML-файла
                    Document doc = Jsoup.connect(link1).get();
                    // Извлечение всех элементов <loc>
                    Elements locElements = doc.select("loc");
                    // Добавление каждой ссылки в LinkedList
                    for (Element locElement : locElements) {
                        String link = locElement.text();

                            links.add(link);
                    }
                }
                // Вывод ссылок
                for (String link : links) {
                    System.out.println(link);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            WebDriverSingleton.getDriver();

//        driver.get("https://www.munora.com.tr/bilekten-gecmeli-nude-hakiki-deri-topuklu-babet-157");
//        parseItemFull(driver);

        int totalLinks = links.size();
        int linksProcessed = 0;

        for (String link : links) {
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

            ItemUtils.removeItemsWithEmptyVariants(productList);
            convertListToXMLAndSaveToFile(productList, "MunoraButik.xml");


        }


        public static void parseItemFull (WebDriver driver) throws InterruptedException {

            // Parse the current webpage's content using Jsoup
            Document doc = Jsoup.parse(driver.getPageSource());

            // Create an instance of the ProductFariaHome class to store product details
            Item productMunoraButik = new Item();


            var productName = doc.selectXpath(productNameXPath);
            productMunoraButik.setProductName(productName.text());

            var category = doc.selectXpath(categoryNameXPath);
            productMunoraButik.setCategoryName(category.text());


            var discountPrice = doc.selectXpath(discountPriceXPath);
            System.out.println(discountPrice.text() + "LOOK FOR PRICE");
            double testDiscountPrice = extractNumber(discountPrice.text());
            productMunoraButik.setItemDiscountPrice(testDiscountPrice);

            var fullPrice = doc.selectXpath(fullPriceXPath);
            System.out.println(fullPrice.text() + " REALNAYA CENA");
            double testFullPrice = extractNumber(fullPrice.text());
            productMunoraButik.setItemFullPrice(testFullPrice);
            System.out.println(testFullPrice + " FULL PRICE");


            var description = doc.selectXpath(descriptionXPath);
            productMunoraButik.setDescription(description.text());

            productMunoraButik.setItemStockCode(generateStockCode(driver.getCurrentUrl()));

            productMunoraButik.setCompany("MunoraButik");

            productMunoraButik.setItemUrl(driver.getCurrentUrl());


            //span[@data-stock='1']

            List<WebElement> sizeNames = driver.findElements(By.xpath("//span[number(@data-stock) >= 1]"));
            for (WebElement variantName : sizeNames) {
                clickElementUsingJS(driver, variantName);

                Thread.sleep(300);

                SubProduct subProduct = parseVariantOfProduct(driver);  // Создаем новый объект SubProduct
                productMunoraButik.getSubProducts().add(subProduct);// Create a new SubProduct object

            }


            productList.add(productMunoraButik);
        }

        public static SubProduct parseVariantOfProduct (WebDriver driver) throws InterruptedException {

            Document doc = Jsoup.parse(driver.getPageSource());

            SubProduct subProduct = new SubProduct();

            var discountPrice = doc.selectXpath(discountPriceXPath);
            double testDiscountPrice = extractNumber(discountPrice.text());
            subProduct.setDiscountPrice(testDiscountPrice);

            var fullPrice = doc.selectXpath(fullPriceXPath);
            double testFullPrice = extractNumber(fullPrice.text());
            subProduct.setFullPrice(testFullPrice);

            var selectedSize = doc.selectXpath("//span[@class='size_box selected']");

            subProduct.setVariantStockCode(generateStockCode(driver.getCurrentUrl() + selectedSize.text()));

            subProduct.setQuantity(2);


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

            WebElement element = driver.findElement(selectedXPath);
            attribute.setSize(element.getText());
            System.out.println(element.getText() + "Название варианта вот такое смотри");
            // Добавляем sizeVariant в attributeList
            attributeList.add(attribute);
            return attributeList;
        }


        public static List<String> sliderPhoto (WebDriver driver) throws InterruptedException {
            List<String> imageUrls = new ArrayList<>();
            List<WebElement> allImages = driver.findElements(photoXPath);

            for (WebElement img : allImages) {
                String srcValue = img.getAttribute("src");
                if (!imageUrls.contains(srcValue)) {
                    imageUrls.add(srcValue);
                }
            }
            return imageUrls;
        }

        public static double extractNumber (String input){
            // Удаление ненужного текста, например, "Sepette %30 İndirim"
            input = input.replaceAll("Sepette %\\d+ İndirim", "").trim();
            input = input.replaceAll("₺", "").trim();
            input = input.replaceAll("TL","").trim();
//            input = input.replaceAll("." ,"").trim();
            System.out.println(input + "VOT takaya cena prisutstvuet");

            // Регулярное выражение для поиска чисел в формате цены
            String regex = "([-]?\\d+[,.]\\d+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);

            // Ищем первое вхождение, которое соответствует шаблону цены
            if (matcher.find()) {
                String price = matcher.group(1);

                // Удаляем символ валюты, если он есть
                price = price.replaceAll("₺", "").trim();

                // Заменяем точки на пустые строки (удаляем их) и запятые на точки
                price = price.replace(".", "").replace(',', '.');

                if (matcher.find()) {
                    String price1 = matcher.group(1);
                    System.out.println("Найденная цена: " + price1); // Добавьте эту строку для отладки
                    // далее ваш код
                }

                try {
                    return Double.parseDouble(price) * 1.20 + 50; // Преобразуем в double
                } catch (NumberFormatException e) {
                    // В случае ошибки преобразования возвращаем 0
                    return 0;
                }
            }
            return 0; // Возвращаем 0, если соответствующее значение не найдено
        }


        public static String generateStockCode (String url){
            // Удаление базовой части URL
            url = url.replace("https://www.munora.com.tr", "").replace("http://www.munora.com.tr", "");
            String path = url.substring(url.indexOf('/'));

            // Получение последних трех слов
            String[] words = path.split("-");
            String lastThreeWords = String.join("-", Arrays.copyOfRange(words, Math.max(words.length - 3, 0), words.length));

            // Получение хэш-кода
            int hashCode = path.hashCode();

            // Формирование stockCode
            return lastThreeWords + "-" + hashCode;
        }

        public static void clickElementUsingJS (WebDriver driver, WebElement element){
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", element);
        }



    }

