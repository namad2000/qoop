package io.qoop.outbox.validator;

import io.qoop.outbox.OutBoxEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Outbox Metadata Validator Tests")
class OutboxMetadataValidatorTest {

    @Test
    @DisplayName("Should validate and return annotation when present")
    void shouldValidateAndReturnAnnotationWhenPresent() {
        // Given
        OutboxMetadataValidator validator = new OutboxMetadataValidator();
        ValidPayload payload = new ValidPayload();

        // When
        OutBoxEvent result = validator.validateAndGetAnnotation(payload);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.channel()).isEqualTo("test-channel");
        assertThat(result.aggregateType()).isEqualTo("TestAggregate");
        assertThat(result.eventType()).isEqualTo("TestEvent");
    }

    @Test
    @DisplayName("Should throw exception when annotation is missing")
    void shouldThrowExceptionWhenAnnotationIsMissing() {
        // Given
        OutboxMetadataValidator validator = new OutboxMetadataValidator();
        InvalidPayload payload = new InvalidPayload();

        // When & Then
        assertThatThrownBy(() -> validator.validateAndGetAnnotation(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload class must be annotated with @OutBoxEvent");
    }

    @Test
    @DisplayName("Should throw exception when payload is null")
    void shouldThrowExceptionWhenPayloadIsNull() {
        // Given
        OutboxMetadataValidator validator = new OutboxMetadataValidator();

        // When & Then
        assertThatThrownBy(() -> validator.validateAndGetAnnotation(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload cannot be null");
    }

    // Test classes
    @OutBoxEvent(channel = "test-channel", aggregateType = "TestAggregate", eventType = "TestEvent")
    private static class ValidPayload {}

    private static class InvalidPayload {}
}