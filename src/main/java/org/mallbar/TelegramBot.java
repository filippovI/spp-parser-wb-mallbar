package org.mallbar;

import lombok.Getter;
import lombok.ToString;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;

@Getter
@ToString
public class TelegramBot extends TelegramLongPollingBot {
    private final String botUsername = "Mallbar WB";
    private final String botToken = "8513691300:AAEGP1RhZBK-p0To4ctdVtyYgZ07qWAJtdE";
    private final String adminChatId = "467744617";

    public static TelegramBot init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBot bot = new TelegramBot();
            botsApi.registerBot(bot);
            System.out.println("Телеграм бот запущен");
            return bot;
        } catch (TelegramApiException ex) {
            throw new RuntimeException("Ошибка при запуске телеграм бота\n" + ex);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                sendTextMessage(chatId, "Привет!\nДля начала работы выбери один из пунктов в меню");
            }
            if (messageText.equals("/updatespp")) {
                sendTextMessage(chatId, "Запускаю обновление СПП");
                Parser parser = new Parser();
                GoogleSheetsService googleService = new GoogleSheetsService("WB Unit БАЗА", "A", "BH");
                sendTextMessage(chatId, "Собираю данные");
                Map<String, String> parseData = parser.parseArticleAndDiscount();
                if (parseData.isEmpty()) {
                    sendTextMessage(chatId, "Не удалось собрать данные");
                } else {
                    sendTextMessage(chatId, "Обновляю таблицу");
                    boolean updateStatus = googleService.updateColumn(parseData);
                    if (updateStatus) {
                        sendTextMessage(chatId, "Данные успешно обновлены!");
                    } else {
                        sendTextMessage(chatId, "Не удалось обновить данные");
                    }
                }
            }

        }
    }

    public void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            System.out.println("Ошибка при отправке сообщения в телеграм бот\n" + ex);
        }
    }
}
