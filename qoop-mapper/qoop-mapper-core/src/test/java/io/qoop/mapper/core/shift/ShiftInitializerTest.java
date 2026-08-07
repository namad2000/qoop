package io.qoop.mapper.core.shift;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.Shift;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShiftInitializerTest {

    @AfterEach
    void tearDown() {
        Shift.setup(null);
    }

    @Test
    @DisplayName("init() should set static JsonEngine in Shift class")
    void init_ShouldSetStaticJsonEngine() {
        JsonEngine engine = new JacksonJsonEngine("yyyy-MM-dd HH:mm:ss");
        ShiftInitializer initializer = new ShiftInitializer(engine);

        initializer.init();

        assertEquals(engine, Shift.engine);
    }
}