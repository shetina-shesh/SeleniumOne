import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SeleniumTests {

    private static final String BASE_URL = "http://localhost:8080";

    private static final String USERNAME = System.getenv("USERNAME");
    private static final String PASSWORD = System.getenv("PASSWORD");

    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(BASE_URL + "/admin");

        if (!driver.findElements(By.id("username")).isEmpty()) {
            login(USERNAME, PASSWORD);
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("tbody")
        ));

        driver.get(BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("products-list")
        ));
    }

    private void login(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("username")
        )).sendKeys(username);

        driver.findElement(By.id("password")).sendKeys(password);

        driver.findElement(
                By.xpath("//button[@type='submit']")
        ).click();
    }

    private void createProduct(String name, String price) {
        driver.get(BASE_URL + "/admin");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("n-name")
        )).sendKeys(name);

        driver.findElement(By.id("n-price")).sendKeys(price);

        driver.findElement(By.id("add-btn")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//tbody[@id='tbody']//input[@value='" + name + "']")
        ));

        driver.findElement(
                By.linkText("Вернуться на сайт")
        ).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("products-list")
        ));
    }

    private void addProductToCart(String name) {
        String productCard = "//div[@class='product-card' and @data-name='"
                + name + "']";

        WebElement addToCartButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath(productCard
                                + "//button[@data-action='add-to-cart']")
                )
        );

        addToCartButton.click();

        driver.findElement(By.id("open-cart-btn")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("cartModal")
        ));
    }

    @Test
    void shouldAddProductThroughAdminAndDisplayItOnStorefront() {
        String productName = "AdminTest_" + UUID.randomUUID();
        String productPrice = "55.55";

        createProduct(productName, productPrice);

        String actualName = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[@class='product-card' and @data-name='"
                                + productName + "']/h4")
                )
        ).getText();

        assertThat(actualName)
                .as("Проверка отображения созданного товара на витрине")
                .isEqualTo(productName);
    }

    @Test
    void shouldAddProductToCart() {
        String productName = "SeleniumTest_" + UUID.randomUUID();
        String productPrice = "55.55";

        createProduct(productName, productPrice);

        addProductToCart(productName);

        List<WebElement> cartItems = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.xpath("//div[@id='cart-items']/div[@class='cart-item']")
                )
        );

        assertThat(cartItems)
                .as("Проверка наличия товара в корзине")
                .anySatisfy(item ->
                        assertThat(item.getText())
                                .contains(productName, productPrice + " ₽")
                );
    }

    @Test
    void shouldNotLoginWithInvalidCredentials() {

        driver.manage().deleteAllCookies();

        driver.get(BASE_URL + "/login");

        login("wrong_username", "wrong_password");

        String actualErrorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class, 'alert-danger')]")
                )
        ).getText();

        assertThat(actualErrorMessage)
                .as("Проверка сообщения при вводе неправильных учётных данных")
                .isEqualTo("Неверные учетные данные пользователя");
    }

    @Test
    void shouldSaveCartItemsAfterPageRefresh() {
        String productName = "RefreshTest_" + UUID.randomUUID();
        String productPrice = "55.55";

        createProduct(productName, productPrice);

        addProductToCart(productName);

        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("open-cart-btn")
        ));

        driver.findElement(By.id("open-cart-btn")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("cartModal")
        ));

        List<WebElement> cartItems = driver.findElements(
                By.xpath("//div[@id='cart-items']/div[@class='cart-item']")
        );

        assertThat(cartItems)
                .as("Проверка сохранения товара в корзине после обновления страницы")
                .anySatisfy(item ->
                        assertThat(item.getText())
                                .contains(productName, productPrice + " ₽")
                );
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
