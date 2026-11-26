
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codeborne.selenide.Configuration;

import java.util.UUID;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class SelenideTests {

    private static final String BASE_URL = "http://localhost:8080";

    private static final String USERNAME =
            System.getenv("USERNAME");

    private static final String PASSWORD =
            System.getenv("PASSWORD");

    @BeforeEach
    void setup() {
        Configuration.browser = "chrome";
        Configuration.timeout = 10000;

        open(BASE_URL + "/admin");

        if ($x("//*[@id='username']").exists()) {
            login(USERNAME, PASSWORD);
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

    private void createProduct(String name, String price) {
        open(BASE_URL + "/admin");

        $x("//*[@id='n-name']")
                .shouldBe(visible)
                .setValue(name);

        $x("//*[@id='n-price']")
                .shouldBe(visible)
                .setValue(price);

        $x("//*[@id='add-btn']")
                .shouldBe(visible, enabled)
                .click();

        $x("//a[normalize-space()='Вернуться на сайт']")
                .shouldBe(visible)
                .click();

        $x(productCardXpath(name))
                .shouldBe(visible);
    }

    private String productCardXpath(String name) {
        return "//div[@class='product-card' and @data-name='"
                + name + "']";
    }

    private String cartItemXpath(String name) {
        return "//div[@id='cart-items']/div[@class='cart-item']"
                + "[.//b[normalize-space()='" + name + "']]";
    }

    private void addProductToCart(String name) {
        $x(productCardXpath(name)
                + "//button[@data-action='add-to-cart']")
                .shouldBe(visible, enabled)
                .click();

        $x("//*[@id='open-cart-btn']")
                .shouldBe(visible)
                .click();

        $x("//*[@id='cartModal']").shouldBe(visible);
    }

    @Test
    void shouldAddProductThroughAdminAndDisplayItOnStorefront() {
        String productName = "AdminSelenideTest_" + UUID.randomUUID();
        String productPrice = "55.55";

        createProduct(productName, productPrice);

        $x(productCardXpath(productName) + "/h4")
                .shouldBe(visible, exactText(productName));
    }

    @Test
    void shouldAddProductToCart() {
        String productName = "SelenideTest_" + UUID.randomUUID();
        String productPrice = "55.55";

        createProduct(productName, productPrice);

        addProductToCart(productName);

        $x(cartItemXpath(productName))
                .shouldBe(
                        visible,
                        text(productName),
                        text(productPrice + " ₽")
                );
    }

    @Test
    void shouldNotLoginWithInvalidCredentials() {

        clearBrowserCookies();

        open(BASE_URL + "/login");

        login("wrong_username", "wrong_password");

        $x("//div[contains(@class, 'alert-danger')]")
                .shouldBe(
                        visible,
                        exactText("Неверные учетные данные пользователя")
                );
    }

    @Test
    void shouldSaveCartItemsAfterPageRefresh() {
        String productName = "RefreshSelenideTest_" + UUID.randomUUID();
        String productPrice = "55.55";

        createProduct(productName, productPrice);

        addProductToCart(productName);

        refresh();

        $x("//*[@id='open-cart-btn']").shouldBe(visible);

        $x("//*[@id='open-cart-btn']").click();

        $x("//*[@id='cartModal']").shouldBe(visible);

        $x(cartItemXpath(productName))
                .shouldBe(
                        visible,
                        text(productName),
                        text(productPrice + " ₽")
                );
    }

    @Test
    void shouldShowAlertWhenOrderExceeds300Rubles() {

        String productName = "ExpensiveTest_" + UUID.randomUUID();

        int productPrice = 200;
        int amount = 2;
        int orderLimit = 300;

        int orderTotal = productPrice * amount;

        createProduct(productName, String.valueOf(productPrice));

        for (int i = 0; i < amount; i++) {
            $x(productCardXpath(productName)
                    + "//button[@data-action='add-to-cart']")
                    .shouldBe(visible, enabled)
                    .click();
        }

        $x("//*[@id='open-cart-btn']")
                .shouldBe(visible)
                .click();

        $x("//*[@id='makeOrder']")
                .shouldBe(visible, enabled)
                .click();

        String expectedAlert = "[SmartShop]: Денег не хватает! Сумма "
                + orderTotal + " ₽ превышает лимит "
                + orderLimit + " ₽.";

        confirm(expectedAlert);
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }
}
