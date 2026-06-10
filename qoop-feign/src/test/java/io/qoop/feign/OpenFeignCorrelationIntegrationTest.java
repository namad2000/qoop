package io.qoop.feign;

import io.qoop.feign.config.QoopFeignConfiguration;
import io.qoop.logs.LogKeys;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {
        CorrelationInterceptor.class,
        QoopFeignConfiguration.class,
        FeignAutoConfiguration.class
})
@EnableFeignClients("io.qoop.feign")
@TestPropertySource(properties = "integration.internal=true")
class OpenFeignCorrelationIntegrationTest {

    static MockWebServer mockWebServer;

    @BeforeAll
    static void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("test.url", () -> mockWebServer.url("/").toString());
    }

    @Autowired
    private TestClient client;

    @Test
    void should_send_correlation_id_header() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody("OK"));
        MDC.put(LogKeys.MDC_KEY, "integration-123");
        client.call();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader(LogKeys.CORRELATION_ID_HEADER)).isEqualTo("integration-123");
        MDC.clear();
    }

    @FeignClient(name = "testClient", url = "${test.url}")
    interface TestClient {
        @GetMapping("/test")
        String call();
    }
}
