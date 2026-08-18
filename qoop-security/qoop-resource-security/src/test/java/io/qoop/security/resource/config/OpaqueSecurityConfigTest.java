package io.qoop.security.resource.config;

import io.qoop.security.api.PrefixPath;
import io.qoop.security.config.SecurityProperties;
import io.qoop.security.resource.config.controller.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        "security.whitelist-urls=/public/api",
        "security.opaque.oauth2.verify-endpoint=http://localhost:8080/oauth/check_token",
        "security.opaque.oauth2.client-id=test-client-id",
        "security.opaque.oauth2.client-secret=test-client-secret"
})
class OpaqueSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpaqueTokenIntrospector opaqueTokenIntrospector;

    @Test
    @DisplayName("Whitelisted endpoints should be accessible without token")
    void whitelistedEndpoint_withoutToken_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/public/api"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Internal prefix endpoints should be accessible without token")
    void publicEndpoint_shouldBeAccessibleWithoutToken() throws Exception {
        // Public endpoint should be accessible without authentication
        mockMvc.perform(get(PrefixPath.INTERNAL.concat("/hello")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint without token should return 401 Unauthorized")
    void privateEndpoint_withoutToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/private/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint with valid opaque token should return 200 OK")
    void privateEndpoint_withOpaqueToken_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/private/hello")
                        .with(opaqueToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin endpoint with user authority should return 403 Forbidden")
    void adminEndpoint_withoutAdminRole_shouldReturnForbidden() throws Exception {
        // JWT exists but without ROLE_ADMIN
        mockMvc.perform(get("/admin/hello")
                        .with(opaqueToken().authorities(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoint with admin authority should return 200 OK")
    void adminEndpoint_withAdminRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/admin/hello")
                        .with(opaqueToken().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RolesAllowed endpoint with wrong role should return 403 Forbidden")
    void rolesAllowedEndpoint_withoutAllUsersRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/all-users/rolesAllowed")
                        .with(opaqueToken().authorities(new SimpleGrantedAuthority("ROLE_OTHER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RolesAllowed endpoint with ALL_USERS role should return 200 OK")
    void rolesAllowedEndpoint_withAllUsersRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/all-users/rolesAllowed")
                        .with(opaqueToken().authorities(new SimpleGrantedAuthority("ROLE_ALL_USERS"))))
                .andExpect(status().isOk());
    }
}