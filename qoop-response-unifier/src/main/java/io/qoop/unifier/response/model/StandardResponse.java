package io.qoop.unifier.response.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    private String correlationId;
    private int code;
    private String message;
    private T data;
}
