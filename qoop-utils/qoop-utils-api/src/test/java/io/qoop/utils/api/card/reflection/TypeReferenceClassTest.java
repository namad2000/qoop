package io.qoop.utils.api.card.reflection;

import io.qoop.utils.api.reflection.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeReferenceClassTest {

    static class SingleGeneric<T> implements TypeReference {
    }

    static class SingleConcrete extends SingleGeneric<String> {
    }

    static class MultiGeneric<A, B> implements TypeReference {
    }

    static class MultiConcrete extends MultiGeneric<String, Integer> {
    }

    static class NestedGeneric<A> implements TypeReference {
    }

    static class NestedConcrete extends NestedGeneric<List<Long>> {
    }

    // ------------------------
    // SUCCESS CASES
    // ------------------------

    @Test
    void shouldReturnClassForSingleGeneric() {
        TypeReference ref = new SingleConcrete();

        Class<?> clazz = ref.genericClass(0);

        assertEquals(String.class, clazz);
    }

    @Test
    void shouldReturnClassForMultiGenericFirstParameter() {
        TypeReference ref = new MultiConcrete();

        Class<?> clazz = ref.genericClass(0);

        assertEquals(String.class, clazz);
    }

    @Test
    void shouldReturnClassForMultiGenericSecondParameter() {
        TypeReference ref = new MultiConcrete();

        Class<?> clazz = ref.genericClass(1);

        assertEquals(Integer.class, clazz);
    }

    @Test
    void shouldReturnRawClassForNestedGeneric() {
        TypeReference ref = new NestedConcrete();

        Class<?> clazz = ref.genericClass(0);

        assertEquals(List.class, clazz);
    }

    // ------------------------
    // FAILURE CASES
    // ------------------------

    @Test
    void shouldThrowExceptionWhenGenericInfoMissing() {
        TypeReference ref = new SingleGeneric<>();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> ref.genericClass(0)
        );

        assertTrue(ex.getMessage().contains("Generic type information is not available"));
    }

    @Test
    void shouldThrowExceptionWhenIndexOutOfBounds() {
        TypeReference ref = new MultiConcrete();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ref.genericClass(2)
        );

        assertTrue(ex.getMessage().contains("index out of bounds"));
    }

    @Test
    void shouldThrowExceptionForUnsupportedGenericType() {
        TypeReference ref = new TypeReference() {
        };

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> ref.genericClass(0)
        );

        assertNotNull(ex.getMessage());
    }
}
