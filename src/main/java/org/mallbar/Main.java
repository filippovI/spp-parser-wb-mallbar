package org.mallbar;

import lombok.SneakyThrows;

import java.util.Map;

import static com.codeborne.selenide.Selenide.sleep;

public class Main {


    @SneakyThrows
    public static void main(String[] args) {
        Parser parser = new Parser();
        GoogleSheetsService googleSheetsService = new GoogleSheetsService("WB Unit БАЗА");
        sleep(1000);
        Map<String, String> result = parser.parseArticleAndDiscount();
        System.out.println(result);
        googleSheetsService.updatePercent(result, "A", "BH");
    }
}

