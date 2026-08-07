package io.qoop.mapper.core.shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JacksonJsonEngineTest {

    private JacksonJsonEngine engine;

    @BeforeEach
    void setUp() {
        engine = new JacksonJsonEngine("yyyy-MM-dd HH:mm:ss");
    }

    @Test
    @DisplayName("Should serialize object to byte array and deserialize back")
    void serializeAndDeserialize_ShouldWork() {
        TestModel model = new TestModel("Qoop", 2026);

        byte[] bytes = engine.serialize(model);
        assertNotNull(bytes);

        TestModel result = engine.deserialize(bytes, TestModel.class);
        assertEquals("Qoop", result.getName());
        assertEquals(2026, result.getYear());
    }

    @Test
    @DisplayName("Should ignore unknown properties during deserialization")
    void deserialize_WithUnknownProperties_ShouldNotFail() {
        String jsonWithExtraField = "{\"name\":\"Qoop\",\"year\":2026,\"extra\":\"ignored\"}";

        TestModel result = engine.deserialize(jsonWithExtraField.getBytes(), TestModel.class);

        assertNotNull(result);
        assertEquals("Qoop", result.getName());
        assertEquals(2026, result.getYear());
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class TestModel {
        private String name;
        private int year;
    }
}