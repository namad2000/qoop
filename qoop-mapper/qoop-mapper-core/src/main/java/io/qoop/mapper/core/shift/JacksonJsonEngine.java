package io.qoop.mapper.core.shift;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qoop.mapper.api.shift.JsonEngine;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;

public class JacksonJsonEngine implements JsonEngine {

    private final ObjectMapper mapper;

    public JacksonJsonEngine(String dateFormat) {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat(dateFormat))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    @SneakyThrows
    public <T> T deserialize(byte[] data, Class<T> tClass) {
        return mapper.readValue(data, tClass);
    }

    @Override
    @SneakyThrows
    public byte[] serialize(Object obj) {
        return mapper.writeValueAsBytes(obj);
    }
}