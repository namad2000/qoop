package io.qoop.property.core;


import io.qoop.property.api.Property;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 * Package: io.qoop.infrastructure.model
 */

public class PaymentService {

    @Property(key = "payment.timeout", defaultValue = "3000")
    private String timeout;

    public String getTimeout() {
        return timeout;
    }
}
