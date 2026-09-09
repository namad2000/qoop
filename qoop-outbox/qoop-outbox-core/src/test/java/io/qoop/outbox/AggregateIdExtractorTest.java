package io.qoop.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Aggregate ID Extractor Tests")
class AggregateIdExtractorTest {

    @Test
    @DisplayName("Should extract aggregate ID from annotated field")
    void shouldExtractAggregateIdFromAnnotatedField() {
        // Given
        AggregateIdExtractor extractor = new AggregateIdExtractor();
        TestPayload payload = new TestPayload("order-123");

        // When
        String result = extractor.extract(payload);

        // Then
        assertThat(result).isEqualTo("order-123");
    }

    @Test
    @DisplayName("Should return null when annotated field is null")
    void shouldReturnNullWhenAnnotatedFieldIsNull() {
        // Given
        AggregateIdExtractor extractor = new AggregateIdExtractor();
        TestPayload payload = new TestPayload(null);

        // When
        String result = extractor.extract(payload);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should throw exception when no @AggregateId field exists")
    void shouldThrowExceptionWhenNoAggregateIdFieldExists() {
        // Given
        AggregateIdExtractor extractor = new AggregateIdExtractor();
        InvalidPayload payload = new InvalidPayload("test");

        // When & Then
        assertThatThrownBy(() -> extractor.extract(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No field annotated with @AggregateId found");
    }

    @Test
    @DisplayName("Should throw exception when payload is null")
    void shouldThrowExceptionWhenPayloadIsNull() {
        // Given
        AggregateIdExtractor extractor = new AggregateIdExtractor();

        // When & Then
        assertThatThrownBy(() -> extractor.extract(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload cannot be null");
    }

    // Test classes
    private static class TestPayload {
        @AggregateId
        private final String orderId;

        TestPayload(String orderId) {
            this.orderId = orderId;
        }
    }

    private static class InvalidPayload {
        private final String id;

        InvalidPayload(String id) {
            this.id = id;
        }
    }
}