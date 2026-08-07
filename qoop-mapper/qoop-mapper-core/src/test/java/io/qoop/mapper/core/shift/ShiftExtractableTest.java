package io.qoop.mapper.core.shift;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.ShiftExtractable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShiftExtractableTest {

    private JsonEngine jsonEngine;

    @BeforeEach
    void setUp() {
        jsonEngine = new JacksonJsonEngine("yyyy-MM-dd HH:mm:ss");
    }

    @Test
    @DisplayName("toObject without args should use default target class")
    void toObject_NoArgs_ShouldUseDefaultClass() {
        TestExtractable<TargetDto> extractable = new TestExtractable<>(jsonEngine, TargetDto.class);
        extractable.setBytes("{\"title\":\"Test\"}".getBytes(StandardCharsets.UTF_8));

        TargetDto result = extractable.toObject();

        assertNotNull(result);
        assertEquals("Test", result.getTitle());
    }

    @Test
    @DisplayName("toObject should return null if bytes, targetClass, or engine is null")
    void toObject_EdgeCases_ShouldReturnNull() {
        // Bytes null
        TestExtractable<TargetDto> nullBytes = new TestExtractable<>(jsonEngine, TargetDto.class);
        assertNull(nullBytes.toObject(TargetDto.class));

        // TargetClass null
        TestExtractable<TargetDto> nullClass = new TestExtractable<>(jsonEngine, null);
        nullClass.setBytes("{}".getBytes());
        assertNull(nullClass.toObject(null));

        // Engine null
        TestExtractable<TargetDto> nullEngine = new TestExtractable<>(null, TargetDto.class);
        nullEngine.setBytes("{}".getBytes());
        assertNull(nullEngine.toObject(TargetDto.class));
    }

    @Test
    @DisplayName("toCollection without args should return stream of target class")
    void toCollection_NoArgs_ShouldReturnStream() {
        TestExtractable<TargetDto> extractable = new TestExtractable<>(jsonEngine, TargetDto.class);
        extractable.setBytes("[{\"title\":\"A\"}]".getBytes(StandardCharsets.UTF_8));

        List<TargetDto> list = extractable.toCollection().toList();

        assertEquals(1, list.size());
        assertEquals("A", list.getFirst().getTitle());
    }

    @Test
    @DisplayName("toCollection should return empty stream on null inputs or invalid types")
    void toCollection_EdgeCases_ShouldReturnEmptyStream() {
        // Bytes null
        TestExtractable<TargetDto> extractable = new TestExtractable<>(jsonEngine, TargetDto.class);
        assertEquals(0, extractable.toCollection(TargetDto.class).count());

        // TargetClass null
        extractable.setBytes("[]".getBytes());
        assertEquals(0, extractable.toCollection(null).count());

        // Engine null
        TestExtractable<TargetDto> noEngine = new TestExtractable<>(null, TargetDto.class);
        noEngine.setBytes("[]".getBytes());
        assertEquals(0, noEngine.toCollection(TargetDto.class).count());

        // Not a Collection JSON
        extractable.setBytes("\"Just a string\"".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, extractable.toCollection(TargetDto.class).count());
    }

    @Test
    @DisplayName("toCollection containing null element should handle null gracefully")
    void toCollection_WithNullItem_ShouldMapNull() {
        TestExtractable<TargetDto> extractable = new TestExtractable<>(jsonEngine, TargetDto.class);
        extractable.setBytes("[{\"title\":\"A\"}, null]".getBytes(StandardCharsets.UTF_8));

        List<TargetDto> list = extractable.toCollection(TargetDto.class).toList();

        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getTitle());
        assertNull(list.get(1));
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class TargetDto {
        private String title;
    }

    private static class TestExtractable<T> implements ShiftExtractable<T> {
        private final JsonEngine engine;
        private final Class<T> targetClass;
        @Setter
        private byte[] bytes;

        public TestExtractable(JsonEngine engine, Class<T> targetClass) {
            this.engine = engine;
            this.targetClass = targetClass;
        }

        @Override
        public byte[] toBytes() {
            return bytes;
        }

        @Override
        public Class<T> getTargetClass() {
            return targetClass;
        }

        @Override
        public JsonEngine getEngine() {
            return engine;
        }
    }
}