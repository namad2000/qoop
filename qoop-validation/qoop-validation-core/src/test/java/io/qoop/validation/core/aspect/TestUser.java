package io.qoop.validation.core.aspect;

import io.qoop.validation.api.IsValid;
import io.qoop.validation.api.NotEmpty;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class TestUser {

    @NotEmpty
    private String name;

    @IsValid
    private NestedInfo nested;

    public TestUser(String name, NestedInfo nested) {
        this.name = name;
        this.nested = nested;
    }
}
