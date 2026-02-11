package io.qoop.mapper.api.shift;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Shift<T> {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static JsonEngine engine;

    private final Charset charset;
    private final byte[] dataBytes;
    private final Class<T> tClass;

    /**
     * Initializes the static engine. Should be called once in the Infrastructure layer.
     */
    public static void setup(JsonEngine jsonEngine) {
        engine = jsonEngine;
    }

    // --- Static Factory Methods ---

    @SuppressWarnings("unchecked")
    public static <R> Shift<R> just(R target) {
        if (target == null) {
            return new Shift<>(DEFAULT_CHARSET, null, null);
        }
        byte[] bytes = engine.serialize(target);
        return new Shift<>(DEFAULT_CHARSET, bytes, (Class<R>) target.getClass());
    }

    public static Shift<String> just(String json) {
        return just(json, DEFAULT_CHARSET);
    }

    public static Shift<String> just(String json, Charset charset) {
        byte[] bytes = (json == null) ? null : json.getBytes(charset);
        return new Shift<>(charset, bytes, String.class);
    }

    // --- Transformation Methods ---

    public T toObject() {
        if (dataBytes == null || tClass == null) return null;
        return engine.deserialize(dataBytes, tClass);
    }

    public <R> R toObject(Class<R> targetClass) {
        if (dataBytes == null || targetClass == null) return null;
        return engine.deserialize(dataBytes, targetClass);
    }

    public String toJson() {
        return dataBytes == null ? null : new String(dataBytes, charset);
    }

    public byte[] toBytes() {
        return dataBytes;
    }

    public <R> Shift<R> map(Function<T, R> function) {
        T obj = toObject();
        R result = function.apply(obj);
        return Shift.just(result);
    }

    public Shift<T> subscribe(Consumer<T> consumer) {
        T obj = toObject();
        if (obj != null) {
            consumer.accept(obj);
        }
        return this;
    }

    /**
     * Changes the target class reference without modifying the underlying data.
     */
    public <R> Shift<R> toShift(Class<R> rClass) {
        return new Shift<>(this.charset, this.dataBytes, rClass);
    }
}