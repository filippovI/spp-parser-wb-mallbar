package org.mallbar.services.parsers;

import lombok.Getter;
import lombok.ToString;
import org.mallbar.services.AuthService;
import org.mallbar.services.GoogleSheetsService;
import org.mallbar.services.TelegramBotService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
        Map<String, String> articleAndSPP = new HashMap<>();

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
            PriceLkParser priceLkParser = new PriceLkParser(bot, chatId);
            ProductCardParser productCardParser = new ProductCardParser();
            bot.sendTextMessage(chatId, "Начинаю собирать цены со скидкой из ЛК");
            Map<String, String> articleAndDiscountMap = priceLkParser.parsePriceFromLk();
            bot.sendTextMessage(chatId, "Начинаю собирать цены с карточек товаров");
            Map<String, String> productCardPrices = productCardParser.parsePriceFromCard(articleAndDiscountMap);
            for (Map.Entry<String, String> entry : productCardPrices.entrySet()) {
                String key = entry.getKey();
                double priceFromProductCard = Double.parseDouble(entry.getValue());
                double priceFromLk = Double.parseDouble(articleAndDiscountMap.get(key));
                double rawSppPercent = (1.0 - (priceFromProductCard / priceFromLk)) * 100;
                String spp = (Math.round(rawSppPercent * 10.0) / 10.0) + "%";
                articleAndSPP.put(key, spp.replace('.', ','));
            }
        }
        System.out.println(articleAndSPP);
        return articleAndSPP;
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
