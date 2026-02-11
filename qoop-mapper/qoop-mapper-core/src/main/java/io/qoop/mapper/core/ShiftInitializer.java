package io.qoop.mapper.core;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.Shift;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

@Component
public class ShiftInitializer implements SmartInitializingSingleton {

    private final JsonEngine jsonEngine;

    public ShiftInitializer(JsonEngine jsonEngine) {
        this.jsonEngine = jsonEngine;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Shift.setup(jsonEngine);
    }
}
