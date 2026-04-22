package org.mallbar.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import lombok.ToString;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

@Getter
@ToString
public class PriceAndDiscountPage {
    public static final String PAGE_URL = "https://seller.wildberries.ru/discount-and-prices/main-table";
    public static final String ARTICLE_COLUMN = "./div[1]";
    public static final String PERCENT_COLUMN = "./div[7]";
    private final SelenideElement mainTable = $(By.xpath("//*[contains(@class, 'ant-table-tbody-virtual-holder-inner')]"));
    private final SelenideElement spinContainer = $(By.xpath("//*[contains(@class, 'ant-table-tbody-virtual-holder')]"));

    public void spinPriceAndDiscountTable(int pixels) {
        executeJavaScript("arguments[0].scrollTop += " + pixels + ";", spinContainer);
    }
}