package io.qoop.security.resource.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestControllerIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 1. Manually create the resolver
        CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

        // 2. Setup MockMvc in Standalone mode
        // This bypasses full context loading and explicitly registers the resolver
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestUserController())
                .setCustomArgumentResolvers(resolver) // <-- Register the resolver
                .build();

        // 3. Setup SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "JohnDoe",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getMyName_returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/test/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("Current user is: " + "JohnDoe"));
    }
}