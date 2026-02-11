package io.qoop.mapper.api.shift;

/**
 * Strategy interface for JSON operations to keep Domain/Application layers pure.
 */
public interface JsonEngine {
    <T> T deserialize(byte[] data, Class<T> tClass);

    byte[] serialize(Object obj);
}