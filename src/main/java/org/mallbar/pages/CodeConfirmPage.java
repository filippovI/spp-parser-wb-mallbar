package org.mallbar.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.ToString;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.actions;

@Getter
@ToString
public class CodeConfirmPage {
    public static final String ERROR_SMS_CODE_LABEL = "//span[contains(text(), 'Неверный код')]";
    public final SelenideElement writeSmsCodeLabel = $(By.xpath("//span[contains(text(), 'Введите код')]"));
    public final SelenideElement errorSmsCodeLabel = $(By.xpath(ERROR_SMS_CODE_LABEL));
    public final SelenideElement firstUnputCode = $(By.xpath("//div[@class='FormCodeInput']//li[1]"));

    public CodeConfirmPage writeSmsCode(String code) {
        firstUnputCode.click();
        for (char digit : code.toCharArray()) {
            actions().sendKeys(String.valueOf(digit)).perform();
            Selenide.sleep(500); // пауза 500 мс (0.5 сек) между цифрами
        }
        return this;
    }
}
