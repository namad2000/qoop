package io.qoop.utils.api.date.qoop;


import io.qoop.fault.handler.api.exception.ExceptionCode;

public interface QoopDateExceptionCode extends ExceptionCode {
    String INVALID_GREGORIAN_DATE_RANGE = "QOOP_DATE_01";
    String INVALID_PERSIAN_MONTH_NUMBER = "QOOP_DATE_02";
    String INVALID_PERSIAN_YEAR = "QOOP_DATE_03";
    String INVALID_PERSIAN_YEAR_RANGE = "QOOP_DATE_04";
    String INVALID_PERSIAN_MONTH_RANGE = "QOOP_DATE_05";
    String INVALID_PERSIAN_DAY_RANGE = "QOOP_DATE_06";
    String INVALID_HOUR_RANGE = "QOOP_DATE_07";
    String INVALID_MINUTE_RANGE = "QOOP_DATE_08";
    String INVALID_SECOND_RANGE = "QOOP_DATE_09";
    String INVALID_MICROSECOND_RANGE = "QOOP_DATE_10";
}
