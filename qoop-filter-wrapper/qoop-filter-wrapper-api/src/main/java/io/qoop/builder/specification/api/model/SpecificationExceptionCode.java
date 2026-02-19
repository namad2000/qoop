package io.qoop.builder.specification.api.model;

import io.qoop.fault.handler.api.exception.ExceptionCode;

public interface SpecificationExceptionCode extends ExceptionCode {
    String FILTER_INVALID_JSON = "FILTER_INVALID_01";
    String FILTER_INVALID_OPERATOR = "FILTER_INVALID_02";
    String FILTER_INVALID_PROPERTY = "FILTER_INVALID_03";
    String FILTER_CAST_ERROR = "FILTER_CAST_ERROR_01";

    String SORT_INVALID_JSON = "SORT_INVALID_01";
    String SORT_INVALID_OPERATOR = "SORT_INVALID_02";
    String SORT_INVALID_PROPERTY = "SORT_INVALID_03";
    String SORT_CAST_ERROR = "SORT_CAST_ERROR_01";
}