package io.qoop.mapper.api.mapper;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TargetMapperTest {

    record SourceDto(String id, String value) {}
    record TargetDto(String id, String value) {}

    static class SimpleTargetMapper implements TargetMapper<SourceDto, TargetDto> {
        @Override
        public TargetDto toTarget(SourceDto source) {
            return source != null ? new TargetDto(source.id(), source.value()) : null;
        }
    }

    private final TargetMapper<SourceDto, TargetDto> mapper = new SimpleTargetMapper();

    @Test
    void testToTarget_SingleObject() {
        SourceDto source = new SourceDto("1", "A");
        TargetDto result = mapper.toTarget(source);

        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("A", result.value());
    }

    @Test
    void testToTarget_OptionalPresent() {
        Optional<SourceDto> source = Optional.of(new SourceDto("1", "A"));
        Optional<TargetDto> result = mapper.toTarget(source);

        assertTrue(result.isPresent());
        assertEquals("1", result.get().id());
    }

    @Test
    void testToTarget_OptionalEmpty() {
        Optional<TargetDto> result = mapper.toTarget(Optional.empty());
        assertTrue(result.isEmpty());
    }

    @Test
    void testToTarget_List() {
        List<SourceDto> sourceList = List.of(new SourceDto("1", "A"), new SourceDto("2", "B"));
        List<TargetDto> result = mapper.toTarget(sourceList);

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).id());
        assertEquals("2", result.get(1).id());
    }

    @Test
    void testToTarget_Set() {
        Set<SourceDto> sourceSet = Set.of(new SourceDto("1", "A"));
        Set<TargetDto> result = mapper.toTarget(sourceSet);

        assertEquals(1, result.size());
        assertEquals("1", result.iterator().next().id());
    }
}