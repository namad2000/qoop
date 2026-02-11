package io.qoop.validation.core.validator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestDto {
    @NotConstraint
    private String field;

    public TestDto(String field) {
        this.field = field;
    }
}
