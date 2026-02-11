package io.qoop.property.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = {PropertyInjector.class, PaymentService.class})
class PropertyInjectionDefaultValueTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void testDefaultValue() {
        assertEquals("3000", paymentService.getTimeout());
    }
}
