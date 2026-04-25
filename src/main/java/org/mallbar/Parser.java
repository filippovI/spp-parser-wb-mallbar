package org.mallbar;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.ToString;
import org.mallbar.pages.PriceAndDiscountPage;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static org.mallbar.pages.PriceAndDiscountPage.ARTICLE_COLUMN;
import static org.mallbar.pages.PriceAndDiscountPage.PERCENT_COLUMN;

@Getter
@ToString
public class Parser {
    private static final Pattern ARTICLE_PATTERN = Pattern.compile("\\d{8,}");
    private final Map<String, String> articleAndDiscountMap = new HashMap<>();

    public boolean authViaSessionFiles() {
        AuthService authService = new AuthService();
        return authService.setCookieAndStorageWithRetry(3);
    }


    public Map<String, String> parseArticleAndDiscount() {
        PriceAndDiscountPage priceAndDiscountPage = new PriceAndDiscountPage();
        open(PriceAndDiscountPage.PAGE_URL);
        priceAndDiscountPage.getMainTable().shouldBe(visible, Duration.ofSeconds(10));
        int articleAndDiscountMapSize = 0;
        executeJavaScript("window.scrollBy(0, 1500)");
        while (true) {
            try {
                for (SelenideElement el : priceAndDiscountPage.getMainTable().$$x("./*")) {
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
                priceAndDiscountPage.spinPriceAndDiscountTable(1500);
                Selenide.sleep(1000);
            } catch (Exception ex) {
                System.out.println("Произошла ошибка при парсинге: \n" + ex);
                return Map.of();
            }
        }
        return articleAndDiscountMap;
    }
}
