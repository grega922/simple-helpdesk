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
public class OperatorApiTest {

    //Test waiting conversations
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testWaitingConversations() {
        given()
                .when().get("/v1/operator/conversations/waiting")
                .then()
                .statusCode(200);
    }

    //Test active operator conversations
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testActiveConversations() {
        given()
                .when().get("/v1/operator/conversations/active")
                .then()
                .statusCode(200);
    }

    //Test unauthorized/login error for waiting conversations
    @Test
    void testWaitingConversationsUnauthorized() {
        given()
                .when().get("/v1/operator/conversations/waiting")
                .then()
                .statusCode(401);
    }

    //Test forbidden request from user with user role
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testWaitingConversationsForbiddenForUser() {
        given()
                .when().get("/v1/operator/conversations/waiting")
                .then()
                .statusCode(403);
    }

    //Test claiming a non existent conversation
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testClaimNonexistentConversation() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/v1/operator/conversations/99999/claim")
                .then()
                .statusCode(404);
    }

    //Test conversation flow between user and operator
    @Test
    void testFullConversationFlow() {
        //Get user an operator roken
        String userToken = loginAndGetToken("JanezNovak", "Janez123");
        String operatorToken = loginAndGetToken("Operater_Petra", "PetraOp123");

        //User creates initial conversation 
        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"STORITVE\", \"title\": \"Pomoč s storitvijo\", \"message\": \"Potrebujem pomoč s storitvijo.\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .body("status", equalTo("WAITING"))
            .extract().path("id");

        //Operator checks list of waiting conversations
        given()
            .header("Authorization", "Bearer " + operatorToken)
            .when().get("/v1/operator/conversations/waiting")
            .then()
            .statusCode(200);

        //Operator claims the conversation, status becomes Active
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + operatorToken)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(200)
            .body("status", equalTo("ACTIVE"))
            .body("operatorName", notNullValue());

        //Operator replies to user message
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + operatorToken)
            .body("{\"content\": \"Pozdravljeni, kako vam lahko pomagam?\"}")
            .when().post("/v1/operator/conversations/" + convId + "/messages")
            .then()
            .statusCode(201)
            .body("content", equalTo("Pozdravljeni, kako vam lahko pomagam?"))
            .body("senderRole", equalTo("OPERATOR"));

        //User replies to operator message
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"content\": \"Hvala, imam vprašanje o računu.\"}")
            .when().post("/v1/conversations/" + convId + "/messages")
            .then()
            .statusCode(201)
            .body("content", equalTo("Hvala, imam vprašanje o računu."))
            .body("senderRole", equalTo("USER"));

        //Operator gets all conversation messages
        given()
            .header("Authorization", "Bearer " + operatorToken)
            .when().get("/v1/operator/conversations/" + convId + "/messages")
            .then()
            .statusCode(200)
            .body("$", hasSize(3));
    }

    //Test two operators claiming the same conversation
    @Test
    void testTakeAlreadyActiveConversationFails() {
        //Get user and two operator tokens
        String userToken = loginAndGetToken("JanezNovak", "Janez123");
        String op1Token = loginAndGetToken("Operater_Petra", "PetraOp123");
        String op2Token = loginAndGetToken("Operater_Luka", "LukaOp123");

        //User creates initial conversation 
        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Pomoč s telefonom\",\"message\": \"Težave imam s telefonom\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        //Operator 1 claims the conversation successfully
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + op1Token)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(200);
        //Operator 1 claims the conversation with error since it's already active
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + op2Token)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(400);
    }

    //Test user opening multiple conversations
    @Test
    void testMultipleConversationsSimultaneously() {
        //Get user token
        String userToken = loginAndGetToken("JanezNovak", "Janez123");

        //Open first conversation
        int conv1 = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Tehnično vprašanje\",  \"message\": \"Vprašanje o tehniki\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        //Open second conversation
        int conv2 = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"STORITVE\", \"title\": \"Storitveno vprašanje\",  \"message\": \"Vprašanje o storitvah\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        org.junit.jupiter.api.Assertions.assertNotEquals(conv1, conv2);
    }

    //Test operator accessing another operator conversation
    @Test
    void testOperatorCannotAccessOtherOperatorsConversation() {
        //Get user/operator token
        String userToken = loginAndGetToken("JanezNovak", "Janez123");
        String op1Token = loginAndGetToken("Operater_Petra", "PetraOp123");
        String op2Token = loginAndGetToken("Operater_Luka", "LukaOp123");

        //User creates initial conversation
        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"POGOVOR\", \"title\": \"Test ownership\", \"message\": \"Test ownership\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        //Operator 1 claims the conversation
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + op1Token)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(200);

        //Operator 2 tries to access the conversation and gets error
        given()
            .header("Authorization", "Bearer " + op2Token)
            .when().get("/v1/operator/conversations/" + convId)
            .then()
            .statusCode(403);
    }

    //Test that operator's active conversations list contains the claimed conversation
    @Test
    void testActiveConversationsContainsClaimedConversation() {
        String userToken = loginAndGetToken("JanezNovak", "Janez123");
        String operatorToken = loginAndGetToken("Operater_Petra", "PetraOp123");

        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Test aktivnih pogovorov\", \"message\": \"Testno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + operatorToken)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(200);

        given()
            .header("Authorization", "Bearer " + operatorToken)
            .when().get("/v1/operator/conversations/active")
            .then()
            .statusCode(200)
            .body("$.size()", org.hamcrest.Matchers.greaterThanOrEqualTo(1))
            .body("id", org.hamcrest.Matchers.hasItem(convId));
    }

    //Test that operator cannot read messages of another operator's conversation
    @Test
    void testOperatorCannotReadOtherOperatorsMessages() {
        String userToken = loginAndGetToken("JanezNovak", "Janez123");
        String op1Token = loginAndGetToken("Operater_Petra", "PetraOp123");
        String op2Token = loginAndGetToken("Operater_Luka", "LukaOp123");

        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"TEHNIKA\", \"title\": \"Test branja sporočil\", \"message\": \"Testno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + op1Token)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(200);

        given()
            .header("Authorization", "Bearer " + op2Token)
            .when().get("/v1/operator/conversations/" + convId + "/messages")
            .then()
            .statusCode(403);
    }

    //Test that operator cannot send a message to another operator's conversation
    @Test
    void testOperatorCannotSendMessageToOtherOperatorsConversation() {
        String userToken = loginAndGetToken("JanezNovak", "Janez123");
        String op1Token = loginAndGetToken("Operater_Petra", "PetraOp123");
        String op2Token = loginAndGetToken("Operater_Luka", "LukaOp123");

        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"STORITVE\", \"title\": \"Test pošiljanja sporočil\", \"message\": \"Testno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + op1Token)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + op2Token)
            .body("{\"content\": \"Nepooblaščeno sporočilo\"}")
            .when().post("/v1/operator/conversations/" + convId + "/messages")
            .then()
            .statusCode(403);
    }

    //Test sending empty message content returns validation error
    @Test
    void testSendEmptyMessageFails() {
        String userToken = loginAndGetToken("JanezNovak", "Janez123");
        String operatorToken = loginAndGetToken("Operater_Petra", "PetraOp123");

        int convId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"POGOVOR\", \"title\": \"Test praznega sporočila\", \"message\": \"Začetno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + operatorToken)
            .when().post("/v1/operator/conversations/" + convId + "/claim")
            .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + operatorToken)
            .body("{\"content\": \"\"}")
            .when().post("/v1/operator/conversations/" + convId + "/messages")
            .then()
            .statusCode(400);
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
