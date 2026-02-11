package io.qoop.utils.api.card.reflection;

import io.qoop.utils.api.reflection.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeReferenceMultiGenericTest {

    static class BiGenericRef<K, V> implements TypeReference {
    }

    static class ConcreteBiGenericRef extends BiGenericRef<String, Integer> {
    }

    static class TripleGenericRef<A, B, C> implements TypeReference {
    }

    static class ConcreteTripleGenericRef extends TripleGenericRef<String, Integer, List<Long>> {
    }

    // ------------------------
    // SUCCESS CASES
    // ------------------------

    @Test
    void shouldReturnFirstGenericType() {
        TypeReference ref = new ConcreteBiGenericRef();

        Type type = ref.genericType(0);

        assertEquals(String.class, type);
    }

    @Test
    void shouldReturnSecondGenericType() {
        TypeReference ref = new ConcreteBiGenericRef();

        Type type = ref.genericType(1);

        assertEquals(Integer.class, type);
    }

    @Test
    void shouldReturnThirdGenericType() {
        TypeReference ref = new ConcreteTripleGenericRef();

        Type type = ref.genericType(2);

        assertTrue(type instanceof java.lang.reflect.ParameterizedType);

        assertEquals(
                List.class,
                ((java.lang.reflect.ParameterizedType) type).getRawType()
        );
    }

    // ------------------------
    // FAILURE CASES
    // ------------------------

    @Test
    void shouldThrowException_whenIndexIsNegative() {
        TypeReference ref = new ConcreteBiGenericRef();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ref.genericType(-1)
        );

        assertTrue(ex.getMessage().contains("index out of bounds"));
    }

    @Test
    void shouldThrowException_whenIndexExceedsGenericCount() {
        TypeReference ref = new ConcreteBiGenericRef();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ref.genericType(2)
        );

        assertTrue(ex.getMessage().contains("index out of bounds"));
    }

    @Test
    void shouldThrowException_whenGenericTypeInformationIsMissing() {
        TypeReference ref = new BiGenericRef<>();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> ref.genericType(0)
        );

        assertTrue(ex.getMessage().contains("Generic type information is not available"));
    }
}
