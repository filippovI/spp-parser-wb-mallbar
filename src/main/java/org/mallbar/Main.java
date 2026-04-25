package org.mallbar;

import com.codeborne.selenide.Configuration;
import lombok.SneakyThrows;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;

public class Main {


    @SneakyThrows
    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        Configuration.browserSize = "1920х1080";
        System.setProperty("chromeoptions.args", "--force-device-scale-factor=0.33");
        Parser parser = new Parser();
        parser.authViaSessionFiles();

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

