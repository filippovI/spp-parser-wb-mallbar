package org.mallbar;

import lombok.SneakyThrows;
import org.mallbar.session.SessionManager;

import java.util.Map;

import static com.codeborne.selenide.Selenide.sleep;

public class Main {


    @SneakyThrows
    public static void main(String[] args) {
        TelegramBot bot = TelegramBot.init();
//        Parser parser = new Parser();
//        SessionManager sessionManager = new SessionManager();
//        sessionManager.setCookieAndStorage();
//        parser.parseArticleAndDiscount();
//        GoogleSheetsService googleSheetsService = new GoogleSheetsService("WB Unit БАЗА");
//        sleep(1000);
//        Map<String, String> result = parser.parseArticleAndDiscount();
//        System.out.println(result);
//        googleSheetsService.updatePercent(result, "A", "BH");
    }
}

