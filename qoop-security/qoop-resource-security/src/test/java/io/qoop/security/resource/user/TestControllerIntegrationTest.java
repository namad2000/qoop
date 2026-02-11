package io.qoop.security.resource.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        CurrentUserArgumentResolver.class,
        TestUserController.class,
})
class TestControllerIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 1. Manually create the resolver
        CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

        // 2. Build MockMvc standalone
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestUserController())
                .setCustomArgumentResolvers(resolver)
                .build();

        // 3. Set the security context
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "JohnDoe",
                "password",
                new ArrayList<>() // This is the magic part
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getMyName_returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/test/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("Current user is: JohnDoe"));
    }
}

