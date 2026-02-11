package io.qoop.utils.api.jwt;


import io.qoop.fault.handler.api.exception.ExceptionCode;

public interface JwtExceptionCode extends ExceptionCode {
    String TOKEN_EXPIRED = "JWT_01";
    String INVALID_TOKEN = "JWT_02";
}
