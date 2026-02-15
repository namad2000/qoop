package io.qoop.validation.api.exception;


import io.qoop.fault.handler.api.exception.ExceptionCode;

public interface ValidationExceptionCode extends ExceptionCode {
    String VALUE_CANNOT_BE_GREATER_THAN = "MaxValidator-01";
    String VALUE_CANNOT_BE_LESS_THAN = "MinValidator-01";
    String NOT_EMPTY = "NotEmptyValidator-01";
    String NOT_NULL = "NotNullValidator-01";
}
