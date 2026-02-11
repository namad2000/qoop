package io.qoop.mapper.api;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class MappingContext {
    Map<String, Object> data;

    public static MappingContext empty() {
        return new MappingContext(new HashMap<>());
    }

    public static MappingContext of(String key, Object value) {
        return new MappingContext(Map.of(key, value));
    }

    public static MappingContext of(Map<String, Object> data) {
        return new MappingContext(data);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(data.get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public MappingContext put(String key, Object value) {
        data.put(key, value);
        return this;
    }
}
