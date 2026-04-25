package org.mallbar.services;

import com.codeborne.selenide.Selenide;
import lombok.Getter;
import lombok.ToString;
import org.mallbar.pages.CodeConfirmPage;
import org.mallbar.pages.MainPage;
import org.mallbar.pages.MobileAuthPage;
import org.mallbar.pages.PriceAndDiscountPage;
import org.mallbar.session.SessionManager;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

@Getter
@ToString
public class AuthService {
    SessionManager sessionManager = new SessionManager();
    MainPage mainPage = new MainPage();
    MobileAuthPage mobileAuthPage = new MobileAuthPage();
    CodeConfirmPage codeConfirmPage = new CodeConfirmPage();
    TelegramBotService bot;


    public boolean manualAuth(int writeCodeRetryCount, TelegramBotService bot, long chatId) {
        mobileAuthPage.numberPhoneInput.shouldBe(visible, Duration.ofSeconds(10));
        String phoneNumber = bot.waitForUserResponse(chatId, "Введите номер телефона в формате 9129999999");
        mobileAuthPage
                .writeNumber(phoneNumber)
                .clickSubmit();
        codeConfirmPage.getWriteSmsCodeLabel().shouldBe(visible, Duration.ofSeconds(20));
        //Сообщение в телеграм введите код
        for (int i = 1; i <= writeCodeRetryCount; i++) {
            String code = bot.waitForUserResponse(chatId, "Введите код:");
            codeConfirmPage.writeSmsCode(code);
            $x(MainPage.MAIN_LOGO + " | " + CodeConfirmPage.ERROR_SMS_CODE_LABEL).shouldBe(visible, Duration.ofSeconds(10));
            if (mainPage.getLogo().isDisplayed()) {
                System.out.println("Успешная авторизация");
                sessionManager.saveCookieAndStorage();
                return true;
            } else if (codeConfirmPage.errorSmsCodeLabel.isDisplayed()) {
                bot.sendTextMessage(chatId, "Неверный код");
                System.out.println("Неверный код. Повторите снова: ");
            }
        }
        return false;
    }

    public boolean setCookieAndStorageWithRetry(int retryCount) {
        for (int i = 1; i <= retryCount; i++) {
            //log
            System.out.println("Попытка № " + i + " авторизации через сессионные файлы");
            open(SessionManager.SESSION_URL);
            clearBrowserCookies();
            clearBrowserLocalStorage();
            Selenide.sleep(2000);
            sessionManager
                    .setLocalStorage()
                    .setCookies();
            Selenide.sleep(2000);
            refresh();
            open(PriceAndDiscountPage.PAGE_URL);
            $x(MobileAuthPage.WRITE_NUMBER_PHONE_LABEL + " | " + MainPage.MAIN_LOGO)
                    .shouldBe(visible, Duration.ofSeconds(10));
            if (mainPage.getLogo().isDisplayed()) {
                //log
                System.out.println("Успешная авторизация через файлы сессии");
                return true;
            } else {
                //log
                System.out.println("Неудачная попытка");
                Selenide.sleep(2000);
            }
        }
        System.out.println("Не удалось авторизоваться через сессионные файлы");
        return false;
    }
}
