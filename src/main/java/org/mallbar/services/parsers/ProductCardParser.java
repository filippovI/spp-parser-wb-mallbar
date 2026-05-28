package org.mallbar.services.parsers;

import com.codeborne.selenide.Selenide;
import lombok.extern.slf4j.Slf4j;
import org.mallbar.pages.ProductCardPage;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class ProductCardParser {
    ProductCardPage productCardPage;

    public ProductCardParser() {
        productCardPage = new ProductCardPage();
    }


    public Map<String, String> parsePriceFromCard(Map<String, String> articleAndDiscountMap) {
        Map<String, String> articleAndPriceFromProductCard = new HashMap<>();
        for (String key : articleAndDiscountMap.keySet()) {
            System.out.print("Открываю карточку для " + key);
            open(productCardPage.getPAGE_URL() + key + productCardPage.getPAGE_URL_END());
            if (productCardPage.getPriceWithWbWallet().is(visible, Duration.ofSeconds(10))) {
                if (!productCardPage.getPriceWithWbWallet().getText().equals("Нет в наличии")) {
                    String price = productCardPage.getPriceWithoutWbWallet().getText().replaceAll("[\\s₽]", "");
                    articleAndPriceFromProductCard.put(key, price);
                    System.out.println(" - " + price);
                }
            } else if (productCardPage.getErrorPage().is(visible, Duration.ofSeconds(10))) {
                System.out.println("Такого товара еще нет");
            } else {
                System.out.println("Ошибка парсинга цены");
                Selenide.screenshot("error_" + key);
            }
        }
        return articleAndPriceFromProductCard;
    }
}
