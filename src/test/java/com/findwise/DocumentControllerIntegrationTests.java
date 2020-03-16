package com.findwise;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInRelativeOrder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentControllerIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    private static String payloadMultipleDocuments = "[\n" +
            "  \"the brown fox jumped over the brown dog\",\n" +
            "  \"the lazy brown dog sat in the corner\",\n" +
            "  \"the red fox bit the lazy dog\"\n" +
            "]";

    @Test
    void addDocumentsTest() throws Exception {
        given().contentType(ContentType.JSON)
                .body(payloadMultipleDocuments)
                .post(restTemplate.getRootUri()+"/api/documents")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void searchTest() throws Exception {
        given().contentType(ContentType.JSON)
                .body(payloadMultipleDocuments)
                .post(restTemplate.getRootUri()+"/api/documents")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value());

        given().contentType(ContentType.JSON)
                .param("searchTerm", "brown")
                .get(restTemplate.getRootUri()+"/api/search")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",containsInRelativeOrder("document1","document2"));

        given().contentType(ContentType.JSON)
                .param("searchTerm", "fox")
                .get(restTemplate.getRootUri()+"/api/search")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id",containsInRelativeOrder("document3","document1"));
    }

    @Test
    void getAllDocumentsTest() throws Exception {
        given().contentType(ContentType.JSON)
                .body(payloadMultipleDocuments)
                .post(restTemplate.getRootUri()+"/api/documents")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value());

        given().contentType(ContentType.JSON)
                .get(restTemplate.getRootUri()+"/api/documents")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
