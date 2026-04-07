package org.acme.helpdesk.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class AuthApiTest {

    //Test successful user login
    @Test
    void testLoginUserSuccess() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"JanezNovak\", \"password\": \"Janez123\"}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("JanezNovak"))
                .body("role", equalTo("USER"))
                .body("expire", greaterThan(0));
    }

    //Test successful operator login
    @Test
    void testLoginOperatorSuccess() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"Operater_Petra\", \"password\": \"PetraOp123\"}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("Operater_Petra"))
                .body("role", equalTo("OPERATOR"))
                .body("expire", greaterThan(0));
    }

    //Test invalid user password
    @Test
    void testLoginInvalidPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"JanezNovak\", \"password\": \"Janez\"}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(401);
    }

    //Test user that does not exist!
    @Test
    void testLoginNonexistentUser() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"Uporabnik\", \"password\": \"Janez123\"}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(401);
    }

    //Test if body is missing
    @Test
    void testLoginMissingFields() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(400);
    }

    //Test login with username only (missing password)
    @Test
    void testLoginMissingPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"JanezNovak\"}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(400);
    }

    //Test login with password only (missing username)
    @Test
    void testLoginMissingUsername() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"password\": \"Janez123\"}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(400);
    }

    //Test that failed login returns structured error response body
    @Test
    void testLoginErrorResponseBody() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"JanezNovak\", \"password\": \"wrong\"}")
                .when().post("/v1/auth/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"))
                .body("message", notNullValue())
                .body("timestamp", notNullValue());
    }

}
