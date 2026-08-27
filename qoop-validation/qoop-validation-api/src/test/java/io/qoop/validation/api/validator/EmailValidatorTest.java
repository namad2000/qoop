package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.Email;
import io.qoop.validation.api.exception.ValidationExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    private Email createEmail() {
        return new Email() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Email.class;
            }
        };
    }

    @Test
    @DisplayName("Should pass when value is null or empty")
    void validate_NullOrEmptyValue_Success() {
        Email annotation = createEmail();
        assertDoesNotThrow(() -> validator.validate(null, annotation, "email"));
        assertDoesNotThrow(() -> validator.validate("", annotation, "email"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"daak1365@gmail.com", "daak1365@yahoo.com", "user.name+tag@domain.co.ir"})
    @DisplayName("Should pass when email address is valid")
    void validate_ValidEmail_Success(String input) {
        Email annotation = createEmail();
        assertDoesNotThrow(() -> validator.validate(input, annotation, "email"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "@domain.com", "user@.com", "user@domain", "user@domain..com"})
    @DisplayName("Should throw exception when email address is invalid")
    void validate_InvalidEmail_ThrowsDomainValidationException(String input) {
        Email annotation = createEmail();

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> validator.validate(input, annotation, "email")
        );

        assertEquals(ValidationExceptionCode.INVALID_EMAIL, exception.getCode());
    }
}