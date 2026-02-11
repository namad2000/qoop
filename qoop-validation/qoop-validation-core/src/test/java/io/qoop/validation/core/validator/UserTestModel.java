package io.qoop.validation.core.validator;

import io.qoop.validation.api.IsValid;
import io.qoop.validation.api.NotEmpty;

public class UserTestModel {

    @NotEmpty
    private String username;

    @IsValid
    private NestedInfo nested;

    public UserTestModel(String username, NestedInfo nested) {
        this.username = username;
        this.nested = nested;
    }
}
