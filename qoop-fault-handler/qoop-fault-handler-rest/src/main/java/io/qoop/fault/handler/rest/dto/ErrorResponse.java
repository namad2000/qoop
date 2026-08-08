package io.qoop.fault.handler.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String correlationId;
    private String code;
    private String message;
    private String field;
    private List<FieldErrorDetail> fieldErrors;
}