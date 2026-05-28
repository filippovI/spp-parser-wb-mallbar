package org.mallbar.services.parsers;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.mallbar.pages.PriceAndDiscountPage;
import org.mallbar.services.TelegramBotService;

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
import static org.mallbar.pages.PriceAndDiscountPage.DISCOUNT_PRICE_COLUMN;


public class PriceLkParser {
    private static final Pattern ARTICLE_PATTERN = Pattern.compile("\\d{8,}");
    private final Map<String, String> articleAndDiscountMap = new HashMap<>();
    TelegramBotService bot;
    long chatId;

    public PriceLkParser(TelegramBotService bot, long chatId) {
        this.chatId = chatId;
        this.bot = bot;
    }

    public Map<String, String> parsePriceFromLk() {
        PriceAndDiscountPage priceAndDiscountPage = new PriceAndDiscountPage();
        open(PriceAndDiscountPage.PAGE_URL);
        priceAndDiscountPage.getAcceptCookieButton().shouldBe(visible, Duration.ofSeconds(10));
        priceAndDiscountPage.clickAcceptCookieButton();
        priceAndDiscountPage.getMainTable().shouldBe(visible, Duration.ofSeconds(10));
        //log
        System.out.println("Начат парсинг данных");
        int articleAndDiscountMapSize = -1;
        executeJavaScript("window.scrollBy(0, 1500)");
        while (true) {
            try {
                for (SelenideElement el : priceAndDiscountPage.getMainTable().$$x("./*")) {
                    SelenideElement nameCell = el.$x(ARTICLE_COLUMN);
                    SelenideElement priceCell = el.$x(DISCOUNT_PRICE_COLUMN);
                    if (!el.getText().isEmpty() && Arrays.asList(el.getText().split("\n")).size() > 4) {
                        Matcher matcher = ARTICLE_PATTERN.matcher(nameCell.getText());
                        String price = priceCell.getText().trim().replaceAll("[\\s₽]", "");
                        if (matcher.find()) {
                            String art = matcher.group().trim();
                            articleAndDiscountMap.put(art, price);
                            System.out.println("Нашел цену со скидкой для " + art + " - " + price);
                        }
                    }
                }

                if (articleAndDiscountMapSize == articleAndDiscountMap.size()) break;
                articleAndDiscountMapSize = articleAndDiscountMap.size();
                priceAndDiscountPage.spinPriceAndDiscountTable(1000);
                Selenide.sleep(1000);
            } catch (Exception ex) {
                //log
                System.out.println("Произошла ошибка при парсинге: \n" + ex);
                bot.sendTextMessage(chatId, "Произошла ошибка при сборе данных");
                return Map.of();
            }
        }
        return articleAndDiscountMap;
    }
}
