package io.qoop.fault.handler.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorDetail {
    private String field;
    private String code;
    private String message;
}