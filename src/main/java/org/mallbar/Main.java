package org.mallbar;

import com.codeborne.selenide.Config;
import com.codeborne.selenide.Configuration;
import lombok.SneakyThrows;
import org.mallbar.services.TelegramBotService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;

import static com.codeborne.selenide.Selenide.open;

public class Main {


    @SneakyThrows
    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions()
                .addArguments(
                        "--headless=new",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--remote-debugging-port=9222",
                        "--window-size=2560,1400",
                        "--force-device-scale-factor=0.33",
                        "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                        "--lang=ru"
                );

        Configuration.headless = true; // дубль для Selenide, но не мешает
        options.setExperimentalOption("excludeSwitches", java.util.List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("prefs", java.util.Map.of("intl.accept_languages", "ru-RU,ru"));
        Configuration.browserCapabilities = options;
        TelegramBotService bot = TelegramBotService.init();


        //TelegramBot bot = TelegramBot.init();
//        Parser parser = new Parser();
//        SessionManager sessionManager = new SessionManager();
//        sessionManager.setCookieAndStorage();
//        parser.parseArticleAndDiscount();
        //GoogleSheetsService googleSheetsService = new GoogleSheetsService("WB Unit БАЗА", "A", "H");
//        sleep(1000);
//        Map<String, String> result = parser.parseArticleAndDiscount();
//        System.out.println(result);
//        Map<String, String> result = new HashMap<>() {{
//            put("195029270", "99%"); //34
//
//        }};
//        googleSheetsService.updatePercent(result);
    }
}

