package io.qoop.utils.api.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface TypeReference {

    @SuppressWarnings("unchecked")
    default <T> Class<T> genericClass(int index) {
        Type type = genericType(index);
        return (Class<T>) toClass(type);
    }

    default Type genericType(int index) {
        Type superClass = getClass().getGenericSuperclass();

        if (!(superClass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException(
                    "Generic type information is not available. " +
                            "Ensure the class directly extends a parameterized superclass."
            );
        }

        Type[] typeArguments = parameterizedType.getActualTypeArguments();

        if (index < 0 || index >= typeArguments.length) {
            throw new IllegalArgumentException(
                    "Generic type index out of bounds. Index: " + index +
                            ", Available: " + typeArguments.length
            );
        }

        return typeArguments[index];
    }

    /**
     * Converts Type to raw Class.
     */
    default Class<?> toClass(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }

        if (type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        }

        throw new IllegalArgumentException(
                "Unsupported generic type: " + type.getTypeName()
        );
    }
}
