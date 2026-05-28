package org.mallbar.session;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import org.openqa.selenium.Cookie;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class SessionManager {
    private static final Path SESSION_DIR = Paths.get(
            System.getenv().getOrDefault("SESSION_DIR", "/srv/session")
    );
    private static final Path COOKIES_FILE = SESSION_DIR.resolve("wb_seller_cookies.json");
    private static final Path STORAGE_FILE = SESSION_DIR.resolve("wb_local_storage.json");
    public static final String SESSION_URL = "https://seller.wildberries.ru/about-portal/ru/ru";
    private final ObjectMapper mapper = new ObjectMapper();

    public void saveCookieAndStorage() {
        saveCookies();
        saveLocalStorage();
    }

    public SessionManager setCookies() {
        File file = COOKIES_FILE.toFile();
        if (!file.exists()) return this;
        try {
            List<Map<String, Object>> cookiesData = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {
            });
            for (Map<String, Object> c : cookiesData) {
                Cookie cookie = new Cookie.Builder((String) c.get("name"), (String) c.get("value"))
                        .domain((String) c.get("domain"))
                        .path((String) c.get("path"))
                        .expiresOn(c.get("expiry") != null ? new Date(((Number) c.get("expiry")).longValue()) : null)
                        .isSecure(c.get("isSecure") != null && (boolean) c.get("isSecure"))
                        .isHttpOnly(c.get("isHttpOnly") != null && (boolean) c.get("isHttpOnly"))
                        .build();
                WebDriverRunner.getWebDriver().manage().addCookie(cookie);
            }
            System.out.println("Cookies загружены успешно");
        } catch (IOException e) {
            System.err.println("Ошибка Cookies: " + e.getMessage());
        }
        return this;
    }

    public SessionManager setLocalStorage() {
        File file = STORAGE_FILE.toFile();
        if (!file.exists()) return this;
        try {
            Map<String, String> storageData = mapper.readValue(file, new TypeReference<Map<String, String>>() {
            });
            storageData.forEach(Selenide.localStorage()::setItem);
            System.out.println("LocalStorage загружен успешно");
        } catch (IOException e) {
            System.err.println("Ошибка Storage: " + e.getMessage());
        }
        return this;
    }

    private void saveCookies() {
        try {
            // Получаем куки напрямую из драйвера
            Set<Cookie> cookies = WebDriverRunner.getWebDriver().manage().getCookies();
            mapper.writeValue(COOKIES_FILE.toFile(), cookies);
            System.out.println("Куки сохранены: " + COOKIES_FILE.toUri());
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении куки: " + e.getMessage());
        }
    }

    private void saveLocalStorage() {
        try {
            // Получаем все элементы LocalStorage через Selenide
            Map<String, String> storageData = Selenide.localStorage().getItems();
            mapper.writeValue(STORAGE_FILE.toFile(), storageData);
            System.out.println("LocalStorage сохранен: " + STORAGE_FILE.toUri());
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении LocalStorage: " + e.getMessage());
        }
    }
}