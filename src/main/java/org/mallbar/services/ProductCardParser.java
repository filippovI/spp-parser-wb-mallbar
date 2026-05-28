package org.mallbar.services;

import org.mallbar.pages.ProductCardPage;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;

public class ProductCardParser {
    ProductCardPage productCardPage;

    public ProductCardParser() {
        productCardPage = new ProductCardPage();
    }


    public Map<String, String> parsePrice (Map<String, String> articleAndDiscountMap) {
        Map<String, String> articleAndPriceFromProductCard = new HashMap<>();
        for (String key : articleAndDiscountMap.keySet()) {
            open(productCardPage.getPAGE_URL() + key + productCardPage.getPAGE_URL_END());
            productCardPage.getPrice().shouldBe(visible, Duration.ofSeconds(10));
            if (!productCardPage.getPrice().getText().equals("Нет в наличии")) {
                articleAndPriceFromProductCard.put(key, productCardPage.getPrice().getText());
            }
            System.out.println(articleAndPriceFromProductCard);
        }
        return articleAndPriceFromProductCard;
    }
}
