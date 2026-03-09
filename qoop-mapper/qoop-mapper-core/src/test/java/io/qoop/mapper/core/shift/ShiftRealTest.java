package io.qoop.mapper.core.shift;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.Shift;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    // ---------- toList ----------
    @Test
    @Order(8)
    void toList_real_conversion() {
        List<UserEntity> users = new ArrayList<>();
        users.add(createUser(1L, "Ali"));
        users.add(createUser(2L, "Reza"));

        Shift<List<UserEntity>> shift = Shift.just(users);
        List<UserDTO> dtos = shift.toList(UserDTO.class);

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Ali", dtos.get(0).name);
        assertEquals("Reza", dtos.get(1).name);
    }

    @Test
    @Order(9)
    void toList_real_empty() {
        List<UserEntity> users = new ArrayList<>();
        Shift<List<UserEntity>> shift = Shift.just(users);
        List<UserDTO> dtos = shift.toList(UserDTO.class);

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    // ---------- mapList ----------
    @Test
    @Order(10)
    void mapList_real_with_logic() {
        List<UserEntity> users = new ArrayList<>();
        users.add(createUser(1L, "ali"));
        users.add(createUser(2L, "reza"));

        Shift<List<UserEntity>> shift = Shift.just(users);

        List<UserDTO> result = shift.mapList(UserDTO.class, dto -> {
            // Logic: Capitalize the first letter
            dto.name = dto.name.substring(0, 1).toUpperCase() + dto.name.substring(1);
            return dto;
        });

        assertNotNull(result);
        assertEquals("Ali", result.get(0).name);
        assertEquals("Reza", result.get(1).name);
    }

    // Helper method to create test data
    private UserEntity createUser(Long id, String name) {
        UserEntity u = new UserEntity();
        u.id = id;
        u.name = name;
        u.createdAt = LocalDateTime.now();
        return u;
    }

    // ---------- toSet ----------
    @Test
    @Order(12)
    void toSet_real_conversion() {
        List<UserEntity> users = new ArrayList<>();
        users.add(createUser(1L, "Ali"));
        users.add(createUser(2L, "Reza"));

        Shift<List<UserEntity>> shift = Shift.just(users);
        Set<UserDTO> dtos = shift.toSet(UserDTO.class);

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        // Check existence since Set order is not guaranteed
        assertTrue(dtos.stream().anyMatch(d -> d.name.equals("Ali")));
        assertTrue(dtos.stream().anyMatch(d -> d.name.equals("Reza")));
    }

    @Test
    @Order(13)
    void toSet_real_empty() {
        List<UserEntity> users = new ArrayList<>();
        Shift<List<UserEntity>> shift = Shift.just(users);
        Set<UserDTO> dtos = shift.toSet(UserDTO.class);

        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    // ---------- mapSet ----------
    @Test
    @Order(14)
    void mapSet_real_with_logic() {
        List<UserEntity> users = new ArrayList<>();
        users.add(createUser(1L, "ali"));
        users.add(createUser(2L, "reza"));

        Shift<List<UserEntity>> shift = Shift.just(users);

        Set<UserDTO> result = shift.mapSet(UserDTO.class, dto -> {
            // Logic: Capitalize the first letter
            dto.name = dto.name.substring(0, 1).toUpperCase() + dto.name.substring(1);
            return dto;
        });

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.name.equals("Ali")));
        assertTrue(result.stream().anyMatch(d -> d.name.equals("Reza")));
    }
}
