package io.qoop.property.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {"payment.timeout=5000"})
@ContextConfiguration(classes = {PropertyInjector.class, PaymentService.class})
class propertyInjectionTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void testPropertyInjection() {
        assertEquals("5000", paymentService.getTimeout());
    }
}
