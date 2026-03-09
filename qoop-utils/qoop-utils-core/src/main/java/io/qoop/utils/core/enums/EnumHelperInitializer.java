package io.qoop.utils.core.enums;

import io.qoop.utils.api.enums.EnumHelper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class EnumHelperInitializer {

    @Value("${app.enum.package:}")
    private String enumPackage;

    @PostConstruct
    public void init() {
        EnumHelper.enumPackage = enumPackage;
    }
}