package io.qoop.mapper.core.shift;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.Shift;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShiftRealTest {

    @BeforeAll
    static void setup() {
        // Real Jackson Engine
        JsonEngine engine = new JacksonJsonEngine("yyyy-MM-dd HH:mm:ss");
        Shift.setup(engine);
    }

    // ---------- just(Object) ----------
    @Test
    @Order(1)
    void just_real_object() {
        UserEntity user = new UserEntity();
        user.id = 1L;
        user.name = "Davood";
        user.createdAt = LocalDateTime.now();

        Shift<UserEntity> shift = Shift.just(user);

        assertNotNull(shift.toBytes());
        assertNotNull(shift.toJson());
        assertTrue(shift.toJson().contains("Davood"));
    }

    // ---------- toObject ----------
    @Test
    @Order(2)
    void toObject_real() {
        UserEntity user = new UserEntity();
        user.id = 2L;
        user.name = "Ali";

        Shift<UserEntity> shift = Shift.just(user);
        UserEntity result = shift.toObject();

        assertEquals("Ali", result.name);
        assertEquals(2L, result.id);
    }

    // ---------- toObject(Class) ----------
    @Test
    @Order(3)
    void toObject_different_class() {
        UserEntity user = new UserEntity();
        user.id = 3L;
        user.name = "Sara";

        Shift<UserEntity> shift = Shift.just(user);
        UserDTO dto = shift.toObject(UserDTO.class);

        assertEquals("Sara", dto.name);
        assertEquals(3L, dto.id);
    }

    // ---------- just(String) ----------
    @Test
    @Order(4)
    void just_json_string() {
        String json = "{\"id\":5,\"name\":\"Test\"}";
        Shift<String> shift = Shift.just(json);

        assertEquals(json, shift.toJson());
        UserDTO dto = shift.toObject(UserDTO.class);
        assertEquals("Test", dto.name);
    }

    // ---------- map ----------
    @Test
    @Order(5)
    void map_real_transformation() {
        UserEntity user = new UserEntity();
        user.id = 10L;
        user.name = "MapUser";

        Shift<UserEntity> shift = Shift.just(user);

        Shift<UserDTO> mapped = shift.map(u -> {
            UserDTO dto = new UserDTO();
            dto.id = u.id;
            dto.name = u.name.toUpperCase();
            return dto;
        });

        UserDTO dto = mapped.toObject();
        assertEquals("MAPUSER", dto.name);
    }

    // ---------- subscribe ----------
    @Test
    @Order(6)
    void subscribe_real() {
        UserEntity user = new UserEntity();
        user.name = "Subscribe";

        Shift<UserEntity> shift = Shift.just(user);

        AtomicBoolean called = new AtomicBoolean(false);

        shift.subscribe(u -> {
            assertEquals("Subscribe", u.name);
            called.set(true);
        });

        assertTrue(called.get());
    }

    // ---------- toShift ----------
    @Test
    @Order(7)
    void toShift_real() {
        UserEntity user = new UserEntity();
        user.id = 99L;
        user.name = "ShiftType";

        Shift<UserEntity> shift = Shift.just(user);
        Shift<UserDTO> dtoShift = shift.toShift(UserDTO.class);

        UserDTO dto = dtoShift.toObject();
        assertEquals("ShiftType", dto.name);
    }

    // ---------- null branches for 100% coverage ----------
    @Test
    @Order(8)
    void null_branches() {
        Shift<Object> shiftNull = Shift.just((Object) null);
        assertNull(shiftNull.toObject());
        assertNull(shiftNull.toJson());
        assertNull(shiftNull.toBytes());

        Shift<String> shiftJsonNull = Shift.just((String) null);
        assertNull(shiftJsonNull.toObject());
    }
}
