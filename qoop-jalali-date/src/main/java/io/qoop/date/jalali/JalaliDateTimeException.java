package io.qoop.date.jalali;

import io.qoop.fault.handler.api.exception.DomainException;

public class JalaliDateTimeException extends DomainException {
    protected JalaliDateTimeException(String code, Object... params) {
        super(code, 400, params);
    }

    public static JalaliDateTimeException of(String code, Object... params) {
        return new JalaliDateTimeException(code, params);
    }
}
