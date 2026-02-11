package io.qoop.validation.core.validator;

import io.qoop.validation.api.NotEmpty;

public class NestedInfo {
    @NotEmpty
    private String title;

    public NestedInfo(String title) {
        this.title = title;
    }
}
