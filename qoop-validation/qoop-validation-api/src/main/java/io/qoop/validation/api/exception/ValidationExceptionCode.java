package io.qoop.validation.api.exception;


import io.qoop.fault.handler.api.exception.ExceptionCode;

public interface ValidationExceptionCode extends ExceptionCode {
    String VALUE_CANNOT_BE_GREATER_THAN = "MaxValidator-01";
    String VALUE_CANNOT_BE_LESS_THAN = "MinValidator-01";
    String NOT_EMPTY = "NotEmptyValidator-01";
    String NOT_NULL = "NotNullValidator-01";
    String NOT_BLANK = "NotBlankValidator-01";
    String INVALID_PATTERN = "PatternValidator-01";
    String INVALID_EMAIL = "EmailValidator-01";
    String INVALID_SIZE = "SizeValidator-01";
    String MUST_BE_NUMERIC = "NumericValidator-01";
    String INVALID_UUID = "UUIDValidator-01";

    String INVALID_LENGTH = "LengthValidator-01";
    String INVALID_CODE_POINT_LENGTH = "CodePointLengthValidator-01";
    String MUST_BE_IN_PAST = "PastValidator-01";
    String MUST_BE_IN_FUTURE = "FutureValidator-01";
    String INVALID_RANGE = "RangeValidator-01";
    String INVALID_URL = "URLValidator-01";
    String INVALID_IP_ADDRESS = "IPAddressValidator-01";
    String MUST_BE_TRUE = "AssertTrueValidator-01";
    String MUST_BE_FALSE = "AssertFalseValidator-01";
    String MUST_BE_IN_PAST_OR_PRESENT = "PastOrPresentValidator-01";
    String MUST_BE_IN_FUTURE_OR_PRESENT = "FutureOrPresentValidator-01";
    String MUST_BE_POSITIVE = "PositiveValidator-01";
    String MUST_BE_POSITIVE_OR_ZERO = "PositiveOrZeroValidator-01";
    String MUST_BE_NEGATIVE = "NegativeValidator-01";
    String MUST_BE_NEGATIVE_OR_ZERO = "NegativeOrZeroValidator-01";
    String INVALID_DIGITS = "DigitsValidator-01";
    String VALUE_CANNOT_BE_LESS_THAN_DECIMAL = "DecimalMinValidator-01";
    String VALUE_CANNOT_BE_GREATER_THAN_DECIMAL = "DecimalMaxValidator-01";
    String INVALID_ENUM_VALUE = "EnumValueValidator-01";
    String INVALID_CRON_EXPRESSION = "CronValidator-01";

    //----- Iranian Validation -----
    String INVALID_LEGAL_NATIONAL_ID = "LegalNationalIdValidator-01";
    String INVALID_NATIONAL_CODE = "NationalCodeValidator-01";
    String INVALID_FOREIGN_CODE = "ForeignCodeValidator-01";
    String INVALID_IRANIAN_MOBILE_NUMBER = "IranianMobileNumberValidator-01";
    String INVALID_IRANIAN_POSTAL_CODE = "IranianPostalCodeValidator-01";
    String INVALID_SHETAB_CARD_NUMBER = "ShetabCardNumberValidator-01";
    String INVALID_IRANIAN_IBAN = "IranianIBANValidator-01";
}
