package io.qoop.mapper.api.shift;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ShiftTest {

    private static JsonEngine engine;

    @BeforeAll
    static void init() {
        engine = mock(JsonEngine.class);

        when(engine.serialize(any()))
                .thenReturn("{\"mock\":true}".getBytes(StandardCharsets.UTF_8));

        when(engine.deserialize(any(byte[].class), eq(String.class)))
                .thenAnswer(inv -> new String(inv.getArgument(0, byte[].class)));

        when(engine.deserialize(any(byte[].class), eq(Dummy.class)))
                .thenReturn(new Dummy("john"));

        Shift.setup(engine);
    }

    @BeforeEach
    void resetMock() {
        reset(engine);
        when(engine.serialize(any()))
                .thenReturn("{\"mock\":true}".getBytes());
        when(engine.deserialize(any(byte[].class), eq(String.class)))
                .thenReturn("hello");
        when(engine.deserialize(any(byte[].class), eq(Dummy.class)))
                .thenReturn(new Dummy("john"));
    }

    @Test
    void just_object_normal() {
        Shift<Dummy> shift = Shift.just(new Dummy("x"));

        assertNotNull(shift.toBytes());
        verify(engine, times(1)).serialize(any()); // حالا درست است
    }

    // ---------- just(Object) ----------
    @Test
    void just_object_null() {
        Shift<Object> shift = Shift.just((Object) null);

        assertNull(shift.toObject());
        assertNull(shift.toJson());
        assertNull(shift.toBytes());
    }

    // ---------- just(String) ----------
    @Test
    void just_string_default() {
        Shift<String> shift = Shift.just("hello");

        assertEquals("hello", shift.toJson());
    }

    @Test
    void just_string_charset() {
        Shift<String> shift = Shift.just("سلام", StandardCharsets.UTF_8);

        assertEquals("سلام", shift.toJson());
    }

    // ---------- toObject ----------
    @Test
    void toObject_null_bytes() {
        Shift<String> shift = Shift.just((String) null);

        assertNull(shift.toObject());
    }

    @Test
    void toObject_with_class() {
        Shift<String> shift = Shift.just("hello");

        String result = shift.toObject(String.class);

        assertEquals("hello", result);
    }

    // ---------- toJson ----------
    @Test
    void toJson_null() {
        Shift<String> shift = Shift.just((String) null);

        assertNull(shift.toJson());
    }

    // ---------- toBytes ----------
    @Test
    void toBytes_normal() {
        Shift<String> shift = Shift.just("hello");

        assertNotNull(shift.toBytes());
    }

    // ---------- map ----------
    @Test
    void map_function() {
        Shift<String> shift = Shift.just("hello");

        Shift<Integer> mapped = shift.map(String::length);

        assertNotNull(mapped);
        verify(engine, atLeastOnce()).serialize(any());
    }

    // ---------- subscribe ----------
    @Test
    void subscribe_called() {
        Shift<Dummy> shift = Shift.just(new Dummy("x"));

        AtomicBoolean called = new AtomicBoolean(false);

        shift.subscribe(d -> called.set(true));

        assertTrue(called.get());
    }

    @Test
    void subscribe_not_called_when_null() {
        Shift<Dummy> shift = Shift.just((Dummy) null);

        AtomicBoolean called = new AtomicBoolean(false);

        shift.subscribe(d -> called.set(true));

        assertFalse(called.get());
    }

    // ---------- toShift ----------
    @Test
    void toShift_change_type() {
        Shift<String> shift = Shift.just("hello");

        Shift<Dummy> newShift = shift.toShift(Dummy.class);

        assertNotNull(newShift);
        assertNotNull(newShift.toBytes());
    }
}
