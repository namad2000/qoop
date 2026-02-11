package io.qoop.mapper.core.shift;

import io.qoop.mapper.api.shift.JsonEngine;
import io.qoop.mapper.api.shift.Shift;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShiftInitializer {

    private final JsonEngine jsonEngine;

    @PostConstruct
    public void init() {
        Shift.setup(jsonEngine);
    }
}