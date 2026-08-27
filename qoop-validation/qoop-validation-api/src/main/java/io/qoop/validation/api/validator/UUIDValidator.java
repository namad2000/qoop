package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.UUID;

import java.util.regex.Pattern;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_UUID;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class UUIDValidator implements AnnotationValidator<CharSequence, UUID> {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
    );

    @Override
    public void validate(CharSequence value, UUID annotation, String paramName) {
        if (value == null || value.length() == 0) {
            return;
        }

        String uuidStr = value.toString().trim();
        if (!UUID_PATTERN.matcher(uuidStr).matches()) {
            throw DomainValidationException.withParams(INVALID_UUID, paramName, paramName);
        }
    }
}