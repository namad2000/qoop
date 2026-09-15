package io.qoop.outbox.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.OutBoxEvent;
import io.qoop.stream.api.Header;
import org.mapstruct.Context;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface BaseMapper {

    @Named("serializePayload")
    default String serializePayload(Object payload, @Context ObjectMapper objectMapper) {
        if (payload == null) {
            return "{}";
        }

        if (payload instanceof String strPayload) {
            String trimmed = strPayload.trim();
            if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                return trimmed;
            }
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Named("serializeHeaders")
    default String serializeHeaders(OutBoxEvent metadata, List<Header> explicitHeaders, @Context ObjectMapper objectMapper) {
        List<Header> combinedHeaders = new ArrayList<>();

        if (metadata != null && metadata.headers() != null) {
            List<Header> headers = Arrays.stream(metadata.headers())
                    .map(header -> new Header(header.name(), header.value())).toList();
            combinedHeaders.addAll(headers);
        }

        if (explicitHeaders != null) {
            combinedHeaders.addAll(explicitHeaders);
        }

        if (combinedHeaders.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(combinedHeaders);
        } catch (Exception e) {
            return "[]";
        }
    }
}