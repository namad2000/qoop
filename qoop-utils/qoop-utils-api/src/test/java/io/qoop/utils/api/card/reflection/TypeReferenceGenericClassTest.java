package io.qoop.utils.api.card.reflection;

import io.qoop.utils.api.reflection.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeReferenceGenericClassTest {

    static class SingleGeneric<T> implements TypeReference {
    }

    static class SingleConcrete extends SingleGeneric<String> {
    }

    static class DoubleGeneric<A, B> implements TypeReference {
    }

    static class DoubleConcrete extends DoubleGeneric<String, Integer> {
    }

    static class NestedGeneric<T> implements TypeReference {
    }

    static class NestedConcrete extends NestedGeneric<List<Long>> {
    }

    // ------------------------
    // SUCCESS
    // ------------------------

    @Test
    void shouldReturnConcreteGenericClass() {
        TypeReference ref = new SingleConcrete();

        Class<String> clazz = ref.genericClass(0);

        assertEquals(String.class, clazz);
    }

    @Test
    void shouldReturnFirstGenericClassFromMultiple() {
        TypeReference ref = new DoubleConcrete();

        Class<String> clazz = ref.genericClass(0);

        assertEquals(String.class, clazz);
    }

    @Test
    void shouldReturnSecondGenericClassFromMultiple() {
        TypeReference ref = new DoubleConcrete();

        Class<Integer> clazz = ref.genericClass(1);

        assertEquals(Integer.class, clazz);
    }

    @Test
    void shouldReturnRawClassForNestedGeneric() {
        TypeReference ref = new NestedConcrete();

        Class<List> clazz = ref.genericClass(0);

        assertEquals(List.class, clazz);
    }

    // ------------------------
    // FAILURE
    // ------------------------

    @Test
    void shouldThrowExceptionWhenGenericInfoIsMissing() {
        TypeReference ref = new SingleGeneric<>();

        assertThrows(
                IllegalStateException.class,
                () -> ref.genericClass(0)
        );
    }

    @Test
    void shouldThrowExceptionWhenIndexOutOfBounds() {
        TypeReference ref = new DoubleConcrete();

        assertThrows(
                IllegalArgumentException.class,
                () -> ref.genericClass(2)
        );
    }
}
