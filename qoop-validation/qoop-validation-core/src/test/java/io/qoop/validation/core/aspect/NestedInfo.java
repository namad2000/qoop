package io.qoop.validation.core.aspect;

import io.qoop.validation.api.NotEmpty;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

public class NestedInfo {

    @NotEmpty
    private String title;

    public NestedInfo(String title) {
        this.title = title;
    }
}
