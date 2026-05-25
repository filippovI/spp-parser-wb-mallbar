package org.mallbar.services;

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
public class ParserService {
    private static final Pattern ARTICLE_PATTERN = Pattern.compile("\\d{8,}");
    private final Map<String, String> articleAndDiscountMap = new HashMap<>();
    private final TelegramBotService bot;
    private final AuthService authService;

    public ParserService(TelegramBotService bot) {
        this.bot = bot;
        this.authService = new AuthService();
    }

    private boolean updateColumn(long chatId, Map<String, String> updateData) {
        GoogleSheetsService googleSheetsService = new GoogleSheetsService("WB unit БАЗА", "A", "BH");
        //log
        System.out.println("Начинаю обновлять таблицу");
        bot.sendTextMessage(chatId, "Обновляю таблицу");
        return googleSheetsService.updateColumn(updateData);
    }

    private Map<String, String> parseData(long chatId) {
        //log
        System.out.println("Вход в лк через сессию");
        bot.sendTextMessage(chatId, "Вхожу в ЛК");
        boolean auth = authService.setCookieAndStorageWithRetry(2);
        if (!auth) {
            //log
            System.out.println("Вход в лк через страницу логина");
            bot.sendTextMessage(chatId, "Необходима авторизация\n");
            auth = authService.manualAuth(3, bot, chatId);
        }
        if (!auth) {
            //log
            System.out.println("Неудачная авторизация");
            bot.sendTextMessage(chatId, "Неудачная авторизация");
        } else {
            PriceAndDiscountPage priceAndDiscountPage = new PriceAndDiscountPage();
            open(PriceAndDiscountPage.PAGE_URL);
            priceAndDiscountPage.getAcceptCookieButton().shouldBe(visible, Duration.ofSeconds(10));
            priceAndDiscountPage.clickAcceptCookieButton();
            priceAndDiscountPage.getMainTable().shouldBe(visible, Duration.ofSeconds(10));
            //log
            System.out.println("Начат парсинг данных");
            bot.sendTextMessage(chatId, "Собираю данные");
            int articleAndDiscountMapSize = -1;
            executeJavaScript("window.scrollBy(0, 1500)");
            while (true) {
                try {
                    for (SelenideElement el : priceAndDiscountPage.getMainTable().$$x("./*")) {
                        SelenideElement nameCell = el.$x(ARTICLE_COLUMN);
                        SelenideElement percentCell = el.$x(PERCENT_COLUMN);
                        if (!el.getText().isEmpty() && Arrays.asList(el.getText().split("\n")).size() > 4) {
                            Matcher matcher = ARTICLE_PATTERN.matcher(nameCell.getText());
                            String percent = percentCell.getText().trim().contains("%") ? percentCell.getText().trim() : "0%";
                            if (matcher.find()) {
                                articleAndDiscountMap.put(matcher.group().trim(), percent);
                            }
                        }
                    }
                    System.out.println(articleAndDiscountMap);
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
        }
        return articleAndDiscountMap;
    }

    public void parseDataAndUpdateColumn(long chatId) {
        Map<String, String> parseData = parseData(chatId);
        if (!parseData.isEmpty()) {
            boolean updateState;
            updateState = updateColumn(chatId, parseData);
            if (updateState) {
                //log
                System.out.println("Таблица обновлена");
                bot.sendTextMessage(chatId, "Таблица обновлена");
            } else {
                //log
                bot.sendTextMessage(chatId, "Ошибка при обновлении таблицы");
            }
        } else {
            //log
            System.out.println("Нет данных на сайте WB");
            bot.sendTextMessage(chatId, "Нет данных на сайте WB");
        }
    }
}
