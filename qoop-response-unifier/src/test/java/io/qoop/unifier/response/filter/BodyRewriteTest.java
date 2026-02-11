package io.qoop.unifier.response.filter;

import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@ContextConfiguration(classes = TestMvcConfig.class)
class BodyRewriteTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_wrap_response_when_status_is_200() throws Exception {

        mockMvc.perform(get("/test/ok")
                        .accept(MediaType.APPLICATION_JSON.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.correlationId").value("test-correlation-id"))
                .andExpect(jsonPath("$.data.name").value("davood"));
    }

    @Test
    void should_return_original_body_when_status_is_not_200() throws Exception {

        mockMvc.perform(get("/test/error")
                        .accept(MediaType.APPLICATION_JSON.toString()))
                .andExpect(status().isBadRequest())
                // Response body must not be wrapped
                .andExpect(jsonPath("$.error").value("bad request"))
                // StandardResponse fields must not exist
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.correlationId").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
