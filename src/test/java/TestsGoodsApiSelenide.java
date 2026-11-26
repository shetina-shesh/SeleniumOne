import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.GoodsApi;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TestsGoodsApiSelenide {

    private static final String BASE_URL = "http://localhost:8080";

    private final List<Integer> createdGoodIds = new ArrayList<>();

    private static final String LOGIN = System.getenv("USERNAME");

    private static final String PASSWORD = System.getenv("PASSWORD");

    @BeforeEach
    void setup() {
        Configuration.browser = "chrome";
        Configuration.timeout = 10000;

        open(BASE_URL + "/admin");

        if ($x("//*[@id='username']").exists()) {
            login(LOGIN, PASSWORD);
        }

        $x("//*[@id='tbody']").shouldBe(visible);

        open(BASE_URL);

        $x("//*[@id='products-list']").shouldBe(visible);

    }

    private void login(String username, String password) {
        $x("//*[@id='username']")
                .shouldBe(visible)
                .setValue(username);

        $x("//*[@id='password']")
                .shouldBe(visible)
                .setValue(password);

        $x("//button[@type='submit']")
                .shouldBe(visible, enabled)
                .click();
    }

    @Test
    void createOrderWithThreeGoods() {
        String name1 = GoodsApi.uniqueName();
        String name2 = GoodsApi.uniqueName();
        String name3 = GoodsApi.uniqueName();

        float price1 = GoodsApi.uniquePrice();
        float price2 = GoodsApi.uniquePrice();
        float price3 = GoodsApi.uniquePrice();

        Integer id1 = GoodsApi.createGood(name1, price1);
        Integer id2 = GoodsApi.createGood(name2, price2);
        Integer id3 = GoodsApi.createGood(name3, price3);

        createdGoodIds.addAll(List.of(id1, id2, id3));

        refresh();

        $x("//*[@id='card-" + id1 + "']").shouldBe(visible);
        $x("//*[@id='card-" + id2 + "']").shouldBe(visible);
        $x("//*[@id='card-" + id3 + "']").shouldBe(visible);

        $x("//*[@id='card-" + id1 + "']//button[@data-action='add-to-cart']").click();
        $x("//*[@id='card-" + id2 + "']//button[@data-action='add-to-cart']").click();
        $x("//*[@id='card-" + id3 + "']//button[@data-action='add-to-cart']").click();

        $x("//*[@id='open-cart-btn']").click();

        $x("//*[@id='cartModal']").shouldBe(visible);

        float expectedTotal = Math.round((price1 + price2 + price3) * 100) / 100.0f;

        String actualTotal = $x("//*[@id='total-price']").shouldBe(visible).getText();

        assertThat(Float.parseFloat(actualTotal.replace(",", ".")))
                .isEqualTo(expectedTotal);

        $x("//*[@id='makeOrder']").click();

        $x("//*[@id='toast-container']//*[contains(@class,'toast') and contains(text(),'Заказ принят в обработку!')]")
                .shouldBe(visible);
    }

    @Test
    void checkCartTotalWithDifferentGoods() {
        String name1 = GoodsApi.uniqueName();
        String name2 = GoodsApi.uniqueName();
        String name3 = GoodsApi.uniqueName();

        Integer id1 = GoodsApi.createGood(name1, 50.0f);
        Integer id2 = GoodsApi.createGood(name2, 70.0f);
        Integer id3 = GoodsApi.createGood(name3, 30.0f);

        createdGoodIds.addAll(List.of(id1, id2, id3));

        refresh();

        $x("//*[@id='card-" + id1 + "']").shouldBe(visible);
        $x("//*[@id='card-" + id2 + "']").shouldBe(visible);
        $x("//*[@id='card-" + id3 + "']").shouldBe(visible);

        $x("//*[@id='card-" + id1 + "']//button[@data-action='add-to-cart']").click();
        $x("//*[@id='card-" + id2 + "']//button[@data-action='add-to-cart']").click();
        $x("//*[@id='card-" + id3 + "']//button[@data-action='add-to-cart']").click();

        $x("//*[@id='open-cart-btn']").click();

        $x("//*[@id='cartModal']").shouldBe(visible);

        String actualTotal = $x("//*[@id='total-price']").shouldBe(visible).getText();

        assertThat(Float.parseFloat(actualTotal.replace(",", ".")))
                .isEqualTo(150.0f);
    }

    @Test
    void createGoodThroughAdminPanel() {
        String name = GoodsApi.uniqueName();
        float price = GoodsApi.uniquePrice();

        open(BASE_URL + "/admin");

        $x("//*[@id='n-name']").setValue(name);
        $x("//*[@id='n-price']").setValue(String.valueOf(price));
        $x("//*[@id='add-btn']").click();

        $x("//*[@id='toast-container']//*[contains(@class,'toast')]")
                .shouldBe(visible)
                .shouldHave(text("Товар успешно добавлен!"));

        String idText = $x(
                "//tbody[@id='tbody']//tr[td/input[@value='" + name + "']]/td[1]"
        ).shouldBe(visible).getText();

        Integer id = Integer.valueOf(idText);
        createdGoodIds.add(id);

        $x("//a[@href='/']").click();

        $x("//*[@id='card-" + id + "']")
                .shouldBe(visible)
                .shouldHave(attribute("data-name", name));
    }

    @Test
    void updateGoodThroughAdminPanel() {
        String oldName = GoodsApi.uniqueName();
        String newName = GoodsApi.uniqueName();

        float oldPrice = GoodsApi.uniquePrice();
        float newPrice = 77.0f;

        Integer id = GoodsApi.createGood(oldName, oldPrice);
        createdGoodIds.add(id);

        open(BASE_URL + "/admin");

        $x("//*[@id='nm-" + id + "']").setValue(newName);
        $x("//*[@id='pr-" + id + "']").setValue(String.valueOf(newPrice));

        $x("//button[@data-action='update' and @data-id='" + id + "']").click();

        $x("//*[@id='toast-container']//*[contains(@class,'toast')]")
                .shouldBe(visible)
                .shouldHave(text("Товар #" + id + " обновлен"));

        $x("//a[@href='/']").click();

        $x("//*[@id='card-" + id + "']")
                .shouldBe(visible)
                .shouldHave(attribute("data-name", newName))
                .shouldHave(attribute("data-price", "77"));
    }

    @AfterEach
    void tearDown() {
        try {
            for (Integer id : createdGoodIds) {
                GoodsApi.deleteGood(id);
            }
        } finally {
            closeWebDriver();
        }
    }
}