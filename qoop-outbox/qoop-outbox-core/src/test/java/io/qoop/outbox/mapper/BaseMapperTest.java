package io.qoop.outbox.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.OutBoxEvent;
import io.qoop.stream.api.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseMapperTest {

    private static class TestBaseMapper implements BaseMapper {
    }

    private TestBaseMapper baseMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OutBoxEvent metadata;

    @BeforeEach
    void setUp() {
        baseMapper = new TestBaseMapper();
    }

    @Nested
    @DisplayName("Payload Serialization Tests")
    class SerializePayloadTests {

        @Test
        @DisplayName("Should return empty JSON object '{}' when payload is null")
        void shouldReturnEmptyJsonObjectWhenPayloadIsNull() {
            String result = baseMapper.serializePayload(null, objectMapper);
            assertThat(result).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should return string as-is if it is already valid JSON (prevents double serialization)")
        void shouldReturnAlreadyJsonStringAsIs() {
            String jsonObject = "{\"id\":100,\"name\":\"test\"}";
            String jsonArray = "[{\"key\":\"val\"}]";

            String resultObject = baseMapper.serializePayload(jsonObject, objectMapper);
            String resultArray = baseMapper.serializePayload(jsonArray, objectMapper);

            assertThat(resultObject).isEqualTo(jsonObject);
            assertThat(resultArray).isEqualTo(jsonArray);
        }

        @Test
        @DisplayName("Should serialize Java object to valid JSON string")
        void shouldSerializeJavaObjectToJson() {
            TestPayload payload = new TestPayload("123", "EVENT_TYPE");

            String result = baseMapper.serializePayload(payload, objectMapper);

            assertThat(result).contains("\"id\":\"123\"");
            assertThat(result).contains("\"type\":\"EVENT_TYPE\"");
        }

        @Test
        @DisplayName("Should return fallback '{}' on serialization exception to prevent database constraints violation")
        void shouldReturnEmptyJsonOnSerializationException() throws Exception {
            Object invalidPayload = new Object();
            doThrow(new JsonProcessingException("Serialization failed") {
            })
                    .when(objectMapper).writeValueAsString(any());

            String result = baseMapper.serializePayload(invalidPayload, objectMapper);

            assertThat(result).isEqualTo("{}");
        }
    }

    @Nested
    @DisplayName("Headers Serialization Tests")
    class SerializeHeadersTests {

        @Test
        @DisplayName("Should return empty JSON object '{}' when headers are empty")
        void shouldReturnEmptyJsonObjectWhenHeadersAreEmpty() {
            when(metadata.headers()).thenReturn(new io.qoop.stream.api.annotaions.Header[0]);

            String result = baseMapper.serializeHeaders(metadata, List.of(), objectMapper);

            assertThat(result).isEqualTo("[]");
        }

        @Test
        @DisplayName("Should return empty JSON object '{}' when all input headers are null")
        void shouldReturnEmptyJsonObjectWhenAllInputsAreNull() {
            String result = baseMapper.serializeHeaders(null, null, objectMapper);

            assertThat(result).isEqualTo("[]");
        }

        @Test
        @DisplayName("Should combine metadata and explicit headers into valid JSON array")
        void shouldCombineAndSerializeHeaders() {
            io.qoop.stream.api.annotaions.Header metaHeader = createAnnotationHeader("X-Source", "Metadata");
            Header explicitHeader = new Header("X-Trace-Id", "12345");

            when(metadata.headers()).thenReturn(new io.qoop.stream.api.annotaions.Header[]{metaHeader});

            String result = baseMapper.serializeHeaders(metadata, List.of(explicitHeader), objectMapper);

            assertThat(result).contains("X-Source");
            assertThat(result).contains("Metadata");
            assertThat(result).contains("X-Trace-Id");
            assertThat(result).contains("12345");
        }

        @Test
        @DisplayName("Should return fallback '{}' on serialization exception")
        void shouldReturnEmptyJsonObjectOnException() throws Exception {
            Header explicitHeader = new Header("Key", "Value");
            doThrow(new JsonProcessingException("Failed") {
            })
                    .when(objectMapper).writeValueAsString(any());

            String result = baseMapper.serializeHeaders(null, List.of(explicitHeader), objectMapper);

            assertThat(result).isEqualTo("[]");
        }
    }

    private static io.qoop.stream.api.annotaions.Header createAnnotationHeader(String name, String value) {
        return new io.qoop.stream.api.annotaions.Header() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return io.qoop.stream.api.annotaions.Header.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    private record TestPayload(String id, String type) {
    }
}