
package org.example;

import com.codeborne.selenide.SelenideElement;
import org.example.cookiesAndStorage.CookieAndStorage;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class PriceAndDiscountPage {

    private static final SelenideElement mainTable = $(By.xpath("//*[contains(@class, 'ant-table-tbody-virtual-holder-inner')]"));
    private static final SelenideElement spinContainer = $(By.xpath("//*[contains(@class, 'ant-table-tbody-virtual-holder')]"));
    private static final Pattern ARTICLE_PATTERN = Pattern.compile("\\d{8,}");
    private static final Map<String, String> articleAndDiscountMap = new HashMap<>();
    private static final String TARGET_URL = "https://seller.wildberries.ru/discount-and-prices/main-table";
    private static final String ARTICLE_COLUMN = "./div[1]";
    private static final String PERCENT_COLUMN = "./div[7]";

    public PriceAndDiscountPage() {
        System.setProperty("chromeoptions.args", "--force-device-scale-factor=0.33");
        CookieAndStorage cookieAndStorage = new CookieAndStorage();
        cookieAndStorage.setCookieAndStorage();
    }

    public Map<String, String> parseArticleAndSpp() {
        open(TARGET_URL);
        getWebDriver().manage().window().maximize();
        mainTable.shouldBe(visible, Duration.ofSeconds(10));
        int articleAndDiscountMapSize = 0;
        executeJavaScript("window.scrollBy(0, 1500)");
        while (true) {
            try {
                for (SelenideElement el : mainTable.$$x("./*")) {
                    SelenideElement nameCell = el.$x(ARTICLE_COLUMN);
                    SelenideElement percentCell = el.$x(PERCENT_COLUMN);
                    if (!el.getText().isEmpty() && Arrays.asList(el.getText().split("\n")).size() > 12) {
                        Matcher matcher = ARTICLE_PATTERN.matcher(nameCell.getText());
                        String percent = percentCell.getText().trim().contains("%") ? percentCell.getText().trim() : "0%";
                        if (matcher.find()) {
                            articleAndDiscountMap.put(matcher.group().trim(), percent);
                        }
                    }
                }
                if (articleAndDiscountMapSize == articleAndDiscountMap.size()) break;
                articleAndDiscountMapSize = articleAndDiscountMap.size();
                executeJavaScript("arguments[0].scrollTop += 1500;", spinContainer);
                sleep(1000);
            } catch (MoveTargetOutOfBoundsException ex) {
                System.out.println("упс");
            }
        }
        return articleAndDiscountMap;
    }
}
