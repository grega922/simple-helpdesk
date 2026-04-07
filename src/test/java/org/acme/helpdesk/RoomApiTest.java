package org.acme.helpdesk.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class RoomApiTest {

    //Test listing all rooms returns seeded data
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testGetAllRooms() {
        given()
            .when().get("/v1/rooms")
            .then()
            .statusCode(200)
            .body("$.size()", greaterThanOrEqualTo(3))
            .body("name", hasItems("TEHNIKA", "STORITVE", "POGOVOR"));
    }

    //Test getting a single room by ID
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testGetRoomById() {
        given()
            .when().get("/v1/rooms/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", notNullValue())
            .body("description", notNullValue());
    }

    //Test getting a nonexistent room returns 404
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testGetNonexistentRoom() {
        given()
            .when().get("/v1/rooms/99999")
            .then()
            .statusCode(404)
            .body("error", equalTo("NOT_FOUND"));
    }

    //Test creating a new room
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testCreateRoom() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\": \"TESTNA_SOBA\", \"description\": \"Testna soba za teste\"}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("TESTNA_SOBA"))
            .body("description", equalTo("Testna soba za teste"));
    }

    //Test that a newly created room can be used for conversations
    @Test
    void testCreatedRoomCanBeUsedForConversation() {
        String operatorToken = loginAndGetToken("Operater_Petra", "PetraOp123");
        String userToken = loginAndGetToken("JanezNovak", "Janez123");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + operatorToken)
            .body("{\"name\": \"NOVA_SOBA\", \"description\": \"Nova testna soba\"}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + userToken)
            .body("{\"room\": \"NOVA_SOBA\", \"title\": \"Test v novi sobi\", \"message\": \"Testno sporočilo\"}")
            .when().post("/v1/conversations/new")
            .then()
            .statusCode(201)
            .body("room", equalTo("NOVA_SOBA"));
    }

    //Test creating a room with a duplicate name returns 400
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testCreateDuplicateRoomFails() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\": \"TEHNIKA\", \"description\": \"Duplicate\"}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(400)
            .body("error", equalTo("BAD_REQUEST"));
    }

    //Test creating a room with missing name returns validation error
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testCreateRoomMissingName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"description\": \"Soba brez imena\"}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(400);
    }

    //Test creating a room with empty fields returns validation error
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testCreateRoomEmptyFields() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\": \"\", \"description\": \"\"}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(400);
    }

    //Test creating a room with empty body returns validation error
    @Test
    @TestSecurity(user = "Operater_Petra", roles = "OPERATOR")
    void testCreateRoomEmptyBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(400);
    }

    //Test that USER role cannot access room management
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testGetAllRoomsForbiddenForUser() {
        given()
            .when().get("/v1/rooms/new")
            .then()
            .statusCode(403);
    }

    //Test that USER role cannot create rooms
    @Test
    @TestSecurity(user = "JanezNovak", roles = "USER")
    void testCreateRoomForbiddenForUser() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\": \"HACKER_SOBA\", \"description\": \"Nedovoljena\"}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(403);
    }

    //Test unauthenticated access returns 401
    @Test
    void testGetAllRoomsUnauthorized() {
        given()
            .when().get("/v1/rooms")
            .then()
            .statusCode(401);
    }

    //Test unauthenticated room creation returns 401
    @Test
    void testCreateRoomUnauthorized() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\": \"ANON\", \"description\": \"Test\"}")
            .when().post("/v1/rooms/new")
            .then()
            .statusCode(401);
    }

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
