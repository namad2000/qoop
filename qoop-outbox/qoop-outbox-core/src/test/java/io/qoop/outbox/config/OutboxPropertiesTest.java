package io.qoop.outbox.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Outbox Properties Tests")
class OutboxPropertiesTest {

    @Test
    @DisplayName("Should have default values")
    void shouldHaveDefaultValues() {
        // Given
        OutboxProperties properties = new OutboxProperties();

        // Then
        assertThat(properties.getPublisherMode()).isEqualTo("batch");
        assertThat(properties.getGridSize()).isEqualTo(4);
        assertThat(properties.getChunkSize()).isEqualTo(100);
        assertThat(properties.getFixedDelay()).isEqualTo(2000L);
        assertThat(properties.getCleanup().isEnabled()).isTrue();
        assertThat(properties.getCleanup().getCron()).isEqualTo("0 0 3 * * *");
        assertThat(properties.getCleanup().getRetentionDays()).isEqualTo(1);
        assertThat(properties.getBatch().getGridSize()).isEqualTo(4);
        assertThat(properties.getBatch().getChunkSize()).isEqualTo(100);
        assertThat(properties.getBatch().getFixedDelay()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("Should allow setting custom values on root")
    void shouldAllowSettingCustomValuesOnRoot() {
        // Given
        OutboxProperties properties = new OutboxProperties();

        // When
        properties.setPublisherMode("debezium");
        properties.setGridSize(8);
        properties.setChunkSize(200);
        properties.setFixedDelay(5000L);
        properties.getCleanup().setEnabled(false);
        properties.getCleanup().setCron("0 0 2 * * *");
        properties.getCleanup().setRetentionDays(7);

        // Then
        assertThat(properties.getPublisherMode()).isEqualTo("debezium");
        assertThat(properties.getGridSize()).isEqualTo(8);
        assertThat(properties.getChunkSize()).isEqualTo(200);
        assertThat(properties.getFixedDelay()).isEqualTo(5000L);
        assertThat(properties.getCleanup().isEnabled()).isFalse();
        assertThat(properties.getCleanup().getCron()).isEqualTo("0 0 2 * * *");
        assertThat(properties.getCleanup().getRetentionDays()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should allow setting custom values on batch")
    void shouldAllowSettingCustomValuesOnBatch() {
        // Given
        OutboxProperties properties = new OutboxProperties();

        // When
        properties.getBatch().setGridSize(6);
        properties.getBatch().setChunkSize(150);
        properties.getBatch().setFixedDelay(3000L);

        // Then
        assertThat(properties.getBatch().getGridSize()).isEqualTo(6);
        assertThat(properties.getBatch().getChunkSize()).isEqualTo(150);
        assertThat(properties.getBatch().getFixedDelay()).isEqualTo(3000L);
        // Root remains default
        assertThat(properties.getGridSize()).isEqualTo(4);
        assertThat(properties.getChunkSize()).isEqualTo(100);
        assertThat(properties.getFixedDelay()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("Should allow setting custom values on both root and batch")
    void shouldAllowSettingCustomValuesOnBoth() {
        // Given
        OutboxProperties properties = new OutboxProperties();

        // When
        properties.setGridSize(8);
        properties.setChunkSize(200);
        properties.setFixedDelay(5000L);
        properties.getBatch().setGridSize(6);
        properties.getBatch().setChunkSize(150);
        properties.getBatch().setFixedDelay(3000L);

        // Then
        assertThat(properties.getGridSize()).isEqualTo(8);
        assertThat(properties.getChunkSize()).isEqualTo(200);
        assertThat(properties.getFixedDelay()).isEqualTo(5000L);
        assertThat(properties.getBatch().getGridSize()).isEqualTo(6);
        assertThat(properties.getBatch().getChunkSize()).isEqualTo(150);
        assertThat(properties.getBatch().getFixedDelay()).isEqualTo(3000L);
    }
}