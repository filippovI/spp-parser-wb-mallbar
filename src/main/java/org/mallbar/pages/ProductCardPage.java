package org.mallbar.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class ProductCardPage {
    private final String PAGE_URL = "https://www.wildberries.ru/catalog/";
    private final String PAGE_URL_END = "/detail.aspx";
    private final SelenideElement acceptAgeButton = $(By.xpath("//span[contains(text(), 'Да, мне есть 18 лет')]/ancestor::button"));
    private final SelenideElement price = $(By.xpath("//div[contains(@class, 'priceBlock')]//h2"));

    public void clickAcceptAgeButton() {
        acceptAgeButton.click();
    }
}

