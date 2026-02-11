package io.qoop.property.core;

import io.qoop.property.api.Property;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = PropertyInjectionFullTypesTest.TestConfig.class)
class PropertyInjectionFullTypesTest {

    @Autowired
    private SampleService sampleService;

    @Test
    void testDefaultValuesInjection() {
        assertEquals("defaultString", sampleService.getStringValue());
        assertEquals(42, sampleService.getIntValue());
        assertEquals(123456789L, sampleService.getLongValue());
        assertTrue(sampleService.isBooleanValue());
        assertEquals(3.14, sampleService.getDoubleValue());
    }

    @Getter
    static class SampleService {
        @Property(key = "sample.string", defaultValue = "defaultString")
        private String stringValue;

        @Property(key = "sample.int", defaultValue = "42")
        private int intValue;

        @Property(key = "sample.long", defaultValue = "123456789")
        private long longValue;

        @Property(key = "sample.boolean", defaultValue = "true")
        private boolean booleanValue;

        @Property(key = "sample.double", defaultValue = "3.14")
        private double doubleValue;

    }

    @Configuration
    @Import(PropertyInjector.class)
    static class TestConfig {
        @Bean
        public SampleService sampleService() {
            return new SampleService();
        }
    }
}
