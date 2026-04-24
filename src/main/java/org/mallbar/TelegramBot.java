package org.mallbar;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TelegramBot extends TelegramLongPollingBot {
    private final String botUsername = "Mallbar WB";
    private final String botToken = "8513691300:AAEGP1RhZBK-p0To4ctdVtyYgZ07qWAJtdE";
    private final String adminChatId = "";

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
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                sendTextMessage(chatId, "Привет!\n" + update.getMessage().getChatId());
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
