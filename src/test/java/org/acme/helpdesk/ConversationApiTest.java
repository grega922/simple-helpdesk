package org.acme.helpdesk.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class ConversationApiTest {
 
    //Test for user to create a successful conversation
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testCreateConversation() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Testna pogovorna tema\", \"message\": \"Imam težavo z nastavitvami.\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("room", equalTo("TEHNIKA"))
            .body("status", equalTo("WAITING"))
            .body("title", equalTo("Testna pogovorna tema"));
    }

    //Test to check error if invalid room is used
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testCreateConversationInvalidRoom() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"SUPPORT\", \"title\": \"Testna pogovorna tema\", \"message\": \"Test\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(404);
    }

    //Test to check error if title is missing
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testCreateConversationNoTitle() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"STORITVE\", \"message\": \"Test\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(400);
    }

    //Test to check error if message is empty
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testCreateConversationEmptyMessage() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"STORITVE\", \"title\": \"Testna pogovorna tema\", \"message\": \"\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(400);
    }

    //Test to check if conversation is created and retrieved
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testMyConversations() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"POGOVOR\", \"title\": \"Testna pogovorna tema\", \"message\": \"Pozdravljen!\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201);

        given()
            .when().get("/v1/conversations")
            .then()
            .statusCode(200)
            .body("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    //Test to check if conversation is created first message is received
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testGetConversationMessages() {
        int convId = given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Testna pogovorna tema\", \"message\": \"Testno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when().get("/v1/conversations/" + convId + "/messages")
            .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].content", equalTo("Testno sporočilo"))
            .body("[0].senderRole", equalTo("USER"));
    }

    //Test to check if conversation is created and polling of messages works as expected
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testGetMessagesSinceFiltering() {
        int convId =given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Testna pogovorna tema\", \"message\": \"Staro sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when().get("/v1/conversations/" + convId + "/messages?since=2099-01-01T00:00:00")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    //Test to check if other user's conversation can not be accessed
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testGetOtherUsersConversationForbidden() {
        String userToken = loginAndGetToken("AnaKovac", "Ana456");

        int convId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body("{\"room\": \"TEHNIKA\", \"title\": \"Testna pogovorna tema\", \"message\": \"Test sporočilo\"}")
                .when().post("/v1/conversations/new")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
            .when().get("/v1/conversations/" + convId)
            .then()
            .statusCode(403);
    }

    //Test to check if user is logged in before creating a conversation
    @Test
    void testCreateConversationUnauthorized() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Testna pogovorna tema\", \"message\": \"Test\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(401);
    }

    //Test to check if operator is forbidden to create a conversation
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testCreateConversationForbiddenForOperator() {
        given()  
            .contentType(ContentType.JSON)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Testna pogovorna tema\", \"message\": \"Test\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(403);
    }

    //Test to check if user can not send message to conversation that is still waiting for operator
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testSendMessageToWaitingConversationFails() {
        int convId = given()
            .contentType(ContentType.JSON)
            .body("{\"room\": \"STORITVE\", \"title\": \"Testna pogovorna tema\", \"message\": \"Začetno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .contentType(ContentType.JSON)
            .body("{\"content\": \"To ne bi smelo delati\"}")
            .when().post("/v1/conversations/" + convId + "/messages")
            .then()
            .statusCode(400);
    }


    //Test accessing a nonexistent conversation returns 404
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testGetNonexistentConversation() {
        given()
            .when().get("/v1/conversations/99999")
            .then()
            .statusCode(404);
    }

    //Test that another user cannot read messages from a conversation they don't own
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testGetOtherUsersMessagesForbidden() {
        String userToken = loginAndGetToken("AnaKovac", "Ana456");

        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"POGOVOR\", \"title\": \"Zasebna tema\", \"message\": \"Zasebno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when().get("/v1/conversations/" + convId + "/messages")
            .then()
            .statusCode(403);
    }

    //Test error response body structure on validation error
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testCreateConversationValidationErrorBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(400)
            .body("title", notNullValue());
    }

    //Helper method to login and get token for user or operator
    private String loginAndGetToken(String username, String password) {
        return given()
            .contentType(ContentType.JSON)
            .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
            .when().post("/v1/auth/login")
            .then()
            .statusCode(200)
            .extract().path("token");
    }
}
