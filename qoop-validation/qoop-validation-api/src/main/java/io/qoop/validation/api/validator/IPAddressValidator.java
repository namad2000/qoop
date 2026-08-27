package io.qoop.validation.api.validator;

import io.qoop.fault.handler.api.exception.DomainValidationException;
import io.qoop.validation.api.IPAddress;

import java.util.regex.Pattern;

import static io.qoop.validation.api.exception.ValidationExceptionCode.INVALID_IP_ADDRESS;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */
public class IPAddressValidator implements AnnotationValidator<CharSequence, IPAddress> {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|24[0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|24[0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|24[0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|24[0-9]|[01]?[0-9][0-9]?)$"
    );

    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
            "^::([0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,7}:$"
    );

    @Override
    public void validate(CharSequence value, IPAddress annotation, String paramName) {
        if (value == null || value.length() == 0) {
            return;
        }

        String ipStr = value.toString().trim();
        boolean isValid;

        switch (annotation.type()) {
            case IPv4:
                isValid = IPV4_PATTERN.matcher(ipStr).matches();
                break;
            case IPv6:
                isValid = IPV6_PATTERN.matcher(ipStr).matches();
                break;
            case ANY:
            default:
                isValid = IPV4_PATTERN.matcher(ipStr).matches() || IPV6_PATTERN.matcher(ipStr).matches();
                break;
        }

        if (!isValid) {
            throw DomainValidationException.withParams(INVALID_IP_ADDRESS, paramName, paramName);
        }
    }
}