package io.qoop.outbox.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.OutBoxEvent;
import io.qoop.outbox.OutboxStatus;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.stream.api.Header;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper responsible for transforming event payloads, metadata,
 * and explicit parameters into persistence-ready OutboxEventEntity instances.
 */
@Mapper(componentModel = "spring", imports = {UUID.class, OutboxStatus.class, LocalDateTime.class})
public interface OutboxEventMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "aggregateType", expression = "java(metadata.aggregateType())")
    @Mapping(target = "aggregateId", source = "aggregateId")
    @Mapping(target = "eventType", expression = "java(metadata.eventType())")
    @Mapping(target = "topic", expression = "java(metadata.channel())")
    @Mapping(target = "payload", expression = "java(serializePayload(payload, objectMapper))")
    @Mapping(target = "headers", expression = "java(serializeHeaders(metadata, explicitHeaders, objectMapper))")
    @Mapping(target = "status", expression = "java(OutboxStatus.NEW)")
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "version", constant = "0L")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "sentAt", ignore = true)
    OutboxEventEntity toEntity(Object payload,
                               String aggregateId,
                               OutBoxEvent metadata,
                               List<Header> explicitHeaders,
                               @Context ObjectMapper objectMapper);

    @Named("serializePayload")
    default String serializePayload(Object payload, @Context ObjectMapper objectMapper) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null for serialization.");
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload to JSON", e);
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
            return null;
        }

        try {
            return objectMapper.writeValueAsString(combinedHeaders);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize headers to JSON", e);
        }
    }
}