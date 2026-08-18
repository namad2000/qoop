package io.qoop.unifier.response.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = TestMvcConfig.class)
class BodyRewriteTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_wrap_response_with_timestamp_when_status_is_ok() throws Exception {
        mockMvc.perform(get("/test/ok")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.correlationId").value("test-correlation-id"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.name").value("davood"));
    }

    @Test
    void should_wrap_string_response_when_status_is_ok() throws Exception {
        mockMvc.perform(get("/test/string")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.correlationId").value("test-correlation-id"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").value("plain string data"));
    }

    @Test
    void should_return_original_body_without_wrapping_when_status_is_error() throws Exception {
        mockMvc.perform(get("/test/error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad request"))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.correlationId").doesNotExist())
                .andExpect(jsonPath("$.timestamp").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void should_bypass_media_responses() throws Exception {
        mockMvc.perform(get("/test/image")
                        .accept(MediaType.IMAGE_PNG))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().string(containsString("fake-image-bytes")))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void should_bypass_plain_text_responses() throws Exception {
        mockMvc.perform(get("/test/plain-text")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("raw text content"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }
}