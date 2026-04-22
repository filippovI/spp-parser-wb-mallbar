package org.example;

import lombok.SneakyThrows;
import org.example.cookiesAndStorage.GoogleSheetsService;

import java.util.HashMap;
import java.util.Map;

public class Main {


    @SneakyThrows
    static void main() {
        PriceAndDiscountPage priceAndDiscountPage = new PriceAndDiscountPage();
        GoogleSheetsService gss = new GoogleSheetsService("WB unit БАЗА", "A");
        //Map<String, String> articlesAndSpp = priceAndDiscountPage.parseArticleAndSpp();
        Map<String, String> articlesAndSpp1 = new HashMap<String, String>() {{
            put("195029270", "66"); //32
            put("192576767", "66"); //36
        }};
        gss.updatePercent(articlesAndSpp1, "A", "BH");
    }
}
