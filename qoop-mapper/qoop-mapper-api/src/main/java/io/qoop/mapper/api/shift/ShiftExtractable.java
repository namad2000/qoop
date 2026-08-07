package io.qoop.mapper.api.shift;

import java.util.Collection;
import java.util.stream.Stream;

public interface ShiftExtractable<T> {

    byte[] toBytes();

    Class<T> getTargetClass();

    JsonEngine getEngine();

    // --- Object Extractions ---

    default T toObject() {
        return toObject(getTargetClass());
    }

    default <R> R toObject(Class<R> targetClass) {
        byte[] bytes = toBytes();
        if (bytes == null || targetClass == null || getEngine() == null) {
            return null;
        }
        return getEngine().deserialize(bytes, targetClass);
    }

    // --- Collection Extractions ---

    default Stream<T> toCollection() {
        return toCollection(getTargetClass());
    }

    default <R> Stream<R> toCollection(Class<R> targetClass) {
        return extractCollection(targetClass);
    }

    // --- Internal Helper ---

    private <R> Stream<R> extractCollection(Class<R> targetClass) {
        byte[] bytes = toBytes();
        if (bytes == null || targetClass == null || getEngine() == null) {
            return Stream.<R>empty();
        }
        Object raw = getEngine().deserialize(bytes, Object.class);
        if (!(raw instanceof Collection<?> collection)) {
            return Stream.<R>empty();
        }

        return collection.stream()
                .map(item -> {
                    if (item == null) {
                        return null;
                    }

                    return getEngine().convert(item, targetClass);
                });
    }
}