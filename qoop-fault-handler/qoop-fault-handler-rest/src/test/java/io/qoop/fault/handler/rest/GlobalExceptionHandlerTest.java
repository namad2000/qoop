package io.qoop.fault.handler.rest;

import io.qoop.message.api.core.ErrorMessageResolver;
import io.qoop.message.api.core.I18nConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static io.qoop.logs.LogKeys.MDC_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    public void testDomainValidationExceptionHandler() throws Exception {
        when(resolver.resolve(eq("validation.required"), any(Locale.class), eq("email")))
                .thenReturn("Field Email is required");

        mockMvc.perform(get("/test/validation-exception")
                        .header("Accept-Language", "en"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation.required"))
                .andExpect(jsonPath("$.message").value("Field Email is required"))
                .andExpect(jsonPath("$.field").value("email"));
    }

    @Test
    public void testDomainExceptionHandler() throws Exception {
        when(resolver.resolve(eq("business.rule.failed"), any(Locale.class), eq("Some parameter")))
                .thenReturn("Some parameter caused a conflict");

        mockMvc.perform(get("/test/domain-exception")
                        .header("Accept-Language", "en"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("business.rule.failed"))
                .andExpect(jsonPath("$.message").value("Some parameter caused a conflict"));
    }

    @Test
    public void testDomainBusinessExceptionHandler() throws Exception {
        when(resolver.resolve(eq("business.logic.error"), eq(Locale.of("fa")), eq("AccountBalance")))
                .thenReturn("Insufficient funds in AccountBalance");

        mockMvc.perform(get("/test/business-exception")
                        .header("Accept-Language", "fa"))
                .andExpect(status().isUnprocessableEntity()) // Checking code 422
                .andExpect(jsonPath("$.code").value("business.logic.error"))
                .andExpect(jsonPath("$.message").value("Insufficient funds in AccountBalance"));
    }

    @Test
    public void testHandleGenericException() throws Exception {
        // Setting the showMessage field to false using Reflection
        org.springframework.test.util.ReflectionTestUtils
                .setField(globalExceptionHandler, "showMessage", false);

        when(resolver.resolve(eq("INTERNAL_ERROR"), any(Locale.class)))
                .thenReturn("An internal server error occurred. Please try again later.");

        mockMvc.perform(get("/test/generic-exception")
                        .header("Accept-Language", "en"))
                .andExpect(status().isInternalServerError()) // Checking code 500
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An internal server error occurred. Please try again later."));
    }

    @Test
    public void testHandleGenericException_WhenShowMessageIsFalse() throws Exception {
        // Setting the showMessage field to false using Reflection
        org.springframework.test.util.ReflectionTestUtils
                .setField(globalExceptionHandler, "showMessage", false);

        when(resolver.resolve(eq("INTERNAL_ERROR"), any(Locale.class)))
                .thenReturn("Internal Server Error occurred");

        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Internal Server Error occurred"));
    }

    @Test
    public void testHandleGenericException_WhenShowMessageIsTrue() throws Exception {
        // Setting the showMessage field to true
        org.springframework.test.util.ReflectionTestUtils
                .setField(globalExceptionHandler, "showMessage", true);

        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Original Exception Message"));
    }

    @Test
    public void testHandleGenericException_ReturnsCorrelationId() throws Exception {
        String mockId = "req-123-abc";
        MDC.put(MDC_KEY, mockId);

        try {
            mockMvc.perform(get("/test/runtime-exception")
                            .header("Accept-Language", "en"))
                    .andExpect(status().isInternalServerError())
                    // Check the presence of the field in the output
                    .andExpect(jsonPath("$.correlationId").value(mockId))
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
