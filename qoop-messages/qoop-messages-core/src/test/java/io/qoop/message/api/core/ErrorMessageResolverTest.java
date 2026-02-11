package io.qoop.message.api.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/29/2025 11:28 AM
 * Package: io.qoop.message
 */
@SpringBootTest
@ContextConfiguration(classes = {I18nConfig.class})
public class ErrorMessageResolverTest {

    @Autowired
    private MessageSource messagesSource;

    @Autowired
    @Qualifier("fieldsMessageSource")
    private MessageSource fieldsSource;

    private ErrorMessageResolver resolver;

    @BeforeEach
    public void setUp() {
        resolver = new ErrorMessageResolver(messagesSource, fieldsSource);
    }

    @Test
    public void testResolveMessageWithFieldTranslation() {
        String message = resolver.resolve(
                "INVALID_INPUT",
                Locale.ENGLISH,
                "email"
        );

        assertEquals("Field Email is invalid", message);
    }

    @Test
    public void testResolveMessageWithoutFieldTranslation() {
        String message = resolver.resolve(
                "INVALID_INPUT",
                Locale.ENGLISH,
                "unknownField"
        );

        // Should use the parameter as-is because field translation does not exist
        assertEquals("Field unknownField is invalid", message);
    }

    @Test
    public void testResolveMessageInPersian() {
        Locale fa = Locale.of("fa", "IR");

        String message = resolver.resolve(
                "INVALID_INPUT",
                fa,
                "email"
        );

        assertEquals("فیلد ایمیل نامعتبر است", message);
    }

    @Test
    public void testResolveMessageErrorCodeNotFound() {
        String message = resolver.resolve(
                "UNKNOWN_CODE",
                Locale.ENGLISH,
                "email"
        );

        // Should return the error code itself when the message is not found
        assertEquals("UNKNOWN_CODE", message);
    }
}
