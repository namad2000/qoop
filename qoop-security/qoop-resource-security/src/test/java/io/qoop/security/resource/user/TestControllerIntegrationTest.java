package io.qoop.security.resource.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestControllerIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 1. Create a simple resolver specifically for the test
        HandlerMethodArgumentResolver testResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(io.qoop.security.api.CurrentUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                // In the test, we simply return a user object
                // (Alternatively, you could copy the main resolveCurrentUser logic here)
                return AuthenticatedUser.builder()
                        .username("JohnDoe")
                        .build();
            }
        };

        // 2. Setup MockMvc with the test resolver
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestUserController())
                .setCustomArgumentResolvers(testResolver) // Use the test resolver
                .build();

        // 3. Setup SecurityContext (if needed by services)
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
                .andExpect(content().string("Current user is: JohnDoe"));
    }
}