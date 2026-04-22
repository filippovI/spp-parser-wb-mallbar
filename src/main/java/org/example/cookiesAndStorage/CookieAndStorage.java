package org.example.cookiesAndStorage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.codeborne.selenide.Selenide.localStorage;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class CookieAndStorage {
    private static final String COOKIES_FILE = "wb_seller_cookies.json";
    private static final String STORAGE_FILE = "wb_local_storage.json";
    private static final Gson gson = new GsonBuilder() // Для корректной работы с датами куки
            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.getTime());
                }
            })
            .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    if (json.isJsonNull()) return null;
                    return new Date(json.getAsJsonPrimitive().getAsLong());
                }
            })
            .create();

    private void setCookies() {
        File cookiesFile = new File(COOKIES_FILE);
        System.out.println("Загружаем куки из файла: " + COOKIES_FILE);
        try (FileReader reader = new FileReader(cookiesFile)) {
            // Определяем тип для чтения JSON массива объектов SimpleCookie
            Type type = new TypeToken<Set<SimpleCookie>>() {
            }.getType();
            Set<SimpleCookie> simpleCookies = gson.fromJson(reader, type);

            // !!! Получаем WebDriver из Selenide !!!
            // Нужно открыть какую-то страницу, чтобы WebDriver был инициализирован.
            // Открываем пустую страницу на домене WB, чтобы Selenide сам настроил браузер.
            open("https://seller.wildberries.ru/"); // Любая страница этого домена

            WebDriver driver = getWebDriver(); // Получаем текущий WebDriver

            // Добавляем куки
            for (SimpleCookie sc : simpleCookies) {
                Date expiryDate = (sc.expiry != null) ? new Date(sc.expiry) : null;
                // Создаем объект Cookie для Selenium
                Cookie cookie = new Cookie(sc.name, sc.value, sc.domain, sc.path, expiryDate, sc.isSecure, sc.isHttpOnly);
                driver.manage().addCookie(cookie);
            }
            System.out.println("Куки успешно добавлены в сессию браузера.");

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла куки: " + e.getMessage());
        } catch (Exception e) {
            // Ловим ошибки, если Selenide не смог инициализировать WebDriver
            System.err.println("Ошибка при инициализации WebDriver для добавления куки: " + e.getMessage());
        }
    }

    private void setStorage() {
        try {
            // 3. Читаем JSON файл в Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> storageData = mapper.readValue(
                    new File(STORAGE_FILE),
                    new TypeReference<Map<String, String>>() {
                    }
            );
            // 4. Записываем данные в LocalStorage через Selenide
            storageData.forEach((key, value) -> {
                // Используем встроенный хелпер Selenide
                localStorage().setItem(key, value);
            });
            System.out.println("LocalStorage успешно загружен!");

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    public void setCookieAndStorage() {
        this.setCookies();
        this.setStorage();
    }
}

