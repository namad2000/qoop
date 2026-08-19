package io.qoop.mapper.api.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainMapperTest {

    record SourceEntity(Long id, String name) {}
    record TargetDto(Long id, String name) {}

    static class UserDomainMapper implements DomainMapper<SourceEntity, TargetDto> {
        @Override
        public TargetDto toTarget(SourceEntity source) {
            return new TargetDto(source.id(), source.name());
        }

        @Override
        public SourceEntity toSource(TargetDto target) {
            return new SourceEntity(target.id(), target.name());
        }
    }

    private final DomainMapper<SourceEntity, TargetDto> mapper = new UserDomainMapper();

    @Test
    void testDomainMapper_BothDirections() {
        SourceEntity source = new SourceEntity(10L, "John");
        
        TargetDto target = mapper.toTarget(source);
        assertEquals(10L, target.id());
        assertEquals("John", target.name());

        SourceEntity restoredSource = mapper.toSource(target);
        assertEquals(source.id(), restoredSource.id());
        assertEquals(source.name(), restoredSource.name());
    }
}