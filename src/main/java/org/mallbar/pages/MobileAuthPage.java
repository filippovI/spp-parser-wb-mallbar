package org.mallbar.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.ToString;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

@Getter
@ToString
public class MobileAuthPage {
    public static final String PAGE_URL = "https://seller-auth.wildberries.ru/";
    public static final String WRITE_NUMBER_PHONE_LABEL = "//span[contains(text(), 'Введите номер телефона')]";
    public final SelenideElement writeNumberPhoneLabel = $(By.xpath(WRITE_NUMBER_PHONE_LABEL));
    public final SelenideElement numberPhoneInput = $(By.xpath("//input[@placeholder='999 999-99-99']"));
    public final SelenideElement submitButton = $(By.xpath("//button[@type='submit']"));

    public MobileAuthPage() {
        //open(PAGE_URL);
    }

    public MobileAuthPage writeNumber(String number) {
        numberPhoneInput.setValue(String.valueOf(number));
        return this;
    }

    public MobileAuthPage clickSubmit() {
        submitButton.click();
        return this;
    }
}
