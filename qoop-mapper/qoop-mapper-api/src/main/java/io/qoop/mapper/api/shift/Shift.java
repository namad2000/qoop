package io.qoop.mapper.api.shift;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Shift<T> implements ShiftExtractable<T> {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static volatile JsonEngine engine;

    private final Charset charset;
    private final byte[] dataBytes;
    private final Class<T> targetClass;

    @Getter
    private final Class<?> elementClass;

    public static void setup(JsonEngine jsonEngine) {
        engine = jsonEngine;
    }

    // --- Implementation of ShiftExtractable ---

    @Override
    public Class<T> getTargetClass() {
        return targetClass;
    }

    @Override
    public JsonEngine getEngine() {
        return engine;
    }

    // --- Static Factories ---

    @SuppressWarnings("unchecked")
    public static <R> Shift<R> just(R target) {
        if (target == null) {
            return new Shift<>(DEFAULT_CHARSET, null, null, null);
        }

        Class<?> elemClass = null;
        if (target instanceof Collection<?> coll) {
            for (Object item : coll) {
                if (item != null) {
                    elemClass = item.getClass();
                    break;
                }
            }
        }

        return new Shift<>(DEFAULT_CHARSET, engine.serialize(target), (Class<R>) target.getClass(), elemClass);
    }

    public static Shift<String> just(String json) {
        byte[] bytes = (json == null) ? null : json.getBytes(DEFAULT_CHARSET);
        return new Shift<>(DEFAULT_CHARSET, bytes, String.class, null);
    }

    // --- Conversion & Utility Methods ---

    public String toJson() {
        return dataBytes == null ? null : new String(dataBytes, charset);
    }

    @Override
    public byte[] toBytes() {
        return dataBytes;
    }

    public void subscribe(Consumer<T> consumer) {
        T obj = toObject();
        if (obj != null) consumer.accept(obj);
    }

    public <R> ShiftMapper<T, R> toShift(Class<R> targetClass) {
        return new ShiftMapper<>(this, targetClass);
    }
}