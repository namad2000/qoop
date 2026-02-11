package io.qoop.fault.handler.api.exception;

import lombok.Getter;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Getter
public class DomainException extends RuntimeException {
    protected final String code;
    protected final int httpStatus;
    protected final Object[] params;

    protected DomainException(String code, int httpStatus, Object[] params) {
        super(code);
        this.code = code;
        this.httpStatus = httpStatus;
        this.params = params;
    }

    public static DomainException of(String code) {
        return DomainException.of(code, 400, (Object) null);
    }

    public static DomainException of(String code, int httpStatus) {
        return DomainException.of(code, httpStatus, (Object) null);
    }

    public static DomainException withParams(String code, Object... params) {
        return DomainException.of(code, 400, params);
    }

    public static DomainException of(String code, int httpStatus, Object... params) {
        return new DomainException(code, httpStatus, params);
    }
}
