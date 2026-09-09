package io.qoop.shedlock.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShedLock Properties Tests")
class ShedLockPropertiesTest {

    @Test
    @DisplayName("Should have default values")
    void shouldHaveDefaultValues() {
        // Given
        ShedLockProperties properties = new ShedLockProperties();

        // Then
        assertThat(properties.getProvider()).isEqualTo("database");
        assertThat(properties.getDefaultLockAtMostFor()).isEqualTo("PT30S");
        assertThat(properties.getDatabase().getTableName()).isEqualTo("shedlock");
        assertThat(properties.getRedis().getKeyPrefix()).isEqualTo("shedlock");
        assertThat(properties.getRedis().getEnvironment()).isNull();
    }

    @Test
    @DisplayName("Should allow setting custom values")
    void shouldAllowSettingCustomValues() {
        // Given
        ShedLockProperties properties = new ShedLockProperties();

        // When
        properties.setProvider("redis");
        properties.setDefaultLockAtMostFor("PT1H");
        properties.getDatabase().setTableName("custom_lock");
        properties.getRedis().setKeyPrefix("custom-prefix");
        properties.getRedis().setEnvironment("prod");

        // Then
        assertThat(properties.getProvider()).isEqualTo("redis");
        assertThat(properties.getDefaultLockAtMostFor()).isEqualTo("PT1H");
        assertThat(properties.getDatabase().getTableName()).isEqualTo("custom_lock");
        assertThat(properties.getRedis().getKeyPrefix()).isEqualTo("custom-prefix");
        assertThat(properties.getRedis().getEnvironment()).isEqualTo("prod");
    }

    @Test
    @DisplayName("Should have nested configuration objects")
    void shouldHaveNestedConfigurationObjects() {
        // Given
        ShedLockProperties properties = new ShedLockProperties();

        // Then
        assertThat(properties.getDatabase()).isNotNull();
        assertThat(properties.getRedis()).isNotNull();
    }
}