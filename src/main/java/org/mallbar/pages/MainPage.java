package org.mallbar.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.ToString;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

@Getter
@ToString
public class MainPage {
    public static final String MAIN_LOGO = "//img[@alt='logo']";
    private final SelenideElement logo = $(By.xpath(MAIN_LOGO));
}
