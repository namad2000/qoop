package io.qoop.feign;

import io.qoop.feign.config.QoopFeignAutoConfiguration;
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
        QoopFeignAutoConfiguration.class,
        FeignAutoConfiguration.class
})
@EnableFeignClients("io.qoop.feign")
@TestPropertySource(properties = "integration.internal=false")
class OpenFeignNoCorrelationIntegrationTest {

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
    private TestClientNOT client;

    @Test
    void should_NOT_send_correlation_id_header() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody("OK"));
        MDC.put(LogKeys.MDC_KEY, "integration-123");
        client.call();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader(LogKeys.CORRELATION_ID_HEADER)).isNull();
        MDC.clear();
    }

    @FeignClient(name = "testClientNOT", url = "${test.url}")
    interface TestClientNOT {
        @GetMapping("/test")
        String call();
    }
}
