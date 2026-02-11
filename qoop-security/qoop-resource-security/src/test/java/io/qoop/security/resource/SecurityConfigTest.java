package io.qoop.security.resource;

import io.qoop.security.api.PrefixPath;
import io.qoop.security.config.SecurityProperties;
import io.qoop.security.resource.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        TestController.class,
        SecurityProperties.class
})
@TestPropertySource(properties = {
        "security.whitelist-urls=/public/api,/another-whitelisted-url"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void whitelistedEndpoint_withoutToken_shouldReturnOk() throws Exception {
        // Assuming in test settings, you have added the address "/public/api" to the whitelist
        // This address should not require authentication
        mockMvc.perform(get("/public/api"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutToken() throws Exception {
        // Public endpoint should be accessible without authentication
        mockMvc.perform(get(PrefixPath.INTERNAL.concat("/hello")))
                .andExpect(status().isOk());
    }

    @Test
    void privateEndpoint_withoutToken_shouldReturnUnauthorized() throws Exception {
        // Private endpoint without token should return 401 Unauthorized
        mockMvc.perform(get("/private/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateEndpoint_withJwt_shouldReturnOk() throws Exception {
        // Private endpoint with valid JWT should return 200 OK
        mockMvc.perform(get("/private/hello")
                        .with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_withoutAdminRole_shouldReturnForbidden() throws Exception {
        // JWT exists but without ROLE_ADMIN
        mockMvc.perform(get("/admin/hello")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_withAdminRole_shouldReturnOk() throws Exception {
        // JWT with ROLE_ADMIN
        mockMvc.perform(get("/admin/hello")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )))
                .andExpect(status().isOk());
    }
}
