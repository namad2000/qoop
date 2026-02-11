package io.qoop.mapper.core.shift;

import org.junit.jupiter.api.BeforeEach;
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
    void shouldSerializeAndDeserializeCorrectly() {
        TestModel model = new TestModel("Gemini", 2026);

        byte[] bytes = engine.serialize(model);
        assertNotNull(bytes);

        TestModel result = engine.deserialize(bytes, TestModel.class);
        assertEquals("Gemini", result.getName());
        assertEquals(2026, result.getYear());
    }

    static class TestModel {
        private String name;
        private int year;

        public TestModel() {
        } // Required for Jackson

        public TestModel(String name, int year) {
            this.name = name;
            this.year = year;
        }

        public String getName() {
            return name;
        }

        public int getYear() {
            return year;
        }
    }
}