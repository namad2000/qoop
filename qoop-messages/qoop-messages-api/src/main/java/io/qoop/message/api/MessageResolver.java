package io.qoop.message.api;

import java.util.Locale;

public interface MessageResolver {
    String resolve(String errorCode, Locale locale, Object... params);

    String resolveField(String field, Locale locale, Object... params);
}