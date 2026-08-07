package io.qoop.test.e2e.base.config;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 5/3/2026 3:25 PM
 * Package: ir.tamin.finance.hub.integration
 */

@ActiveProfiles("test")
@WireMockTest(httpPort = 666) // WireMock runs on port 666
public abstract class BaseE2eTest {

    @LocalServerPort
    protected int port;

    protected RequestSpecification requestSpec;

    public abstract String getToken();

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .addHeader("Authorization", "Bearer " + getToken())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}