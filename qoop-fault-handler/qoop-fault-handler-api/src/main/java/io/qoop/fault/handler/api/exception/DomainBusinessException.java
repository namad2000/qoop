package io.qoop.fault.handler.api.exception;

import lombok.Getter;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Getter
public class DomainBusinessException extends DomainException {

    protected DomainBusinessException(String code, Object... params) {
        super(code, 422, params);
    }

    public static DomainBusinessException of(String code) {
        return new DomainBusinessException(code);
    }

    public static DomainBusinessException withParams(String code, Object... params) {
        return new DomainBusinessException(code, params);
    }
}
