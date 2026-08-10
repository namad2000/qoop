package io.qoop.message.api.core;

import io.qoop.message.api.MessageResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ErrorMessageResolver implements MessageResolver {

    private final MessageSource messagesSource;
    private final MessageSource fieldsSource;

    public ErrorMessageResolver(MessageSource messagesSource,
                                @Qualifier("fieldsMessageSource") MessageSource fieldsSource) {
        this.messagesSource = messagesSource;
        this.fieldsSource = fieldsSource;
    }

    @Override
    public String resolve(String errorCode, Locale locale, Object... params) {

        // 1. Retrieve the message template from messages properties
        String template;
        try {
            template = messagesSource.getMessage(errorCode, null, locale);
        } catch (NoSuchMessageException e) {
            // If the message code does not exist, return the error code itself
            // and skip parameter replacement
            return errorCode;
        }

        // If there are no parameters, return the template as-is
        if (params == null || params.length == 0) {
            return template;
        }

        String resolvedMessage = template;

        // 2. Replace placeholders ({0}, {1}, ...) with provided parameters
        for (int i = 0; i < params.length; i++) {
            String param = String.valueOf(params[i]);

            // Try to translate the parameter as a field name using fields properties
            String translatedParam = param;
            try {
                translatedParam = resolveField(param, locale);
            } catch (NoSuchMessageException ignored) {
                // If no field translation is found, use the original parameter value
            }

            resolvedMessage = resolvedMessage.replace(
                    "{" + i + "}",
                    translatedParam
            );
        }

        return resolvedMessage;
    }

    @Override
    public String resolveField(String field, Locale locale, Object... params) {
        return fieldsSource.getMessage(field, null, locale);
    }
}
