package org.mallbar;

import com.codeborne.selenide.Selenide;
import lombok.Getter;
import lombok.ToString;
import org.mallbar.pages.MainPage;
import org.mallbar.pages.MobileAuthPage;
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

    public boolean setCookieAndStorageWithRetry(int retryCount) {
        for (int i = 0; i <= retryCount; i++) {
            //log
            System.out.println("Попытка № " + (i + 1) + " авторизации через сессионные файлы");
            open("https://seller.wildberries.ru/");
            clearBrowserCookies();
            clearBrowserLocalStorage();
            Selenide.sleep(2000);
            sessionManager
                    .setLocalStorage()
                    .setCookies();
            Selenide.sleep(2000);
            refresh();
            open("https://seller-auth.wildberries.ru/ru/");
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
