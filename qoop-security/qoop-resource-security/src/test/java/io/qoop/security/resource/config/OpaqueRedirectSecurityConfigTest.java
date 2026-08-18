package io.qoop.security.resource.config;

import io.qoop.security.config.SecurityProperties;
import io.qoop.security.resource.config.controller.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("verify")
@ContextConfiguration(classes = {
        OpaqueSecurityConfiguration.class,
        TestController.class,
        SecurityProperties.class
})
@TestPropertySource(properties = {
        "security.opaque.oauth2.auth-endpoint=http://auth-server/oauth/authorize",
        "security.opaque.oauth2.verify-endpoint=http://localhost:8080/oauth/check_token",
        "security.opaque.oauth2.client-id=test-client-id",
        "security.opaque.oauth2.client-secret=test-client-secret"
})
class OpaqueRedirectSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpaqueTokenIntrospector opaqueTokenIntrospector;

    @Test
    @DisplayName("Should redirect 302 to auth endpoint when auth-endpoint property is present")
    void shouldRedirectToAuthEndpointWhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://auth-server/oauth/authorize"));
    }
}