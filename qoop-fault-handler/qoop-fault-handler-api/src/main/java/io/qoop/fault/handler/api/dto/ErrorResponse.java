package io.qoop.fault.handler.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class ErrorResponse {
    private String correlationId;   // currently nullable, to be set later
    private String code;
    private String message;
    private String field;           // for DomainValidationException
}
