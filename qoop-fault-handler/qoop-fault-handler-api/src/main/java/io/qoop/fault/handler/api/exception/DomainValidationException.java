package io.qoop.fault.handler.api.exception;

import lombok.Getter;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Getter
public class DomainValidationException extends DomainException {
    protected final String paramName;

    protected DomainValidationException(String code, String paramName, Object... params) {
        super(code, 400, params);
        this.paramName = paramName;
    }

    public static DomainValidationException of(String code, String paramName) {
        return new DomainValidationException(code, paramName);
    }

    public static DomainValidationException withParams(String code, String paramName, Object... params) {
        return new DomainValidationException(code, paramName, params);
    }
}
