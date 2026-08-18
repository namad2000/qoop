package io.qoop.fault.handler.rest;

import io.qoop.logs.DomainLogger;
import io.qoop.message.api.core.ErrorMessageResolver;
import io.qoop.message.api.core.I18nConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static io.qoop.logs.LogKeys.MDC_KEY;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@Import({GlobalExceptionHandler.class, I18nConfig.class, TestController.class})
public class GlobalExceptionHandlerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ErrorMessageResolver resolver;

    @MockitoBean
    private DomainLogger domainLogger;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    public void testMethodArgumentNotValidException_HibernateValidator() throws Exception {
        when(resolver.resolve(eq("VALIDATION_FAILED"), any(Locale.class)))
                .thenReturn("Validation failed for one or more fields");

        when(resolver.resolveField(eq("name"), any(Locale.class))).thenReturn("Name Field");
        when(resolver.resolveField(eq("age"), any(Locale.class))).thenReturn("Age Field");

        when(resolver.resolve(eq("Name cannot be blank"), any(Locale.class))).thenReturn("Name cannot be blank");
        when(resolver.resolve(eq("Age must be at least 18"), any(Locale.class))).thenReturn("Age must be at least 18");

        String invalidJson = """
                {
                    "name": "",
                    "age": 15
                }
                """;

        mockMvc.perform(post("/test/dto-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .header("Accept-Language", "en"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(2)));
    }

    @Test
    public void testAuthenticationException_Unauthorized() throws Exception {
        when(resolver.resolve(eq("UNAUTHORIZED_ERROR"), any(Locale.class)))
                .thenReturn("Authentication is required.");

        mockMvc.perform(get("/test/unauthorized-exception")
                        .header("Accept-Language", "en"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_ERROR"))
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    public void testAccessDeniedException_Forbidden() throws Exception {
        when(resolver.resolve(eq("FORBIDDEN_ERROR"), any(Locale.class)))
                .thenReturn("Access denied.");

        mockMvc.perform(get("/test/forbidden-exception")
                        .header("Accept-Language", "en"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_ERROR"))
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    @Test
    public void testConstraintViolationException_HibernateValidator() throws Exception {
        when(resolver.resolve(eq("CONSTRAINT_VIOLATION"), any(Locale.class)))
                .thenReturn("Constraint violation occurred");

        when(resolver.resolveField(eq("validateParam.count"), any(Locale.class))).thenReturn("Count Param");
        when(resolver.resolve(eq("Value must be at least 10"), any(Locale.class))).thenReturn("Value must be at least 10");

        mockMvc.perform(get("/test/param-validation")
                        .param("count", "5")
                        .header("Accept-Language", "en"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").value("CONSTRAINT_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Constraint violation occurred"));
    }

    @Test
    public void testDomainValidationExceptionHandler() throws Exception {
        when(resolver.resolve(eq("validation.required"), any(Locale.class), eq("email")))
                .thenReturn("Field Email is required");

        mockMvc.perform(get("/test/validation-exception")
                        .header("Accept-Language", "en"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").value("validation.required"))
                .andExpect(jsonPath("$.field").value("email"));
    }

    @Test
    public void testDomainExceptionHandler() throws Exception {
        when(resolver.resolve(eq("business.rule.failed"), any(Locale.class), eq("Some parameter")))
                .thenReturn("Some parameter caused a conflict");

        mockMvc.perform(get("/test/domain-exception")
                        .header("Accept-Language", "en"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").value("business.rule.failed"));
    }

    @Test
    public void testHandleGenericException_ReturnsCorrelationId() throws Exception {
        String mockId = "req-123-abc";
        MDC.put(MDC_KEY, mockId);

        try {
            when(resolver.resolve(eq("INTERNAL_ERROR"), any(Locale.class)))
                    .thenReturn("Internal Server Error occurred");

            mockMvc.perform(get("/test/runtime-exception")
                            .header("Accept-Language", "en"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.correlationId").value(mockId))
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}