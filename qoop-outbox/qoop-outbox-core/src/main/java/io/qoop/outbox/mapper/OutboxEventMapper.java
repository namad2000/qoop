package io.qoop.outbox.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.outbox.OutBoxEvent;
import io.qoop.outbox.OutboxStatus;
import io.qoop.outbox.persistence.entity.OutboxEventEntity;
import io.qoop.stream.api.Header;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, OutboxStatus.class, LocalDateTime.class})
public interface OutboxEventMapper extends BaseMapper {

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
}