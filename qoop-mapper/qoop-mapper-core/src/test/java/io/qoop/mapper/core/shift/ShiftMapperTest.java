package io.qoop.mapper.core.shift;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.Shift;
import io.qoop.mapper.api.shift.ShiftMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShiftMapperTest {

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
    @DisplayName("ShiftMapper delegated methods should return correct values")
    void delegatedMethods_ShouldReturnSourceValues() {
        SourceDto source = new SourceDto("Data");
        Shift<SourceDto> shift = Shift.just(source);
        ShiftMapper<SourceDto, TargetDto> mapper = shift.toShift(TargetDto.class);

        assertArrayEquals(shift.toBytes(), mapper.toBytes());
        assertEquals(TargetDto.class, mapper.getTargetClass());
        assertEquals(jsonEngine, mapper.getEngine());
    }

    @Test
    @DisplayName("map() without customizer should deserialize directly")
    void map_WithoutCustomizer_ShouldReturnTarget() {
        SourceDto source = new SourceDto("Title");

        Optional<TargetDto> result = Shift
                .just(source)
                .toShift(TargetDto.class)
                .map();

        assertTrue(result.isPresent());
        assertEquals("Title", result.get().getTitle());
    }

    @Test
    @DisplayName("map(BiConsumer) with null source or null targetClass should return empty Optional")
    void map_WithNullSourceOrClass_ShouldReturnEmpty() {
        ShiftMapper<SourceDto, TargetDto> mapper1 = Shift.just((SourceDto) null).toShift(TargetDto.class);
        ;
        assertTrue(mapper1.map((src, tgt) -> {
        }).isEmpty());

        ShiftMapper<SourceDto, TargetDto> mapper2 = new ShiftMapper<>(Shift.just(new SourceDto("A")), null);
        assertTrue(mapper2.map((src, tgt) -> {
        }).isEmpty());
    }

    @Test
    @DisplayName("map(Function) should process custom function or return empty on nulls")
    void map_WithFunction_ShouldApplyOrReturnEmpty() {
        ShiftMapper<SourceDto, TargetDto> mapper = Shift.just(new SourceDto("Java")).toShift(TargetDto.class);

        // Valid function
        Optional<TargetDto> result = mapper.map(src -> new TargetDto(src.getTitle() + " 21", 100));
        assertTrue(result.isPresent());
        assertEquals("Java 21", result.get().getTitle());

        // Null function
        assertTrue(mapper.map((java.util.function.Function<SourceDto, TargetDto>) null).isEmpty());

        // Function returning null
        assertTrue(mapper.map(src -> null).isEmpty());

        // Null source
        ShiftMapper<SourceDto, TargetDto> emptyMapper = new ShiftMapper<>(Shift.just((SourceDto) null), TargetDto.class);
        assertTrue(emptyMapper.map(src -> new TargetDto("A", 1)).isEmpty());
    }

    @Test
    @DisplayName("mapCollection() without customizer should transform list")
    void mapCollection_NoCustomizer_ShouldTransform() {
        List<SourceDto> list = List.of(new SourceDto("A"), new SourceDto("B"));
        ShiftMapper<List<SourceDto>, TargetDto> mapper = Shift.just(list).toShift(TargetDto.class);

        List<TargetDto> results = mapper.mapCollection().toList();

        assertEquals(2, results.size());
        assertEquals("A", results.get(0).getTitle());
        assertEquals("B", results.get(1).getTitle());
    }

    @Test
    @DisplayName("mapCollection should return empty stream when sources list is empty")
    void mapCollection_EmptyList_ShouldReturnEmptyStream() {
        List<SourceDto> emptyList = List.of();
        ShiftMapper<List<SourceDto>, TargetDto> mapper = Shift.just(emptyList).toShift(TargetDto.class);

        assertEquals(0, mapper.mapCollection().count());
    }

    @Test
    @DisplayName("mapCollection containing null item in collection should handle null item branch")
    void mapCollection_WithNullItem_ShouldMapNull() {
        List<SourceDto> listWithNull = new ArrayList<>();
        listWithNull.add(new SourceDto("A"));
        listWithNull.add(null);

        ShiftMapper<List<SourceDto>, TargetDto> mapper = Shift.just(listWithNull).toShift(TargetDto.class);
        List<TargetDto> results = mapper.<SourceDto>mapCollection((src, tgt) -> tgt.setScore(50)).toList();

        assertEquals(2, results.size());
        assertEquals("A", results.get(0).getTitle());
        assertEquals(50, results.get(0).getScore());
        assertNull(results.get(1));
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class SourceDto {
        private String title;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class TargetDto {
        private String title;
        private int score;
    }
}