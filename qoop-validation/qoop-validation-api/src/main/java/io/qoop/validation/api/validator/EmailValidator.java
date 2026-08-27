package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Email;

import java.util.regex.Pattern;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_EMAIL;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class EmailValidator implements AnnotationValidator<CharSequence, Email> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Override
    public void validate(CharSequence value, Email annotation, String paramName) {
        if (value == null || value.length() == 0) {
            return;
        }

        String email = value.toString().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw DomainValidationException.withParams(INVALID_EMAIL, paramName, paramName);
        }
    }
}