package io.qoop.jpa.persistence.mapper;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InfrastructureMapperTest {

    // Concrete implementation for testing purposes
    // Assuming D = String (Domain) and E = Integer (Entity)
    static class TestMapper implements InfrastructureMapper<String, Integer> {
        @Override
        public String toDomain(Integer e) {
            if (e == null) return null;
            return "Domain-" + e;
        }

        @Override
        public Integer toEntity(String d) {
            if (d == null) return null;
            return Integer.parseInt(d.replace("Domain-", ""));
        }
    }

    @Test
    void testToDomain_whenOptionalIsPresent_shouldReturnMappedOptional() {
        // Arrange (Given)
        TestMapper mapper = new TestMapper();
        Optional<Integer> input = Optional.of(100);

        // Act (When)
        Optional<String> result = mapper.toDomain(input);

        // Assert (Then)
        assertTrue(result.isPresent(), "Result should not be empty");
        assertEquals("Domain-100", result.get());
    }

    @Test
    void testToDomain_whenOptionalIsEmpty_shouldReturnEmptyOptional() {
        // Arrange
        TestMapper mapper = new TestMapper();
        Optional<Integer> input = Optional.empty();

        // Act
        Optional<String> result = mapper.toDomain(input);

        // Assert
        assertTrue(result.isEmpty(), "Result should be empty");
    }

    @Test
    void testToEntity_whenOptionalIsPresent_shouldReturnMappedOptional() {
        // Arrange
        TestMapper mapper = new TestMapper();
        Optional<String> input = Optional.of("Domain-50");

        // Act
        Optional<Integer> result = mapper.toEntity(input);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(50, result.get());
    }

    @Test
    void testToEntity_whenOptionalIsEmpty_shouldReturnEmptyOptional() {
        // Arrange
        TestMapper mapper = new TestMapper();
        Optional<String> input = Optional.empty();

        // Act
        Optional<Integer> result = mapper.toEntity(input);

        // Assert
        assertFalse(result.isPresent());
    }
}