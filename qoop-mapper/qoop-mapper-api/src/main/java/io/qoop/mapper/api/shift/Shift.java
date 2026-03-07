package io.qoop.mapper.api.shift;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void subscribe(Consumer<T> consumer) {
        T obj = toObject();
        if (obj != null) {
            consumer.accept(obj);
        }
    }

    /**
     * Changes the target class reference without modifying the underlying data.
     */
    public <R> Shift<R> toShift(Class<R> rClass) {
        return new Shift<>(this.charset, this.dataBytes, rClass);
    }

    // --- NEW: Collection Support Methods ---

    /**
     * Converts the current object (which can be a List) to a List of the specified target class.
     * This method handles the iteration and mapping of collection items automatically.
     */
    public <R> List<R> toList(Class<R> targetClass) {
        if (dataBytes == null || targetClass == null) {
            return new ArrayList<>();
        }

        Object rawObject = engine.deserialize(dataBytes, Object.class);

        if (!(rawObject instanceof Collection<?> sourceCollection)) {
            return new ArrayList<>();
        }

        return sourceCollection.stream()
                .map(item -> {
                    if (item == null) return null;
                    byte[] itemBytes = engine.serialize(item);
                    return engine.deserialize(itemBytes, targetClass);
                })
                .collect(Collectors.toList());
    }

    /**
     * Maps a List of Source objects to a List of Target objects using a custom mapper function.
     * Usage: Shift.just(sourceList).mapList(Target.class, item -> { ... logic ... return item; });
     */
    public <R> List<R> mapList(Class<R> targetClass, Function<R, R> mapperFunction) {
        List<R> list = toList(targetClass);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(mapperFunction)
                .collect(Collectors.toList());
    }

    // --- NEW: Set Support Methods ---

    /**
     * Converts the current object (which can be a List or Set) to a Set of the specified target class.
     * This method handles the iteration and mapping of collection items automatically.
     */
    public <R> Set<R> toSet(Class<R> targetClass) {
        if (dataBytes == null || targetClass == null) {
            return new HashSet<>();
        }

        Object rawObject = engine.deserialize(dataBytes, Object.class);

        if (!(rawObject instanceof Collection<?> sourceCollection)) {
            return new HashSet<>();
        }

        return sourceCollection.stream()
                .map(item -> {
                    if (item == null) return null;
                    byte[] itemBytes = engine.serialize(item);
                    return engine.deserialize(itemBytes, targetClass);
                })
                .collect(Collectors.toSet());
    }

    /**
     * Maps a Collection of Source objects to a Set of Target objects using a custom mapper function.
     */
    public <R> Set<R> mapSet(Class<R> targetClass, Function<R, R> mapperFunction) {
        Set<R> set = toSet(targetClass);
        if (set == null || set.isEmpty()) {
            return new HashSet<>();
        }
        return set.stream()
                .map(mapperFunction)
                .collect(Collectors.toSet());
    }
}