package org.mallbar.services;

import com.codeborne.selenide.Selenide;
import lombok.Getter;
import lombok.ToString;
import org.mallbar.services.parsers.ParserService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@ToString
public class TelegramBotService extends TelegramLongPollingBot {
    private final Map<Long, CompletableFuture<String>> waitingResponses = new ConcurrentHashMap<>();
    private final String botUsername = "Mallbar WB";
    private final String botToken = System.getenv("TELEGRAM_TOKEN");
    private final String adminChatId = "467744617";
    private final AtomicBoolean isParserRunning = new AtomicBoolean(false);

    public static TelegramBotService init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBotService bot = new TelegramBotService();
            botsApi.registerBot(bot);
            System.out.println("Телеграм бот запущен");
            return bot;
        } catch (TelegramApiException ex) {
            throw new RuntimeException("Ошибка при запуске телеграм бота: " + ex.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (waitingResponses.containsKey(chatId)) {
                CompletableFuture<String> future = waitingResponses.get(chatId);
                future.complete(messageText); // "Размораживаем" парсер и передаем ему текст
                waitingResponses.remove(chatId); // Удаляем из ожидания
                return; // Выходим, чтобы не обрабатывать это как команду
            }
            if (messageText.equals("/start")) {
                sendTextMessage(chatId, "Привет!\nДля начала работы выбери один из пунктов в меню");
            }

            if (messageText.equals("/updatespp")) {
                if (isParserRunning.compareAndSet(false, true)) {
                    new Thread(() -> {
                        try {
                            ParserService parser = new ParserService(this);
                            parser.parseDataAndUpdateColumn(chatId);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            isParserRunning.set(false);
                            Selenide.closeWebDriver();
                        }
                    }).start();
                } else {
                    sendTextMessage(chatId, "Обновление уже запущено");
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


    public String waitForUserResponse(long chatId, String text) {
        sendTextMessage(chatId, text);
        CompletableFuture<String> future = new CompletableFuture<>();
        waitingResponses.put(chatId, future);
        try {
            return future.get(2, TimeUnit.MINUTES);
        } catch (Exception e) {
            waitingResponses.remove(chatId);
            return null;
        }
    }
}
