package io.qoop.outbox.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.mapper.core.CommonsMapperConfig;
import io.qoop.outbox.kafka.KafkaMessage;
import io.qoop.outbox.persistence.entity.ErrorMessageEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.UUID;

@Mapper(config = CommonsMapperConfig.class, imports = {UUID.class, Instant.class})
public interface ErrorMessageMapper {

    @Mapping(target = "id", expression = "java(message.eventId() != null ? message.eventId() : UUID.randomUUID())")
    @Mapping(target = "topic", source = "message.topic")
    @Mapping(target = "key", source = "message.aggregateId")
    @Mapping(target = "payload", source = "message.value", qualifiedByName = "mapPayloadToJson")
    @Mapping(target = "errorMessage", expression = "java(ex != null ? truncateMessage(ex.getMessage(), 2000) : null)")
    @Mapping(target = "exceptionClass", expression = "java(ex != null ? ex.getClass().getName() : null)")
    @Mapping(target = "stackTrace", expression = "java(ex != null ? org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(ex) : null)")
    @Mapping(target = "correlationId", source = "correlationId")
    @Mapping(target = "timestamp", expression = "java(Instant.now())")
    ErrorMessageEntity toEntity(KafkaMessage message, Exception ex, String correlationId, @Context ObjectMapper objectMapper);

    @Named("mapPayloadToJson")
    default String mapPayloadToJson(Object value, @Context ObjectMapper objectMapper) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    default String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() <= maxLength ? message : message.substring(0, maxLength);
    }
}