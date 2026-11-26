package utils;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.basic;
import static io.restassured.http.ContentType.JSON;

public class GoodsApi {

    private static final String BASE_URI = "http://localhost:8080";
    private static final String LOGIN = System.getenv("USERNAME");
    private static final String PASSWORD = System.getenv("PASSWORD");

    private static final RequestSpecification requestSpecification =
            new RequestSpecBuilder()
                    .setBaseUri(BASE_URI)
                    .setAuth(basic(LOGIN, PASSWORD))
                    .build();

    public static String uniqueName() {
        return "Test_" + UUID.randomUUID();
    }

    public static float uniquePrice() {
        return Math.round(new Random().nextFloat(0, 100) * 100) / 100.0f;
    }

    public static Integer createGood(String name, float price) {
        return given()
                .spec(requestSpecification)
                .contentType(JSON)
                .body("""
                        {
                            "name": "%s",
                            "price": %s
                        }
                        """.formatted(name, price))

                .when()
                .post("/goods/add")

                .then()
                .statusCode(200)
                .extract()
                .path("data.id");
    }

    public static void deleteGood(Integer id) {
        given()
                .spec(requestSpecification)

                .when()
                .delete("/goods/{id}", id)

                .then()
                .statusCode(200);
    }
}