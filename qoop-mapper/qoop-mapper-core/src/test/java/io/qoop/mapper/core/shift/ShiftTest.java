package io.qoop.mapper.core.shift;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.Shift;
import io.qoop.mapper.api.shift.ShiftMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ShiftTest {

    private JsonEngine jsonEngine;

    @BeforeEach
    void setUp() {
        jsonEngine = new JacksonJsonEngine("yyyy-MM-dd HH:mm:ss");
        Shift.setup(jsonEngine);
    }

    @AfterEach
    void tearDown() {
        Shift.setup(null);
    }

    @Test
    @DisplayName("setup should set static JsonEngine")
    void setup_ShouldSetEngine() {
        assertEquals(jsonEngine, Shift.engine);
    }

    @Test
    @DisplayName("just(Object) with null should create empty Shift")
    void justObject_WithNull_ShouldReturnEmptyShift() {
        Shift<SampleDto> shift = Shift.just((SampleDto) null);

        assertNotNull(shift);
        assertNull(shift.toBytes());
        assertNull(shift.getTargetClass());
        assertNull(shift.toJson());
        assertEquals(jsonEngine, shift.getEngine());
    }

    @Test
    @DisplayName("just(Object) with valid object should serialize correctly")
    void justObject_WithValidObject_ShouldSerialize() {
        SampleDto dto = new SampleDto("John", 30);

        Shift<SampleDto> shift = Shift.just(dto);

        assertNotNull(shift);
        assertNotNull(shift.toBytes());
        assertEquals(SampleDto.class, shift.getTargetClass());
        assertTrue(shift.toJson().contains("\"name\":\"John\""));
    }

    @Test
    @DisplayName("just(String) with null should return null bytes")
    void justString_WithNull_ShouldReturnNullBytes() {
        Shift<String> shift = Shift.just((String) null);

        assertNotNull(shift);
        assertNull(shift.toBytes());
        assertEquals(String.class, shift.getTargetClass());
        assertNull(shift.toJson());
    }

    @Test
    @DisplayName("just(String) with valid json should store bytes")
    void justString_WithValidJson_ShouldSetBytes() {
        String json = "{\"name\":\"John\",\"age\":30}";
        Shift<String> shift = Shift.just(json);

        assertNotNull(shift);
        assertArrayEquals(json.getBytes(StandardCharsets.UTF_8), shift.toBytes());
        assertEquals(String.class, shift.getTargetClass());
        assertEquals(json, shift.toJson());
    }

    @Test
    @DisplayName("subscribe when object exists should invoke consumer")
    void subscribe_WhenObjectExists_ShouldCallConsumer() {
        SampleDto dto = new SampleDto("Alice", 25);
        Shift<SampleDto> shift = Shift.just(dto);

        AtomicBoolean consumed = new AtomicBoolean(false);
        shift.subscribe(item -> {
            consumed.set(true);
            assertEquals("Alice", item.name());
        });

        assertTrue(consumed.get());
    }

    @Test
    @DisplayName("subscribe when object is null should not invoke consumer")
    void subscribe_WhenObjectIsNull_ShouldNotCallConsumer() {
        Shift<SampleDto> shift = Shift.just((SampleDto) null);

        AtomicBoolean consumed = new AtomicBoolean(false);
        shift.subscribe(item -> consumed.set(true));

        assertFalse(consumed.get());
    }

    @Test
    @DisplayName("toShift should return configured ShiftMapper instance")
    void toShift_ShouldReturnShiftMapper() {
        Shift<SampleDto> shift = Shift.just(new SampleDto("Bob", 40));
        ShiftMapper<SampleDto, TargetDto> mapper = shift.toShift(TargetDto.class);

        assertNotNull(mapper);
        assertEquals(TargetDto.class, mapper.getTargetClass());
    }

    record SampleDto(String name, int age) {
    }

    record TargetDto(String name, int age) {
    }
}