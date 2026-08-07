package io.qoop.mapper.api.shift;

import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

@AllArgsConstructor
public class ShiftMapper<T, R> implements ShiftExtractable<R> {

    private final Shift<T> sourceShift;
    private final Class<R> targetClass;

    // --- Implementation of ShiftExtractable ---

    @Override
    public byte[] toBytes() {
        return sourceShift.toBytes();
    }

    @Override
    public Class<R> getTargetClass() {
        return targetClass;
    }

    @Override
    public JsonEngine getEngine() {
        return Shift.engine;
    }

    // --- Single Object Mapping ---

    public Optional<R> map(BiConsumer<T, R> customizer) {
        T source = sourceShift.toObject();
        if (source == null || targetClass == null) {
            return Optional.empty();
        }
        R target = sourceShift.toObject(targetClass);
        if (customizer != null) {
            customizer.accept(source, target);
        }

        return Optional.ofNullable(target);
    }

    public Optional<R> map(Function<T, R> function) {
        T source = sourceShift.toObject();
        if (source == null || function == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(function.apply(source));
    }

    public Optional<R> map() {
        return map((BiConsumer<T, R>) null);
    }

    // --- Collection Mapping ---

    public <S> Stream<R> mapCollection() {
        return mapCollection(null);
    }

    public <S> Stream<R> mapCollection(BiConsumer<S, R> customizer) {
        Collection<?> rawSources = (Collection<?>) sourceShift.toObject();

        if (rawSources == null || rawSources.isEmpty()) {
            return Stream.empty();
        }

        return getTargetStream(customizer, rawSources);
    }

    private <S> Stream<R> getTargetStream(BiConsumer<S, R> customizer, Collection<?> rawSources) {
        @SuppressWarnings("unchecked")
        Class<S> effectiveSourceClass = (Class<S>) sourceShift.getElementClass();

        return rawSources.stream().map(rawSrc -> {
            if (rawSrc == null) {
                return null;
            }

            R target = getEngine().convert(rawSrc, targetClass);

            if (customizer != null) {
                S sourceObj;
                if (effectiveSourceClass != null && !effectiveSourceClass.isInstance(rawSrc)) {
                    sourceObj = getEngine().convert(rawSrc, effectiveSourceClass);
                } else {
                    @SuppressWarnings("unchecked")
                    S casted = (S) rawSrc;
                    sourceObj = casted;
                }
                customizer.accept(sourceObj, target);
            }

            return target;
        });
    }
}