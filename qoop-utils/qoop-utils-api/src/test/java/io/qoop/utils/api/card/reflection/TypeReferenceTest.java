package io.qoop.utils.api.card.reflection;

import io.qoop.utils.api.reflection.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/16/2025 7:36 PM
 * Package: io.qoop.util.card.reflection
 */

class TypeReferenceTest {

    static class StringTypeRef implements TypeReference {
    }

    static class GenericTypeRef<T> implements TypeReference {
    }

    static class ConcreteTypeRef extends GenericTypeRef<String> {
    }

    @Test
    void shouldReturnGenericType_whenSubclassProvidesType() {
        TypeReference ref = new ConcreteTypeRef();

        Type type = ref.genericType(0);

        assertEquals(String.class, type);
    }

    @Test
    void shouldThrowException_whenNoGenericInformationExists() {
        TypeReference ref = new StringTypeRef();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ref.genericType(0)
        );

        assertTrue(exception.getMessage().contains("Generic type information is not available"));
    }

    @Test
    void shouldThrowException_whenIndexIsOutOfBounds() {
        TypeReference ref = new ConcreteTypeRef();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ref.genericType(1)
        );

        assertTrue(exception.getMessage().contains("index out of bounds"));
    }
}
